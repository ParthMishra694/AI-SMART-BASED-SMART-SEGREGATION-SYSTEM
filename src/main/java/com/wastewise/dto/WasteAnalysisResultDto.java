package com.wastewise.dto;

import java.util.List;

public class WasteAnalysisResultDto {

    private String itemName;
    private String predictedCategory;
    private String aiExplanation;
    private String disposalRecommendation;
    private String reuseRecycleTips;
    private String safetyPrecautions;
    private String confidenceLevel; // High, Medium, Low, Uncertain
    private boolean uncertain;
    private boolean clarificationNeeded;
    private List<String> alternativeCategories;
    private String agentExecutionTrace;
    private boolean demoFallbackUsed;

    public WasteAnalysisResultDto() {}

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getPredictedCategory() {
        return predictedCategory;
    }

    public void setPredictedCategory(String predictedCategory) {
        this.predictedCategory = predictedCategory;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public String getDisposalRecommendation() {
        return disposalRecommendation;
    }

    public void setDisposalRecommendation(String disposalRecommendation) {
        this.disposalRecommendation = disposalRecommendation;
    }

    public String getReuseRecycleTips() {
        return reuseRecycleTips;
    }

    public void setReuseRecycleTips(String reuseRecycleTips) {
        this.reuseRecycleTips = reuseRecycleTips;
    }

    public String getSafetyPrecautions() {
        return safetyPrecautions;
    }

    public void setSafetyPrecautions(String safetyPrecautions) {
        this.safetyPrecautions = safetyPrecautions;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public boolean isUncertain() {
        return uncertain;
    }

    public void setUncertain(boolean uncertain) {
        this.uncertain = uncertain;
    }

    public boolean isClarificationNeeded() {
        return clarificationNeeded;
    }

    public void setClarificationNeeded(boolean clarificationNeeded) {
        this.clarificationNeeded = clarificationNeeded;
    }

    public List<String> getAlternativeCategories() {
        return alternativeCategories;
    }

    public void setAlternativeCategories(List<String> alternativeCategories) {
        this.alternativeCategories = alternativeCategories;
    }

    public String getAgentExecutionTrace() {
        return agentExecutionTrace;
    }

    public void setAgentExecutionTrace(String agentExecutionTrace) {
        this.agentExecutionTrace = agentExecutionTrace;
    }

    public boolean isDemoFallbackUsed() {
        return demoFallbackUsed;
    }

    public void setDemoFallbackUsed(boolean demoFallbackUsed) {
        this.demoFallbackUsed = demoFallbackUsed;
    }
}
