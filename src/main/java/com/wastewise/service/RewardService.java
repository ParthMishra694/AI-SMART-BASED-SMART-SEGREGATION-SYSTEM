package com.wastewise.service;

import com.wastewise.entity.RewardTransaction;
import com.wastewise.entity.User;
import com.wastewise.repository.RewardTransactionRepository;
import com.wastewise.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RewardService {

    private static final Logger log = LoggerFactory.getLogger(RewardService.class);

    private final RewardTransactionRepository rewardTransactionRepository;
    private final UserRepository userRepository;

    @Value("${wastewise.points.waste-analysis:2}")
    private int pointsWasteAnalysis;

    @Value("${wastewise.points.daily-analysis-limit:10}")
    private int dailyAnalysisPointsLimit;

    @Value("${wastewise.points.quiz-completion:5}")
    private int pointsQuizCompletion;

    @Value("${wastewise.points.verified-recycling:15}")
    private int pointsVerifiedRecycling;

    @Value("${wastewise.points.weekly-challenge:25}")
    private int pointsWeeklyChallenge;

    @Value("${wastewise.points.streak-bonus:10}")
    private int pointsStreakBonus;

    public RewardService(RewardTransactionRepository rewardTransactionRepository, UserRepository userRepository) {
        this.rewardTransactionRepository = rewardTransactionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Idempotently awards EcoPoints with strict duplicate check and audit ledger recording.
     */
    @Transactional
    public synchronized boolean awardPoints(User user, int points, String transactionType, String reason, String activityReference) {
        if (user == null || points <= 0 || activityReference == null || activityReference.isBlank()) {
            log.warn("Invalid reward request: user={}, points={}, ref={}", user != null ? user.getUsername() : "null", points, activityReference);
            return false;
        }

        // Idempotency check: prevent duplicate reward for the same activity
        if (rewardTransactionRepository.existsByActivityReference(activityReference)) {
            log.info("Activity '{}' already rewarded. Skipping duplicate points.", activityReference);
            return false;
        }

        // Save immutable ledger entry
        RewardTransaction transaction = new RewardTransaction(user, points, transactionType, reason, activityReference);
        rewardTransactionRepository.save(transaction);

        // Update user's aggregate balance transactionally
        user.setTotalEcoPoints(user.getTotalEcoPoints() + points);
        userRepository.save(user);

        log.info("Awarded {} EcoPoints to '{}' for '{}' (ref: {}). New balance: {}",
                points, user.getUsername(), reason, activityReference, user.getTotalEcoPoints());
        return true;
    }

    /**
     * Checks eligibility and awards points for waste image analysis.
     * Enforces daily cap (e.g., maximum 10 points per day from scans).
     */
    @Transactional
    public boolean awardWasteAnalysisPoints(User user, Long analysisId) {
        if (user == null || analysisId == null) return false;

        String activityRef = "waste-analysis-" + analysisId;

        // Check daily count of analysis rewards
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayAnalyses = rewardTransactionRepository.countByUserAndTransactionTypeAndCreatedAtAfter(
                user, "WASTE_ANALYSIS", startOfDay);

        int maxDailyScans = dailyAnalysisPointsLimit / Math.max(1, pointsWasteAnalysis);
        if (todayAnalyses >= maxDailyScans) {
            log.info("User '{}' reached daily analysis reward limit ({}/{}).", user.getUsername(), todayAnalyses, maxDailyScans);
            return false;
        }

        return awardPoints(user, pointsWasteAnalysis, "WASTE_ANALYSIS",
                "AI Waste Scan & Responsible Segregation", activityRef);
    }

    @Transactional
    public boolean awardQuizPoints(User user, String quizTitle, Long attemptId) {
        String activityRef = "quiz-" + quizTitle.toLowerCase().replaceAll("\\s+", "-") + "-user-" + user.getId();
        return awardPoints(user, pointsQuizCompletion, "QUIZ_COMPLETION",
                "Completed SDG 12 Quiz: " + quizTitle, activityRef);
    }

    @Transactional
    public boolean awardRecyclingVerificationPoints(User user, Long submissionId, String title) {
        String activityRef = "recycling-submission-" + submissionId;
        return awardPoints(user, pointsVerifiedRecycling, "RECYCLING_VERIFIED",
                "Admin-Verified Recycling: " + title, activityRef);
    }

    @Transactional
    public boolean awardChallengePoints(User user, Long challengeId, String challengeTitle) {
        String activityRef = "challenge-" + challengeId + "-user-" + user.getId();
        return awardPoints(user, pointsWeeklyChallenge, "CHALLENGE_COMPLETED",
                "Completed Weekly Challenge: " + challengeTitle, activityRef);
    }

    @Transactional
    public boolean awardStreakBonus(User user, int streakDays) {
        String activityRef = "streak-" + streakDays + "-user-" + user.getId() + "-date-" + LocalDate.now();
        return awardPoints(user, pointsStreakBonus, "STREAK_BONUS",
                "Earned " + streakDays + "-Day Sustainability Streak Bonus", activityRef);
    }

    public List<RewardTransaction> getUserTransactions(User user) {
        return rewardTransactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<RewardTransaction> getRecentUserTransactions(User user) {
        return rewardTransactionRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    public List<RewardTransaction> getAllTransactions() {
        return rewardTransactionRepository.findAllByOrderByCreatedAtDesc();
    }

    public int getRecalculatedBalance(User user) {
        return rewardTransactionRepository.calculateTotalPointsByUser(user);
    }
}
