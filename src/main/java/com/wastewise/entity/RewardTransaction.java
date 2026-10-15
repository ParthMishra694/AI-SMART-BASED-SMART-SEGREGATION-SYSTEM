package com.wastewise.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reward_transactions", indexes = {
    @Index(name = "idx_reward_user", columnList = "user_id"),
    @Index(name = "idx_reward_activity_ref", columnList = "activityReference")
})
public class RewardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int points;

    @Column(nullable = false)
    private String transactionType; // WASTE_ANALYSIS, QUIZ_COMPLETION, RECYCLING_VERIFIED, CHALLENGE_COMPLETED, STREAK_BONUS

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, unique = true)
    private String activityReference; // Unique idempotency key (e.g. "analysis-12", "quiz-5-attempt-1", "challenge-1-user-2")

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public RewardTransaction() {}

    public RewardTransaction(User user, int points, String transactionType, String reason, String activityReference) {
        this.user = user;
        this.points = points;
        this.transactionType = transactionType;
        this.reason = reason;
        this.activityReference = activityReference;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getActivityReference() {
        return activityReference;
    }

    public void setActivityReference(String activityReference) {
        this.activityReference = activityReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
