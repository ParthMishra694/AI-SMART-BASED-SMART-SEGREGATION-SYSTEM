package com.wastewise.service;

import com.wastewise.dto.LeaderboardEntryDto;
import com.wastewise.entity.User;
import com.wastewise.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LeaderboardService {

    private final UserRepository userRepository;

    public LeaderboardService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Get leaderboard filtered by scope:
     * - "city" + value  → filter by city
     * - "state" + value → filter by state
     * - "country" + value → filter by country
     * - null / "ALL"    → global leaderboard
     */
    public List<LeaderboardEntryDto> getLeaderboard(String filterType, String filterValue, User currentUser) {
        List<User> users;

        if (filterType != null && filterValue != null && !filterValue.isBlank() && !filterValue.equalsIgnoreCase("ALL")) {
            switch (filterType.toLowerCase()) {
                case "city":
                    users = userRepository.findByCityIgnoreCaseOrderByTotalEcoPointsDesc(filterValue);
                    break;
                case "state":
                    users = userRepository.findByStateIgnoreCaseOrderByTotalEcoPointsDesc(filterValue);
                    break;
                case "country":
                    users = userRepository.findByCountryIgnoreCaseOrderByTotalEcoPointsDesc(filterValue);
                    break;
                default:
                    users = userRepository.findAllByOrderByTotalEcoPointsDesc();
            }
        } else {
            users = userRepository.findAllByOrderByTotalEcoPointsDesc();
        }

        return buildEntries(users, currentUser);
    }

    /**
     * Legacy method — kept for backward compat with ward-based filtering
     */
    public List<LeaderboardEntryDto> getLeaderboard(String ward, User currentUser) {
        List<User> users;
        if (ward != null && !ward.isBlank() && !ward.equalsIgnoreCase("ALL")) {
            users = userRepository.findByWardOrderByTotalEcoPointsDesc(ward);
        } else {
            users = userRepository.findAllByOrderByTotalEcoPointsDesc();
        }
        return buildEntries(users, currentUser);
    }

    private List<LeaderboardEntryDto> buildEntries(List<User> users, User currentUser) {
        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = 1;
        for (User u : users) {
            String badge = getBadgeForPoints(u.getTotalEcoPoints());
            boolean isCurrent = (currentUser != null && u.getId().equals(currentUser.getId()));
            boolean isDemo = u.getUsername().startsWith("demo_") || u.getUsername().contains("sample");

            entries.add(new LeaderboardEntryDto(
                    rank++,
                    u.getUsername(),
                    u.getWard(),
                    u.getCity(),
                    u.getState(),
                    u.getCountry(),
                    u.getLocationDisplay(),
                    u.getTotalEcoPoints(),
                    badge,
                    isCurrent,
                    isDemo
            ));
        }
        return entries;
    }

    public long getUserRank(User user) {
        if (user == null) return 0;
        return userRepository.countUsersWithMorePoints(user.getTotalEcoPoints()) + 1;
    }

    public List<String> getAvailableWards() {
        return userRepository.findDistinctWards();
    }

    public List<String> getAvailableCities() {
        return userRepository.findDistinctCities();
    }

    public List<String> getAvailableStates() {
        return userRepository.findDistinctStates();
    }

    public List<String> getAvailableCountries() {
        return userRepository.findDistinctCountries();
    }

    /**
     * Get nearby users in the same city (for dashboard widget)
     */
    public List<LeaderboardEntryDto> getNearbyCityLeaderboard(User currentUser, int limit) {
        if (currentUser == null || currentUser.getCity() == null || currentUser.getCity().isBlank()) {
            return List.of();
        }
        List<User> nearbyUsers = userRepository.findNearbyCityUsers(currentUser.getCity(), currentUser.getId());
        List<LeaderboardEntryDto> entries = buildEntries(nearbyUsers, currentUser);
        // Include the current user's own entry at their position
        // Limit to top N
        return entries.size() > limit ? entries.subList(0, limit) : entries;
    }

    private String getBadgeForPoints(int points) {
        if (points >= 200) return "Eco Guardian Champion 👑";
        if (points >= 100) return "Zero-Waste Warrior 🥇";
        if (points >= 50) return "Green Pioneer 🥈";
        if (points >= 20) return "Eco Apprentice 🥉";
        return "Seedling Planter 🌱";
    }
}
