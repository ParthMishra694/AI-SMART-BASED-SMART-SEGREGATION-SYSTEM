package com.wastewise.repository;

import com.wastewise.entity.Challenge;
import com.wastewise.entity.User;
import com.wastewise.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    List<UserChallenge> findByUser(User user);
    Optional<UserChallenge> findByUserAndChallenge(User user, Challenge challenge);
    List<UserChallenge> findByUserAndCompletedTrue(User user);
    List<UserChallenge> findByUserAndCompletedFalse(User user);
}
