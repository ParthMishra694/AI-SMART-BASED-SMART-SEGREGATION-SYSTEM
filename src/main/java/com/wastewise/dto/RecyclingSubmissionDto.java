package com.wastewise.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class RecyclingSubmissionDto {

    @NotBlank(message = "Activity title is required")
    private String title;

    @NotBlank(message = "Material type is required")
    private String materialType; // Paper, Plastic, E-waste, Metal, Glass, Batteries

    @Positive(message = "Estimated weight must be greater than 0")
    private Double estimatedWeightKg;

    @NotBlank(message = "Please describe your recycling activity")
    private String description;

    public RecyclingSubmissionDto() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public Double getEstimatedWeightKg() {
        return estimatedWeightKg;
    }

    public void setEstimatedWeightKg(Double estimatedWeightKg) {
        this.estimatedWeightKg = estimatedWeightKg;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
