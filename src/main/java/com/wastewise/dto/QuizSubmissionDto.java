package com.wastewise.dto;

import java.util.List;
import java.util.Map;

public class QuizSubmissionDto {

    private String quizTitle;
    private Map<Integer, Integer> answers; // questionIndex -> selectedOptionIndex

    public QuizSubmissionDto() {}

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public Map<Integer, Integer> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Integer, Integer> answers) {
        this.answers = answers;
    }
}
