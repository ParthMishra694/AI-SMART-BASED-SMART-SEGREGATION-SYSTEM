package com.wastewise.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewise.dto.WasteAnalysisResultDto;
import com.wastewise.entity.User;
import com.wastewise.entity.WasteAnalysis;
import com.wastewise.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class WasteAgentService {

    private static final Logger log = LoggerFactory.getLogger(WasteAgentService.class);

    private final GeminiVisionClient visionClient;
    private final StorageService storageService;
    private final WasteAnalysisService wasteAnalysisService;
    private final RewardService rewardService;
    private final ChallengeService challengeService;
    private final UserService userService;
    private final RecommendationService recommendationService;

    public WasteAgentService(GeminiVisionClient visionClient,
                             StorageService storageService,
                             WasteAnalysisService wasteAnalysisService,
                             RewardService rewardService,
                             ChallengeService challengeService,
                             UserService userService,
                             RecommendationService recommendationService) {
        this.visionClient = visionClient;
        this.storageService = storageService;
        this.wasteAnalysisService = wasteAnalysisService;
        this.rewardService = rewardService;
        this.challengeService = challengeService;
        this.userService = userService;
        this.recommendationService = recommendationService;
    }

    /**
     * Complete Agentic Execution Workflow:
     * Receives image, coordinates tool invocations, maintains execution trace,
     * persists classification, calculates reward eligibility deterministically,
     * and advances user challenges.
     */
    public WasteAnalysis processWasteScan(User user, MultipartFile file) throws Exception {
        List<Map<String, Object>> traceSteps = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        // Tool 1: Input Validation & Image Storage
        log.info("Agent Tool [1/6]: Validating and storing image...");
        String imagePath = storageService.storeFile(file);
        traceSteps.add(Map.of(
            "step", 1,
            "tool", "StorageService.storeFile",
            "status", "SUCCESS",
            "details", "Validated MIME type (" + file.getContentType() + ") and file size (" + (file.getSize() / 1024) + " KB). Stored at " + imagePath
        ));

        // Tool 2: Vision Analysis Tool (Gemini API or Fallback)
        log.info("Agent Tool [2/6]: Calling analyzeWasteImage...");
        WasteAnalysisResultDto analysisResult = visionClient.analyzeImage(file.getBytes(), file.getContentType(), file.getOriginalFilename());
        traceSteps.add(Map.of(
            "step", 2,
            "tool", "GeminiVisionClient.analyzeWasteImage",
            "status", "SUCCESS",
            "mode", analysisResult.isDemoFallbackUsed() ? "DEMO_FALLBACK" : "LIVE_GEMINI_VISION",
            "details", "Identified item '" + analysisResult.getItemName() + "' with category '" + analysisResult.getPredictedCategory() + "' (Confidence: " + analysisResult.getConfidenceLevel() + ")"
        ));

        // Tool 3: Ambiguity and Uncertainty Verification
        if (analysisResult.isUncertain() || "Unknown / needs clarification".equalsIgnoreCase(analysisResult.getPredictedCategory())) {
            traceSteps.add(Map.of(
                "step", 3,
                "tool", "UncertaintyEvaluator",
                "status", "FLAGGED",
                "details", "High uncertainty detected in visual features. Flagged for user clarification."
            ));
        } else {
            traceSteps.add(Map.of(
                "step", 3,
                "tool", "UncertaintyEvaluator",
                "status", "PASSED",
                "details", "Confidence threshold satisfied. Proceeding to municipal segregation assignment."
            ));
        }

        // Attach structured JSON execution trace to DTO
        ObjectMapper mapper = new ObjectMapper();
        analysisResult.setAgentExecutionTrace(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(traceSteps));

        // Tool 4: Persistence Service
        log.info("Agent Tool [4/6]: Saving WasteAnalysis record...");
        WasteAnalysis savedRecord = wasteAnalysisService.saveAnalysis(user, analysisResult, imagePath);
        traceSteps.add(Map.of(
            "step", 4,
            "tool", "WasteAnalysisService.saveAnalysis",
            "status", "SUCCESS",
            "recordId", savedRecord.getId(),
            "details", "Persisted analysis to database."
        ));

        // Tool 5: Deterministic Reward Rule Validator
        log.info("Agent Tool [5/6]: Validating activity reward eligibility...");
        boolean pointsAwarded = rewardService.awardWasteAnalysisPoints(user, savedRecord.getId());
        traceSteps.add(Map.of(
            "step", 5,
            "tool", "RewardService.awardWasteAnalysisPoints",
            "status", pointsAwarded ? "POINTS_AWARDED" : "DAILY_LIMIT_OR_DUPLICATE",
            "details", pointsAwarded ? "Awarded 2 EcoPoints to ledger." : "Daily limit reached or duplicate detected."
        ));

        // Tool 6: Challenge Progress Evaluator & Streak Maintenance
        log.info("Agent Tool [6/6]: Advancing active challenges and updating streak...");
        challengeService.progressChallengesByCategory(user, "WASTE_SCAN");
        userService.updateUserActivityAndStreak(user);
        traceSteps.add(Map.of(
            "step", 6,
            "tool", "ChallengeService.progressChallengesByCategory",
            "status", "SUCCESS",
            "details", "Updated challenge progress for category WASTE_SCAN. Refreshed user activity timestamp."
        ));

        // Update the execution trace on the saved record
        savedRecord.setAgentExecutionTrace(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(traceSteps));
        wasteAnalysisService.saveAnalysis(user, analysisResult, imagePath); // persist final trace

        long totalDuration = System.currentTimeMillis() - startTime;
        log.info("Agent execution completed in {} ms. Saved analysis ID: {}", totalDuration, savedRecord.getId());

        return savedRecord;
    }
}
