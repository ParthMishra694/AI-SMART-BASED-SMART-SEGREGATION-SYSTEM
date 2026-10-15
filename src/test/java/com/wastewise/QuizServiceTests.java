package com.wastewise;

import com.wastewise.entity.QuizAttempt;
import com.wastewise.entity.User;
import com.wastewise.repository.UserRepository;
import com.wastewise.service.QuizService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QuizServiceTests {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("quiz_tester_" + System.currentTimeMillis(), "quiz@test.org", "pass", "Ward 12", "ROLE_USER");
        testUser.setTotalEcoPoints(0);
        userRepository.save(testUser);
    }

    @Test
    void testQuizPassingAwardsEcoPoints() {
        // Correct answers: Q1->2 (SDG 12), Q2->1 (Fire/heavy metal), Q3->1 (Clean/dry), Q4->2 (Organic scrap), Q5->1 (Green bin)
        Map<Integer, Integer> answers = Map.of(
            1, 2,
            2, 1,
            3, 1,
            4, 2,
            5, 1
        );

        QuizAttempt attempt = quizService.evaluateQuiz(testUser, answers);
        assertEquals(5, attempt.getScore());
        assertTrue(attempt.isPassed());
        assertEquals(5, attempt.getPointsAwarded());
        // 5 pts for quiz + 25 pts for completing the active weekly challenge "SDG 12 Knowledge Master" = 30 total pts
        assertEquals(30, testUser.getTotalEcoPoints());
    }

    @Test
    void testQuizFailureAwardsZeroPoints() {
        // Incorrect answers
        Map<Integer, Integer> answers = Map.of(
            1, 0,
            2, 0,
            3, 0,
            4, 0,
            5, 0
        );

        QuizAttempt attempt = quizService.evaluateQuiz(testUser, answers);
        assertEquals(0, attempt.getScore());
        assertFalse(attempt.isPassed());
        assertEquals(0, attempt.getPointsAwarded());
        assertEquals(0, testUser.getTotalEcoPoints());
    }
}
