package com.wastewise.service;

import com.wastewise.entity.User;
import com.wastewise.entity.WasteAnalysis;
import com.wastewise.repository.WasteAnalysisRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecommendationService {

    private final WasteAnalysisRepository wasteAnalysisRepository;

    public RecommendationService(WasteAnalysisRepository wasteAnalysisRepository) {
        this.wasteAnalysisRepository = wasteAnalysisRepository;
    }

    public List<String> generatePersonalizedRecommendations(User user) {
        List<String> recommendations = new ArrayList<>();
        if (user == null) {
            return getDefaultRecommendations();
        }

        List<WasteAnalysis> history = wasteAnalysisRepository.findByUserOrderByCreatedAtDesc(user);
        if (history.isEmpty()) {
            recommendations.add("Welcome to WasteWise AI! Scan your first household waste item (like a food peel, plastic bottle, or delivery box) to receive tailored segregation recommendations.");
            recommendations.add("UN SDG 12 Insight: Over 30% of household municipal waste is biodegradable organic matter that can be diverted to compost rather than landfills.");
            recommendations.add("Check out this week's Segregation Quiz in the Challenges tab to earn your first 5 EcoPoints.");
            return recommendations;
        }

        // Count categories in user's saved records
        Map<String, Integer> categoryCounts = new HashMap<>();
        for (WasteAnalysis w : history) {
            String cat = w.getPredictedCategory();
            categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
        }

        // Generate tailored tips based on actual records
        if (categoryCounts.getOrDefault("Recyclable plastic", 0) >= 2) {
            recommendations.add(String.format("You have analyzed %d plastic items. Remember to always rinse out milk or beverage residues and compress plastic containers before binning to prevent contamination of dry recyclables.",
                    categoryCounts.get("Recyclable plastic")));
        }

        if (categoryCounts.getOrDefault("Organic / wet waste", 0) >= 2) {
            recommendations.add(String.format("You have recorded %d organic waste items. If you have outdoor space, consider a small kitchen tumbler composter to transform food peels into rich garden humus.",
                    categoryCounts.get("Organic / wet waste")));
        }

        if (categoryCounts.getOrDefault("Paper and cardboard", 0) >= 1) {
            recommendations.add("Keep corrugated delivery cartons dry and flatten them before disposal. Unsoiled cardboard maintains high recycling fiber value.");
        }

        if (categoryCounts.getOrDefault("Batteries", 0) >= 1 || categoryCounts.getOrDefault("Electronic waste", 0) >= 1) {
            recommendations.add("You previously analyzed batteries or electronics. Never mix these with regular curbside trash; keep a dedicated sealed container and deposit them at your local authorized e-waste drop-off.");
        }

        if (categoryCounts.getOrDefault("Glass", 0) >= 1) {
            recommendations.add("Glass bottles can be recycled indefinitely without degradation of purity or quality. Remove caps and metal rings where possible.");
        }

        // General reinforcement
        if (recommendations.size() < 3) {
            recommendations.add("Consistency matters: Segregating at source prevents up to 70% of recyclable material from being ruined by wet kitchen waste.");
        }

        return recommendations;
    }

    private List<String> getDefaultRecommendations() {
        return List.of(
            "Start by identifying common items like beverage containers and kitchen peels.",
            "Keep dry and wet waste segregated from the point of generation.",
            "Inspect packaging for recycling symbols (#1 PET and #2 HDPE are most widely accepted)."
        );
    }
}
