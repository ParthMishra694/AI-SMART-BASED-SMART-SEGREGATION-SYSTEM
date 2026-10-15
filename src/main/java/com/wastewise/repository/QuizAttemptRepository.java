package com.wastewise.repository;

import com.wastewise.entity.QuizAttempt;
import com.wastewise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndQuizTitleAndPassedTrue(User user, String quizTitle);
}
