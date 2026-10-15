package com.wastewise.config;

import com.wastewise.dto.WasteAnalysisResultDto;
import com.wastewise.entity.Challenge;
import com.wastewise.entity.RecyclingSubmission;
import com.wastewise.entity.User;
import com.wastewise.entity.WasteAnalysis;
import com.wastewise.repository.ChallengeRepository;
import com.wastewise.repository.RecyclingSubmissionRepository;
import com.wastewise.repository.UserRepository;
import com.wastewise.repository.WasteAnalysisRepository;
import com.wastewise.service.RewardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final WasteAnalysisRepository wasteAnalysisRepository;
    private final RecyclingSubmissionRepository recyclingSubmissionRepository;
    private final RewardService rewardService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ChallengeRepository challengeRepository,
                           WasteAnalysisRepository wasteAnalysisRepository,
                           RecyclingSubmissionRepository recyclingSubmissionRepository,
                           RewardService rewardService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.wasteAnalysisRepository = wasteAnalysisRepository;
        this.recyclingSubmissionRepository = recyclingSubmissionRepository;
        this.rewardService = rewardService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already initialized. Skipping seeding.");
            return;
        }

        log.info("Seeding initial data with clearly labeled demo accounts and challenges...");

        // Helper to set location on a user
        // 1. Seed Admin User
        User admin = new User("admin", "admin@wastewise.org", passwordEncoder.encode("admin123"), "Bhopal, Madhya Pradesh, India", "ROLE_ADMIN");
        admin.setTotalEcoPoints(250);
        admin.setStreakDays(14);
        admin.setEmailVerified(true);
        admin.setCity("Bhopal");
        admin.setState("Madhya Pradesh");
        admin.setCountry("India");
        admin.setLocationSetupComplete(true);
        admin.setLastActiveDate(LocalDate.now());
        userRepository.save(admin);

        // 2. Seed Primary Demo User
        User demoUser = new User("demo_user", "user@wastewise.org", passwordEncoder.encode("demo123"), "Bhopal, Madhya Pradesh, India", "ROLE_USER");
        demoUser.setTotalEcoPoints(42);
        demoUser.setStreakDays(3);
        demoUser.setEmailVerified(true);
        demoUser.setCity("Bhopal");
        demoUser.setState("Madhya Pradesh");
        demoUser.setCountry("India");
        demoUser.setLocationSetupComplete(true);
        demoUser.setLastActiveDate(LocalDate.now());
        userRepository.save(demoUser);

        // 3. Seed Clearly Labeled Sample Community Leaderboard Users
        User sample1 = new User("demo_sarah", "demo_sarah@sample.org", passwordEncoder.encode("pass123"), "Bhopal, Madhya Pradesh, India", "ROLE_USER");
        sample1.setTotalEcoPoints(145);
        sample1.setStreakDays(12);
        sample1.setEmailVerified(true);
        sample1.setCity("Bhopal");
        sample1.setState("Madhya Pradesh");
        sample1.setCountry("India");
        sample1.setLocationSetupComplete(true);
        sample1.setLastActiveDate(LocalDate.now());
        userRepository.save(sample1);

        User sample2 = new User("demo_arjun", "demo_arjun@sample.org", passwordEncoder.encode("pass123"), "Indore, Madhya Pradesh, India", "ROLE_USER");
        sample2.setTotalEcoPoints(110);
        sample2.setStreakDays(8);
        sample2.setEmailVerified(true);
        sample2.setCity("Indore");
        sample2.setState("Madhya Pradesh");
        sample2.setCountry("India");
        sample2.setLocationSetupComplete(true);
        sample2.setLastActiveDate(LocalDate.now());
        userRepository.save(sample2);

        User sample3 = new User("demo_priya", "demo_priya@sample.org", passwordEncoder.encode("pass123"), "Bhopal, Madhya Pradesh, India", "ROLE_USER");
        sample3.setTotalEcoPoints(85);
        sample3.setStreakDays(5);
        sample3.setEmailVerified(true);
        sample3.setCity("Bhopal");
        sample3.setState("Madhya Pradesh");
        sample3.setCountry("India");
        sample3.setLocationSetupComplete(true);
        sample3.setLastActiveDate(LocalDate.now());
        userRepository.save(sample3);

        User sample4 = new User("demo_vikram", "demo_vikram@sample.org", passwordEncoder.encode("pass123"), "Jabalpur, Madhya Pradesh, India", "ROLE_USER");
        sample4.setTotalEcoPoints(40);
        sample4.setStreakDays(2);
        sample4.setEmailVerified(true);
        sample4.setCity("Jabalpur");
        sample4.setState("Madhya Pradesh");
        sample4.setCountry("India");
        sample4.setLocationSetupComplete(true);
        sample4.setLastActiveDate(LocalDate.now());
        userRepository.save(sample4);

        // 4. Seed Active Weekly Challenges
        LocalDate now = LocalDate.now();
        challengeRepository.saveAll(List.of(
            new Challenge(
                "5-Item Segregation Sprint",
                "Scan and classify 5 distinct household waste items to build source segregation discipline.",
                "WASTE_SCAN",
                5,
                25,
                now.minusDays(2),
                now.plusDays(5),
                "bi-camera"
            ),
            new Challenge(
                "SDG 12 Knowledge Master",
                "Complete the UN SDG 12 Responsible Consumption and Waste Segregation Quiz with 80%+ score.",
                "QUIZ",
                1,
                15,
                now.minusDays(1),
                now.plusDays(6),
                "bi-patch-question"
            ),
            new Challenge(
                "Verified Recyclable Batch Drop-off",
                "Submit a photo and details of segregated dry recyclables or paper drop-off for admin verification.",
                "RECYCLING",
                1,
                30,
                now,
                now.plusDays(7),
                "bi-recycle"
            )
        ));

        // 5. Seed Initial Sample Analyses for demo_user so Chart.js has realistic data
        createSampleAnalysis(demoUser, "Transparent Beverage Bottle [DEMO]", "Recyclable plastic",
                "Clear polyethylene terephthalate container. Recyclable once rinsed clean.",
                "Rinse, compress, and deposit in Blue Dry Recyclables bin.",
                "Can be re-spun into recycled polyester fabric.",
                "Ensure cap is separated if municipal facility requests.", "High");

        createSampleAnalysis(demoUser, "Corrugated Cardboard Box [DEMO]", "Paper and cardboard",
                "Clean packaging box without food stains.",
                "Flatten completely and place in dry paper collection.",
                "High fiber re-pulping value.",
                "Keep sheltered from rain to prevent rotting.", "High");

        createSampleAnalysis(demoUser, "Fruit and Vegetable Kitchen Peels [DEMO]", "Organic / wet waste",
                "Biodegradable kitchen vegetable and fruit trimmings.",
                "Deposit in Green Wet Waste bin or kitchen composter.",
                "Transforms into nutrient-rich garden compost.",
                "Segregate immediately to avoid fruit flies.", "High");

        createSampleAnalysis(demoUser, "Alkaline AA Batteries [DEMO]", "Batteries",
                "Spent dry-cell cylindrical batteries containing hazardous metals.",
                "Do NOT dispose in general trash. Bring to battery collection bin.",
                "Switch to rechargeable NiMH cells.",
                "CAUTION: Chemical leakage hazard. Keep terminals insulated.", "High");

        // Seed initial reward transactions for demoUser
        rewardService.awardPoints(demoUser, 2, "WASTE_ANALYSIS", "AI Waste Scan: Plastic Bottle", "demo-analysis-1");
        rewardService.awardPoints(demoUser, 2, "WASTE_ANALYSIS", "AI Waste Scan: Cardboard Box", "demo-analysis-2");
        rewardService.awardPoints(demoUser, 2, "WASTE_ANALYSIS", "AI Waste Scan: Kitchen Peels", "demo-analysis-3");
        rewardService.awardPoints(demoUser, 5, "QUIZ_COMPLETION", "Completed SDG 12 Quiz", "demo-quiz-1");

        // 6. Seed a pending recycling submission for admin evaluation demo
        RecyclingSubmission demoSub = new RecyclingSubmission();
        demoSub.setUser(demoUser);
        demoSub.setTitle("5 kg Segregated Cardboard & Newspapers [DEMO]");
        demoSub.setMaterialType("Paper and cardboard");
        demoSub.setEstimatedWeightKg(5.2);
        demoSub.setDescription("Bundled clean old newspapers and flat cardboard boxes ready for municipal paper collection.");
        demoSub.setStatus("PENDING");
        demoSub.setCreatedAt(LocalDateTime.now().minusHours(4));
        recyclingSubmissionRepository.save(demoSub);

        log.info("Database seeding complete. Demo accounts ready.");
    }

    private void createSampleAnalysis(User user, String name, String category, String explanation,
                                      String recommendation, String reuseTips, String safety, String confidence) {
        WasteAnalysis a = new WasteAnalysis();
        a.setUser(user);
        a.setItemName(name);
        a.setPredictedCategory(category);
        a.setAiExplanation(explanation);
        a.setDisposalRecommendation(recommendation);
        a.setReuseRecycleTips(reuseTips);
        a.setSafetyPrecautions(safety);
        a.setConfidenceLevel(confidence);
        a.setUncertain(false);
        a.setDemoFallbackUsed(true);
        a.setAgentExecutionTrace("[\n  {\n    \"step\": 1,\n    \"tool\": \"StorageService.storeFile\",\n    \"status\": \"SUCCESS\"\n  },\n  {\n    \"step\": 2,\n    \"tool\": \"GeminiVisionClient.analyzeWasteImage\",\n    \"status\": \"SUCCESS (DEMO_MODE)\"\n  }\n]");
        wasteAnalysisRepository.save(a);
    }
}
