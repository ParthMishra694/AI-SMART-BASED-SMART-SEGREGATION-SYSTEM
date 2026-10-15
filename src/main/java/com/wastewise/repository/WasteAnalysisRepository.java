package com.wastewise.repository;

import com.wastewise.entity.User;
import com.wastewise.entity.WasteAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WasteAnalysisRepository extends JpaRepository<WasteAnalysis, Long> {

    List<WasteAnalysis> findByUserOrderByCreatedAtDesc(User user);

    List<WasteAnalysis> findTop5ByUserOrderByCreatedAtDesc(User user);

    long countByUser(User user);

    long countByUserAndCreatedAtAfter(User user, LocalDateTime after);

    List<WasteAnalysis> findByUserAndPredictedCategoryIgnoreCase(User user, String category);

    @Query("SELECT w FROM WasteAnalysis w WHERE w.user = :user AND " +
           "(LOWER(w.itemName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(w.predictedCategory) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           " LOWER(w.aiExplanation) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY w.createdAt DESC")
    List<WasteAnalysis> searchByUserAndQuery(@Param("user") User user, @Param("query") String query);

    @Query("SELECT w.predictedCategory, COUNT(w) FROM WasteAnalysis w WHERE w.user = :user GROUP BY w.predictedCategory")
    List<Object[]> countCategoryDistributionByUser(@Param("user") User user);

    @Query("SELECT w.predictedCategory, COUNT(w) FROM WasteAnalysis w GROUP BY w.predictedCategory")
    List<Object[]> countGlobalCategoryDistribution();
}
