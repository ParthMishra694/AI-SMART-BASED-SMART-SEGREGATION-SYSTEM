package com.wastewise.service;

import com.wastewise.entity.Challenge;
import com.wastewise.entity.User;
import com.wastewise.entity.UserChallenge;
import com.wastewise.repository.ChallengeRepository;
import com.wastewise.repository.UserChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChallengeService {

    private static final Logger log = LoggerFactory.getLogger(ChallengeService.class);

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final RewardService rewardService;

    public ChallengeService(ChallengeRepository challengeRepository,
                            UserChallengeRepository userChallengeRepository,
                            RewardService rewardService) {
        this.challengeRepository = challengeRepository;
        this.userChallengeRepository = userChallengeRepository;
        this.rewardService = rewardService;
    }

    public List<Challenge> getActiveChallenges() {
        return challengeRepository.findByActiveTrue();
    }

    public List<UserChallenge> getUserChallenges(User user) {
        ensureUserChallengesInitialized(user);
        return userChallengeRepository.findByUser(user);
    }

    @Transactional
    public void ensureUserChallengesInitialized(User user) {
        List<Challenge> activeChallenges = challengeRepository.findByActiveTrue();
        for (Challenge ch : activeChallenges) {
            Optional<UserChallenge> existing = userChallengeRepository.findByUserAndChallenge(user, ch);
            if (existing.isEmpty()) {
                UserChallenge uc = new UserChallenge(user, ch);
                userChallengeRepository.save(uc);
            }
        }
    }

    /**
     * Increments user progress on all active challenges matching the category.
     * Checks completion and awards points idempotently if target is reached.
     */
    @Transactional
    public void progressChallengesByCategory(User user, String category) {
        ensureUserChallengesInitialized(user);
        List<UserChallenge> userChallenges = userChallengeRepository.findByUserAndCompletedFalse(user);

        for (UserChallenge uc : userChallenges) {
            Challenge challenge = uc.getChallenge();
            if (challenge.isActive() && (challenge.getCategory().equalsIgnoreCase(category) || challenge.getCategory().equalsIgnoreCase("ANY_ACTION"))) {
                uc.setCurrentProgress(uc.getCurrentProgress() + 1);

                if (uc.getCurrentProgress() >= challenge.getTargetCount()) {
                    uc.setCompleted(true);
                    uc.setCompletedAt(LocalDateTime.now());
                    userChallengeRepository.save(uc);

                    // Award challenge completion points
                    rewardService.awardChallengePoints(user, challenge.getId(), challenge.getTitle());
                    log.info("User '{}' completed challenge '{}' (+{} pts)", user.getUsername(), challenge.getTitle(), challenge.getPointReward());
                } else {
                    userChallengeRepository.save(uc);
                }
            }
        }
    }

    public int getCompletedCount(User user) {
        return userChallengeRepository.findByUserAndCompletedTrue(user).size();
    }
}
