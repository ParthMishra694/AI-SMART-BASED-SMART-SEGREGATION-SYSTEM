package com.wastewise.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_analyses")
public class WasteAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String predictedCategory;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String aiExplanation;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String disposalRecommendation;

    @Column(columnDefinition = "TEXT")
    private String reuseRecycleTips;

    @Column(columnDefinition = "TEXT")
    private String safetyPrecautions;

    @Column(nullable = false)
    private String confidenceLevel = "High"; // High, Medium, Low, Uncertain

    @Column(nullable = false)
    private boolean isUncertain = false;

    private String imagePath;

    @Column(columnDefinition = "TEXT")
    private String agentExecutionTrace;

    @Column(nullable = false)
    private boolean demoFallbackUsed = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public WasteAnalysis() {}

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

    public String disposalRecommendation() {
        return disposalRecommendation;
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
        return isUncertain;
    }

    public void setUncertain(boolean uncertain) {
        isUncertain = uncertain;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
