package com.wastewise.dto;

public class LeaderboardEntryDto {

    private int rank;
    private String username;
    private String ward;
    private String city;
    private String state;
    private String country;
    private String locationDisplay;
    private int points;
    private String badge;
    private boolean isCurrentUser;
    private boolean isDemoUser;

    public LeaderboardEntryDto() {}

    public LeaderboardEntryDto(int rank, String username, String ward, String city, String state, String country,
                               String locationDisplay, int points, String badge, boolean isCurrentUser, boolean isDemoUser) {
        this.rank = rank;
        this.username = username;
        this.ward = ward != null ? ward : "General";
        this.city = city;
        this.state = state;
        this.country = country;
        this.locationDisplay = locationDisplay;
        this.points = points;
        this.badge = badge;
        this.isCurrentUser = isCurrentUser;
        this.isDemoUser = isDemoUser;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getLocationDisplay() { return locationDisplay; }
    public void setLocationDisplay(String locationDisplay) { this.locationDisplay = locationDisplay; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }

    public boolean isCurrentUser() { return isCurrentUser; }
    public void setCurrentUser(boolean currentUser) { isCurrentUser = currentUser; }

    public boolean isDemoUser() { return isDemoUser; }
    public void setDemoUser(boolean demoUser) { isDemoUser = demoUser; }
}
