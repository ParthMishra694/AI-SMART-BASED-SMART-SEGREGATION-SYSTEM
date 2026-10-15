package com.wastewise;

import com.wastewise.entity.User;
import com.wastewise.repository.RewardTransactionRepository;
import com.wastewise.repository.UserRepository;
import com.wastewise.service.RewardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RewardServiceTests {

    @Autowired
    private RewardService rewardService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RewardTransactionRepository rewardTransactionRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test_citizen_" + System.currentTimeMillis(), "test@waste.org", "pass", "Ward 12", "ROLE_USER");
        testUser.setTotalEcoPoints(0);
        userRepository.save(testUser);
    }

    @Test
    void testAwardPointsSuccess() {
        boolean awarded = rewardService.awardPoints(testUser, 10, "TEST", "Testing point ledger", "test-ref-1");
        assertTrue(awarded);
        assertEquals(10, testUser.getTotalEcoPoints());
        assertTrue(rewardTransactionRepository.existsByActivityReference("test-ref-1"));
    }

    @Test
    void testDuplicateRewardPreventionIdempotency() {
        boolean first = rewardService.awardPoints(testUser, 10, "TEST", "First try", "idempotency-key-100");
        assertTrue(first);
        assertEquals(10, testUser.getTotalEcoPoints());

        // Attempt duplicate award with same activityReference
        boolean duplicate = rewardService.awardPoints(testUser, 10, "TEST", "Duplicate try", "idempotency-key-100");
        assertFalse(duplicate, "Duplicate activityReference must be rejected to prevent cheating");
        assertEquals(10, testUser.getTotalEcoPoints(), "Balance must remain unchanged on duplicate award attempt");
    }

    @Test
    void testWasteAnalysisDailyCap() {
        // Daily limit is 10 points (5 scans of 2 points each)
        for (int i = 1; i <= 5; i++) {
            boolean awarded = rewardService.awardWasteAnalysisPoints(testUser, (long) (1000 + i));
            assertTrue(awarded, "Scan #" + i + " should be rewarded within daily cap");
        }

        // 6th scan should exceed daily limit and be rejected
        boolean sixthScan = rewardService.awardWasteAnalysisPoints(testUser, 1006L);
        assertFalse(sixthScan, "Scan exceeding daily cap must not award points");
    }
}
