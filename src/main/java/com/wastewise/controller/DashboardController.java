package com.wastewise.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wastewise.dto.DashboardStatsDto;
import com.wastewise.dto.LeaderboardEntryDto;
import com.wastewise.entity.User;
import com.wastewise.service.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final UserService userService;
    private final WasteAnalysisService wasteAnalysisService;
    private final RewardService rewardService;
    private final ChallengeService challengeService;
    private final LeaderboardService leaderboardService;
    private final RecommendationService recommendationService;
    private final RecyclingSubmissionService recyclingSubmissionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DashboardController(UserService userService,
                               WasteAnalysisService wasteAnalysisService,
                               RewardService rewardService,
                               ChallengeService challengeService,
                               LeaderboardService leaderboardService,
                               RecommendationService recommendationService,
                               RecyclingSubmissionService recyclingSubmissionService) {
        this.userService = userService;
        this.wasteAnalysisService = wasteAnalysisService;
        this.rewardService = rewardService;
        this.challengeService = challengeService;
        this.leaderboardService = leaderboardService;
        this.recommendationService = recommendationService;
        this.recyclingSubmissionService = recyclingSubmissionService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) throws Exception {
        User user = userService.resolveUser(auth);

        // Redirect to location setup if not completed
        if (!user.isLocationSetupComplete()) {
            return "redirect:/setup-location";
        }

        DashboardStatsDto stats = new DashboardStatsDto();
        stats.setTotalAnalyses(wasteAnalysisService.countUserAnalyses(user));
        stats.setTotalEcoPoints(user.getTotalEcoPoints());
        stats.setCurrentStreak(user.getStreakDays());
        stats.setRank(leaderboardService.getUserRank(user));
        stats.setCompletedChallengesCount(challengeService.getCompletedCount(user));
        stats.setPendingSubmissionsCount(recyclingSubmissionService.getUserSubmissions(user).stream()
                .filter(s -> "PENDING".equalsIgnoreCase(s.getStatus())).count());

        Map<String, Long> categoryDistribution = wasteAnalysisService.getCategoryDistribution(user);
        stats.setCategoryDistribution(categoryDistribution);

        stats.setRecentAnalyses(wasteAnalysisService.getRecentUserAnalyses(user));
        stats.setRecentTransactions(rewardService.getRecentUserTransactions(user));
        stats.setActiveChallenges(challengeService.getUserChallenges(user));
        stats.setRecommendations(recommendationService.generatePersonalizedRecommendations(user));

        // Nearby users from the same city
        List<LeaderboardEntryDto> nearbyUsers = leaderboardService.getNearbyCityLeaderboard(user, 5);

        model.addAttribute("currentUser", user);
        model.addAttribute("stats", stats);
        model.addAttribute("nearbyUsers", nearbyUsers);
        model.addAttribute("chartLabelsJson", objectMapper.writeValueAsString(categoryDistribution.keySet()));
        model.addAttribute("chartDataJson", objectMapper.writeValueAsString(categoryDistribution.values()));

        return "dashboard";
    }
}
