package com.wastewise.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false)
    private String category; // WASTE_SCAN, QUIZ, BATTERY_EDUCATION, RECYCLING

    @Column(nullable = false)
    private int targetCount = 1;

    @Column(nullable = false)
    private int pointReward = 25;

    private LocalDate startDate;
    private LocalDate endDate;

    private String icon = "bi-trophy";
    private boolean active = true;

    public Challenge() {}

    public Challenge(String title, String description, String category, int targetCount, int pointReward, LocalDate startDate, LocalDate endDate, String icon) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.targetCount = targetCount;
        this.pointReward = pointReward;
        this.startDate = startDate;
        this.endDate = endDate;
        this.icon = icon != null ? icon : "bi-trophy";
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTargetCount() {
        return targetCount;
    }

    public void setTargetCount(int targetCount) {
        this.targetCount = targetCount;
    }

    public int getPointReward() {
        return pointReward;
    }

    public void setPointReward(int pointReward) {
        this.pointReward = pointReward;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
