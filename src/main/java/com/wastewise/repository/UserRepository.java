package com.wastewise.repository;

import com.wastewise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    List<User> findAllByOrderByTotalEcoPointsDesc();

    List<User> findByWardOrderByTotalEcoPointsDesc(String ward);

    @Query("SELECT DISTINCT u.ward FROM User u WHERE u.ward IS NOT NULL AND u.ward <> '' ORDER BY u.ward ASC")
    List<String> findDistinctWards();

    @Query("SELECT COUNT(u) FROM User u WHERE u.totalEcoPoints > :points")
    long countUsersWithMorePoints(int points);

    // Location-based queries
    List<User> findByCityIgnoreCaseOrderByTotalEcoPointsDesc(String city);
    List<User> findByStateIgnoreCaseOrderByTotalEcoPointsDesc(String state);
    List<User> findByCountryIgnoreCaseOrderByTotalEcoPointsDesc(String country);

    @Query("SELECT DISTINCT u.city FROM User u WHERE u.city IS NOT NULL AND u.city <> '' ORDER BY u.city ASC")
    List<String> findDistinctCities();

    @Query("SELECT DISTINCT u.state FROM User u WHERE u.state IS NOT NULL AND u.state <> '' ORDER BY u.state ASC")
    List<String> findDistinctStates();

    @Query("SELECT DISTINCT u.country FROM User u WHERE u.country IS NOT NULL AND u.country <> '' ORDER BY u.country ASC")
    List<String> findDistinctCountries();

    // Nearby users in same city (for dashboard)
    @Query("SELECT u FROM User u WHERE LOWER(u.city) = LOWER(:city) AND u.id <> :userId ORDER BY u.totalEcoPoints DESC")
    List<User> findNearbyCityUsers(String city, Long userId);
}
