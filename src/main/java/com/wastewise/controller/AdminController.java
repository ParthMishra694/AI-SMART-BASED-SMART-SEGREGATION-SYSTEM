package com.wastewise.controller;

import com.wastewise.entity.Challenge;
import com.wastewise.entity.RecyclingSubmission;
import com.wastewise.entity.RewardTransaction;
import com.wastewise.entity.User;
import com.wastewise.repository.ChallengeRepository;
import com.wastewise.repository.UserRepository;
import com.wastewise.repository.WasteAnalysisRepository;
import com.wastewise.service.RecyclingSubmissionService;
import com.wastewise.service.RewardService;
import com.wastewise.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RecyclingSubmissionService recyclingSubmissionService;
    private final RewardService rewardService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final WasteAnalysisRepository wasteAnalysisRepository;
    private final ChallengeRepository challengeRepository;

    public AdminController(RecyclingSubmissionService recyclingSubmissionService,
                           RewardService rewardService,
                           UserService userService,
                           UserRepository userRepository,
                           WasteAnalysisRepository wasteAnalysisRepository,
                           ChallengeRepository challengeRepository) {
        this.recyclingSubmissionService = recyclingSubmissionService;
        this.rewardService = rewardService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.wasteAnalysisRepository = wasteAnalysisRepository;
        this.challengeRepository = challengeRepository;
    }

    @GetMapping
    public String adminDashboard(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        List<RecyclingSubmission> pendingSubmissions = recyclingSubmissionService.getPendingSubmissions();
        List<RewardTransaction> allTransactions = rewardService.getAllTransactions();
        List<Challenge> challenges = challengeRepository.findAll();

        model.addAttribute("currentUser", user);
        model.addAttribute("pendingSubmissions", pendingSubmissions);
        model.addAttribute("allTransactions", allTransactions);
        model.addAttribute("challenges", challenges);
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalAnalyses", wasteAnalysisRepository.count());
        model.addAttribute("pendingCount", recyclingSubmissionService.getPendingCount());

        return "admin";
    }

    @PostMapping("/submissions/{id}/approve")
    public String approveSubmission(@PathVariable("id") Long id,
                                    @RequestParam(value = "notes", required = false, defaultValue = "Verified recyclable material batch.") String notes,
                                    RedirectAttributes redirectAttributes) {
        boolean success = recyclingSubmissionService.reviewSubmission(id, true, notes);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Submission #" + id + " APPROVED! 15 EcoPoints awarded to user.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not approve submission #" + id);
        }
        return "redirect:/admin";
    }

    @PostMapping("/submissions/{id}/reject")
    public String rejectSubmission(@PathVariable("id") Long id,
                                   @RequestParam("notes") String notes,
                                   RedirectAttributes redirectAttributes) {
        boolean success = recyclingSubmissionService.reviewSubmission(id, false, notes);
        if (success) {
            redirectAttributes.addFlashAttribute("successMessage", "Submission #" + id + " marked REJECTED with notes.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not reject submission #" + id);
        }
        return "redirect:/admin";
    }
}
