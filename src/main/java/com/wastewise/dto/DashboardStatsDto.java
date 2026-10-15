package com.wastewise.dto;

import com.wastewise.entity.RewardTransaction;
import com.wastewise.entity.UserChallenge;
import com.wastewise.entity.WasteAnalysis;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {

    private long totalAnalyses;
    private int totalEcoPoints;
    private int currentStreak;
    private long rank;
    private long totalUsers;
    private int completedChallengesCount;
    private long pendingSubmissionsCount;
    private Map<String, Long> categoryDistribution;
    private List<WasteAnalysis> recentAnalyses;
    private List<RewardTransaction> recentTransactions;
    private List<UserChallenge> activeChallenges;
    private List<String> recommendations;

    public DashboardStatsDto() {}

    public long getTotalAnalyses() {
        return totalAnalyses;
    }

    public void setTotalAnalyses(long totalAnalyses) {
        this.totalAnalyses = totalAnalyses;
    }

    public int getTotalEcoPoints() {
        return totalEcoPoints;
    }

    public void setTotalEcoPoints(int totalEcoPoints) {
        this.totalEcoPoints = totalEcoPoints;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public long getRank() {
        return rank;
    }

    public void setRank(long rank) {
        this.rank = rank;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getCompletedChallengesCount() {
        return completedChallengesCount;
    }

    public void setCompletedChallengesCount(int completedChallengesCount) {
        this.completedChallengesCount = completedChallengesCount;
    }

    public long getPendingSubmissionsCount() {
        return pendingSubmissionsCount;
    }

    public void setPendingSubmissionsCount(long pendingSubmissionsCount) {
        this.pendingSubmissionsCount = pendingSubmissionsCount;
    }

    public Map<String, Long> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<String, Long> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public List<WasteAnalysis> getRecentAnalyses() {
        return recentAnalyses;
    }

    public void setRecentAnalyses(List<WasteAnalysis> recentAnalyses) {
        this.recentAnalyses = recentAnalyses;
    }

    public List<RewardTransaction> getRecentTransactions() {
        return recentTransactions;
    }

    public void setRecentTransactions(List<RewardTransaction> recentTransactions) {
        this.recentTransactions = recentTransactions;
    }

    public List<UserChallenge> getActiveChallenges() {
        return activeChallenges;
    }

    public void setActiveChallenges(List<UserChallenge> activeChallenges) {
        this.activeChallenges = activeChallenges;
    }

    public List<String> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<String> recommendations) {
        this.recommendations = recommendations;
    }
}
