package com.wastewise.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String ward; // kept for backward compat, now auto-generated from city

    // Real location fields
    private String city;
    private String state;
    private String country;

    @Column(nullable = false)
    private boolean locationSetupComplete = false;

    @Column(nullable = false)
    private int totalEcoPoints = 0;

    @Column(nullable = false)
    private int streakDays = 0;

    private LocalDate lastActiveDate;

    @Column(nullable = false)
    private String role = "ROLE_USER"; // ROLE_USER or ROLE_ADMIN

    @Column(nullable = false)
    private boolean emailVerified = false;

    private String verificationToken;

    private LocalDateTime verificationTokenExpiry;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {}

    public User(String username, String email, String password, String ward, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.ward = ward;
        this.role = role != null ? role : "ROLE_USER";
        this.totalEcoPoints = 0;
        this.streakDays = 0;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Returns a display-friendly location string like "Bhopal, Madhya Pradesh, India"
     */
    public String getLocationDisplay() {
        StringBuilder sb = new StringBuilder();
        if (city != null && !city.isBlank()) sb.append(city);
        if (state != null && !state.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state);
        }
        if (country != null && !country.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.length() > 0 ? sb.toString() : (ward != null ? ward : "Location not set");
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isLocationSetupComplete() { return locationSetupComplete; }
    public void setLocationSetupComplete(boolean locationSetupComplete) { this.locationSetupComplete = locationSetupComplete; }

    public int getTotalEcoPoints() { return totalEcoPoints; }
    public void setTotalEcoPoints(int totalEcoPoints) { this.totalEcoPoints = totalEcoPoints; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public LocalDate getLastActiveDate() { return lastActiveDate; }
    public void setLastActiveDate(LocalDate lastActiveDate) { this.lastActiveDate = lastActiveDate; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }

    public LocalDateTime getVerificationTokenExpiry() { return verificationTokenExpiry; }
    public void setVerificationTokenExpiry(LocalDateTime verificationTokenExpiry) { this.verificationTokenExpiry = verificationTokenExpiry; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
