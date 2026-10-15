package com.wastewise.service;

import com.wastewise.dto.RegistrationDto;
import com.wastewise.entity.User;
import com.wastewise.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RewardService rewardService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RewardService rewardService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.rewardService = rewardService;
    }

    @Transactional
    public User registerUser(RegistrationDto dto) {
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Username '" + dto.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(dto.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already registered.");
        }

        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setWard(dto.getWard() != null && !dto.getWard().isBlank() ? dto.getWard().trim() : "Ward 12 - Green Valley");
        user.setRole("ROLE_USER");
        user.setTotalEcoPoints(0);
        user.setStreakDays(1);
        user.setLastActiveDate(LocalDate.now());
        user.setCreatedAt(LocalDateTime.now());

        log.info("Registered new user: {}", user.getUsername());
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User findOrCreateGoogleUser(String email, String name) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google account must provide an email.");
        }
        String cleanEmail = email.toLowerCase().trim();
        Optional<User> existing = userRepository.findByEmail(cleanEmail);
        if (existing.isPresent()) {
            User u = existing.get();
            u.setLastActiveDate(LocalDate.now());
            if (!u.isEmailVerified()) {
                u.setEmailVerified(true);
            }
            return userRepository.save(u);
        }

        String username = (name != null && !name.isBlank()) ? name.trim().replaceAll("\\s+", "_") : cleanEmail.split("@")[0];
        if (userRepository.existsByUsername(username)) {
            username = username + "_" + (System.currentTimeMillis() % 1000);
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(cleanEmail);
        newUser.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        newUser.setRole("ROLE_USER");
        newUser.setTotalEcoPoints(0);
        newUser.setStreakDays(1);
        newUser.setEmailVerified(true);
        newUser.setLocationSetupComplete(false); // will prompt for location on first login
        newUser.setLastActiveDate(LocalDate.now());
        newUser.setCreatedAt(LocalDateTime.now());

        log.info("Created new citizen account via Google authentication: {} ({})", username, cleanEmail);
        return userRepository.save(newUser);
    }

    /**
     * Resolve a User from an Authentication object.
     * Works seamlessly for form login, Google OAuth2, and Google OIDC.
     */
    public User resolveUser(org.springframework.security.core.Authentication auth) {
        if (auth == null) {
            throw new org.springframework.security.core.userdetails.UsernameNotFoundException("No authentication found");
        }

        // 1. If OAuth2 / OIDC authentication, inspect attributes directly
        if (auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User oauthUser) {
            String email = oauthUser.getAttribute("email");
            if (email != null && !email.isBlank()) {
                Optional<User> byEmail = userRepository.findByEmail(email.toLowerCase().trim());
                if (byEmail.isPresent()) {
                    return byEmail.get();
                }
            }

            String fullName = oauthUser.getAttribute("name");
            if (fullName != null && !fullName.isBlank()) {
                String potentialUsername = fullName.trim().replaceAll("\\s+", "_");
                Optional<User> byName = userRepository.findByUsername(potentialUsername);
                if (byName.isPresent()) {
                    return byName.get();
                }
            }

            // Auto-provision if somehow not created yet
            if (email != null && !email.isBlank()) {
                return findOrCreateGoogleUser(email, fullName);
            }
        }

        // 2. Form login or fallback by name
        String name = auth.getName();
        if (name != null && !name.isBlank()) {
            Optional<User> byUsername = userRepository.findByUsername(name);
            if (byUsername.isPresent()) {
                return byUsername.get();
            }

            Optional<User> byEmail = userRepository.findByEmail(name.toLowerCase().trim());
            if (byEmail.isPresent()) {
                return byEmail.get();
            }
        }

        throw new org.springframework.security.core.userdetails.UsernameNotFoundException(
                "No WasteWise account found for: " + name);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void updateUserActivityAndStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastActive = user.getLastActiveDate();

        if (lastActive == null) {
            user.setStreakDays(1);
            user.setLastActiveDate(today);
            userRepository.save(user);
            return;
        }

        long daysBetween = ChronoUnit.DAYS.between(lastActive, today);

        if (daysBetween == 1) {
            // Consecutive day: increment streak
            int newStreak = user.getStreakDays() + 1;
            user.setStreakDays(newStreak);
            user.setLastActiveDate(today);

            if (newStreak % 7 == 0) {
                // Award 7-day streak bonus points
                rewardService.awardStreakBonus(user, newStreak);
            }
            userRepository.save(user);
        } else if (daysBetween > 1) {
            // Missed a day or more: reset to 1
            user.setStreakDays(1);
            user.setLastActiveDate(today);
            userRepository.save(user);
        }
        // If daysBetween == 0, already active today, no change
    }

    @Transactional
    public User setupLocation(User user, String city, String state, String country) {
        if (city != null && !city.isBlank()) user.setCity(city.trim());
        if (state != null && !state.isBlank()) user.setState(state.trim());
        if (country != null && !country.isBlank()) user.setCountry(country.trim());
        // Auto-generate ward from city for backward compatibility
        user.setWard(user.getLocationDisplay());
        user.setLocationSetupComplete(true);
        log.info("Location set for user '{}': {}, {}, {}", user.getUsername(), city, state, country);
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(User user, String city, String state, String country) {
        if (city != null && !city.isBlank()) user.setCity(city.trim());
        if (state != null && !state.isBlank()) user.setState(state.trim());
        if (country != null && !country.isBlank()) user.setCountry(country.trim());
        user.setWard(user.getLocationDisplay());
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(User user, String ward) {
        if (ward != null && !ward.isBlank()) {
            user.setWard(ward.trim());
        }
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole()))
        );
    }
}
