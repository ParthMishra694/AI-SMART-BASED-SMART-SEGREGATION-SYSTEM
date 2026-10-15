package com.wastewise.service;

import com.wastewise.entity.QuizAttempt;
import com.wastewise.entity.User;
import com.wastewise.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class QuizService {

    public record QuizQuestion(int id, String question, List<String> options, int correctOptionIndex, String explanation) {}

    private final QuizAttemptRepository quizAttemptRepository;
    private final RewardService rewardService;
    private final ChallengeService challengeService;

    public QuizService(QuizAttemptRepository quizAttemptRepository,
                       RewardService rewardService,
                       ChallengeService challengeService) {
        this.quizAttemptRepository = quizAttemptRepository;
        this.rewardService = rewardService;
        this.challengeService = challengeService;
    }

    public List<QuizQuestion> getSdg12QuizQuestions() {
        return List.of(
            new QuizQuestion(
                1,
                "Which United Nations Sustainable Development Goal (SDG) specifically focuses on 'Responsible Consumption and Production'?",
                List.of("SDG 7: Affordable Energy", "SDG 11: Sustainable Cities", "SDG 12: Responsible Consumption & Production", "SDG 13: Climate Action"),
                2,
                "SDG 12 aims to halve global per capita food waste, substantially reduce waste generation through prevention, reduction, recycling, and reuse by 2030."
            ),
            new QuizQuestion(
                2,
                "Why must household lithium-ion and alkaline batteries NEVER be thrown into ordinary household trash cans?",
                List.of("They take up too much physical space in garbage trucks", "They cause fire hazards in trucks/compactors and leach toxic heavy metals into soil and water", "They smell bad when exposed to heat", "They biodegrade too quickly"),
                1,
                "Batteries can spark or ignite under compression in garbage trucks and release toxic cadmium, lead, and lithium into groundwater when buried in open landfills."
            ),
            new QuizQuestion(
                3,
                "What is the most critical preparation step before placing a plastic food container or milk jug into the dry recyclables bin?",
                List.of("Cut it into small pieces with scissors", "Empty all food residue and rinse it clean with water", "Paint it blue", "Freeze it for 24 hours"),
                1,
                "Food grease and liquid remnants contaminate entire batches of dry recyclable paper and plastic, turning recyclable materials into landfill trash."
            ),
            new QuizQuestion(
                4,
                "What type of household waste is ideal for turning into organic compost for soil enrichment?",
                List.of("Styrofoam peanuts and plastic bags", "Used alkaline batteries and electronics", "Fruit rinds, vegetable peels, coffee grounds, and dry leaves", "Treated lumber and paint cans"),
                2,
                "Organic food scraps and plant trimmings are biodegradable and break down with moisture and oxygen into rich nutrient-dense compost."
            ),
            new QuizQuestion(
                5,
                "In standard municipal two-bin systems, which color bin is traditionally designated for wet organic waste?",
                List.of("Blue Bin", "Green Bin", "Red Bin", "Black Bin"),
                1,
                "Green bins are universally designated for biodegradable wet waste and kitchen scraps, while Blue bins are designated for dry recyclables."
            )
        );
    }

    @Transactional
    public QuizAttempt evaluateQuiz(User user, Map<Integer, Integer> userAnswers) {
        List<QuizQuestion> questions = getSdg12QuizQuestions();
        int score = 0;

        for (QuizQuestion q : questions) {
            Integer chosen = userAnswers.get(q.id());
            if (chosen != null && chosen == q.correctOptionIndex()) {
                score++;
            }
        }

        boolean passed = (score >= 4); // 80% passing grade
        int pointsAwarded = 0;

        if (passed) {
            boolean alreadyPassed = quizAttemptRepository.existsByUserAndQuizTitleAndPassedTrue(user, "UN SDG 12 & Waste Segregation Master");
            if (!alreadyPassed) {
                pointsAwarded = 5;
                rewardService.awardQuizPoints(user, "UN SDG 12 & Waste Segregation Master", 1L);
            }
            // Advance weekly challenge if active
            challengeService.progressChallengesByCategory(user, "QUIZ");
        }

        QuizAttempt attempt = new QuizAttempt(
                user,
                "UN SDG 12 & Waste Segregation Master",
                score,
                questions.size(),
                passed,
                pointsAwarded
        );
        return quizAttemptRepository.save(attempt);
    }
}
