package com.wastewise.service;

import com.wastewise.dto.RecyclingSubmissionDto;
import com.wastewise.entity.RecyclingSubmission;
import com.wastewise.entity.User;
import com.wastewise.repository.RecyclingSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RecyclingSubmissionService {

    private static final Logger log = LoggerFactory.getLogger(RecyclingSubmissionService.class);

    private final RecyclingSubmissionRepository recyclingSubmissionRepository;
    private final RewardService rewardService;
    private final ChallengeService challengeService;

    public RecyclingSubmissionService(RecyclingSubmissionRepository recyclingSubmissionRepository,
                                      RewardService rewardService,
                                      ChallengeService challengeService) {
        this.recyclingSubmissionRepository = recyclingSubmissionRepository;
        this.rewardService = rewardService;
        this.challengeService = challengeService;
    }

    @Transactional
    public RecyclingSubmission createSubmission(User user, RecyclingSubmissionDto dto, String photoPath) {
        RecyclingSubmission sub = new RecyclingSubmission();
        sub.setUser(user);
        sub.setTitle(dto.getTitle());
        sub.setMaterialType(dto.getMaterialType());
        sub.setEstimatedWeightKg(dto.getEstimatedWeightKg());
        sub.setDescription(dto.getDescription());
        sub.setPhotoPath(photoPath);
        sub.setStatus("PENDING");
        sub.setPointsAwarded(0);
        sub.setCreatedAt(LocalDateTime.now());

        log.info("Created recycling submission #{} for user '{}'", sub.getTitle(), user.getUsername());
        return recyclingSubmissionRepository.save(sub);
    }

    public List<RecyclingSubmission> getUserSubmissions(User user) {
        return recyclingSubmissionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<RecyclingSubmission> getPendingSubmissions() {
        return recyclingSubmissionRepository.findByStatusOrderByCreatedAtAsc("PENDING");
    }

    public List<RecyclingSubmission> getAllSubmissions() {
        return recyclingSubmissionRepository.findAllByOrderByCreatedAtDesc();
    }

    public long getPendingCount() {
        return recyclingSubmissionRepository.countByStatus("PENDING");
    }

    @Transactional
    public boolean reviewSubmission(Long submissionId, boolean approve, String adminNotes) {
        Optional<RecyclingSubmission> opt = recyclingSubmissionRepository.findById(submissionId);
        if (opt.isEmpty()) return false;

        RecyclingSubmission sub = opt.get();
        if (!"PENDING".equalsIgnoreCase(sub.getStatus())) {
            log.warn("Submission #{} is already in status {}", submissionId, sub.getStatus());
            return false;
        }

        sub.setAdminNotes(adminNotes);
        sub.setReviewedAt(LocalDateTime.now());

        if (approve) {
            sub.setStatus("APPROVED");
            sub.setPointsAwarded(15);
            // Award verified recycling points
            rewardService.awardRecyclingVerificationPoints(sub.getUser(), sub.getId(), sub.getTitle());
            // Progress recycling challenge
            challengeService.progressChallengesByCategory(sub.getUser(), "RECYCLING");
            log.info("Approved recycling submission #{} (+15 pts awarded to user '{}')", sub.getId(), sub.getUser().getUsername());
        } else {
            sub.setStatus("REJECTED");
            sub.setPointsAwarded(0);
            log.info("Rejected recycling submission #{} (reason: {})", sub.getId(), adminNotes);
        }

        recyclingSubmissionRepository.save(sub);
        return true;
    }
}
