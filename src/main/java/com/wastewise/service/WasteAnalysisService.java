package com.wastewise.service;

import com.wastewise.dto.WasteAnalysisResultDto;
import com.wastewise.entity.User;
import com.wastewise.entity.WasteAnalysis;
import com.wastewise.repository.WasteAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class WasteAnalysisService {

    private final WasteAnalysisRepository wasteAnalysisRepository;

    public WasteAnalysisService(WasteAnalysisRepository wasteAnalysisRepository) {
        this.wasteAnalysisRepository = wasteAnalysisRepository;
    }

    @Transactional
    public WasteAnalysis saveAnalysis(User user, WasteAnalysisResultDto dto, String imagePath) {
        WasteAnalysis analysis = new WasteAnalysis();
        analysis.setUser(user);
        analysis.setItemName(dto.getItemName());
        analysis.setPredictedCategory(dto.getPredictedCategory());
        analysis.setAiExplanation(dto.getAiExplanation());
        analysis.setDisposalRecommendation(dto.getDisposalRecommendation());
        analysis.setReuseRecycleTips(dto.getReuseRecycleTips());
        analysis.setSafetyPrecautions(dto.getSafetyPrecautions());
        analysis.setConfidenceLevel(dto.getConfidenceLevel() != null ? dto.getConfidenceLevel() : "High");
        analysis.setUncertain(dto.isUncertain());
        analysis.setImagePath(imagePath);
        analysis.setAgentExecutionTrace(dto.getAgentExecutionTrace());
        analysis.setDemoFallbackUsed(dto.isDemoFallbackUsed());

        return wasteAnalysisRepository.save(analysis);
    }

    public List<WasteAnalysis> getUserHistory(User user) {
        return wasteAnalysisRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<WasteAnalysis> getRecentUserAnalyses(User user) {
        return wasteAnalysisRepository.findTop5ByUserOrderByCreatedAtDesc(user);
    }

    public Optional<WasteAnalysis> getAnalysisById(Long id) {
        return wasteAnalysisRepository.findById(id);
    }

    public List<WasteAnalysis> filterByCategory(User user, String category) {
        if (category == null || category.equalsIgnoreCase("ALL")) {
            return getUserHistory(user);
        }
        return wasteAnalysisRepository.findByUserAndPredictedCategoryIgnoreCase(user, category);
    }

    public List<WasteAnalysis> searchHistory(User user, String query) {
        if (query == null || query.trim().isEmpty()) {
            return getUserHistory(user);
        }
        return wasteAnalysisRepository.searchByUserAndQuery(user, query.trim());
    }

    public long countUserAnalyses(User user) {
        return wasteAnalysisRepository.countByUser(user);
    }

    public Map<String, Long> getCategoryDistribution(User user) {
        List<Object[]> rows = wasteAnalysisRepository.countCategoryDistributionByUser(user);
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            distribution.put(category, count);
        }
        return distribution;
    }

    public Map<String, Long> getGlobalCategoryDistribution() {
        List<Object[]> rows = wasteAnalysisRepository.countGlobalCategoryDistribution();
        Map<String, Long> distribution = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            distribution.put(category, count);
        }
        return distribution;
    }

    /**
     * Delete an analysis record.
     * Crucial: Deleting an analysis record does NOT delete or alter earned reward ledger transactions,
     * maintaining audit integrity.
     */
    @Transactional
    public boolean deleteAnalysis(Long analysisId, User user) {
        Optional<WasteAnalysis> opt = wasteAnalysisRepository.findById(analysisId);
        if (opt.isPresent()) {
            WasteAnalysis analysis = opt.get();
            if (analysis.getUser().getId().equals(user.getId()) || "ROLE_ADMIN".equals(user.getRole())) {
                wasteAnalysisRepository.delete(analysis);
                return true;
            }
        }
        return false;
    }
}
