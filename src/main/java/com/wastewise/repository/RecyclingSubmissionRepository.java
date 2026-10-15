package com.wastewise.repository;

import com.wastewise.entity.RecyclingSubmission;
import com.wastewise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecyclingSubmissionRepository extends JpaRepository<RecyclingSubmission, Long> {
    List<RecyclingSubmission> findByUserOrderByCreatedAtDesc(User user);
    List<RecyclingSubmission> findByStatusOrderByCreatedAtAsc(String status);
    List<RecyclingSubmission> findAllByOrderByCreatedAtDesc();
    long countByStatus(String status);
}
