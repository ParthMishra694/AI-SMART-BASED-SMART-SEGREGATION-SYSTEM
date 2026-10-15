package com.wastewise.repository;

import com.wastewise.entity.RewardTransaction;
import com.wastewise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RewardTransactionRepository extends JpaRepository<RewardTransaction, Long> {

    List<RewardTransaction> findByUserOrderByCreatedAtDesc(User user);

    List<RewardTransaction> findTop10ByUserOrderByCreatedAtDesc(User user);

    boolean existsByActivityReference(String activityReference);

    long countByUserAndTransactionTypeAndCreatedAtAfter(User user, String transactionType, LocalDateTime after);

    @Query("SELECT COALESCE(SUM(r.points), 0) FROM RewardTransaction r WHERE r.user = :user")
    int calculateTotalPointsByUser(@Param("user") User user);

    List<RewardTransaction> findAllByOrderByCreatedAtDesc();
}
