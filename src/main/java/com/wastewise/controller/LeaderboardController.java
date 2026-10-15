package com.wastewise.controller;

import com.wastewise.dto.LeaderboardEntryDto;
import com.wastewise.entity.User;
import com.wastewise.service.LeaderboardService;
import com.wastewise.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final UserService userService;

    public LeaderboardController(LeaderboardService leaderboardService, UserService userService) {
        this.leaderboardService = leaderboardService;
        this.userService = userService;
    }

    @GetMapping("/leaderboard")
    public String leaderboard(@RequestParam(value = "filterType", required = false, defaultValue = "city") String filterType,
                              @RequestParam(value = "filterValue", required = false) String filterValue,
                              Model model,
                              Authentication auth) {
        User user = userService.resolveUser(auth);

        // Default: show user's own city if no filter provided
        if ((filterValue == null || filterValue.isBlank()) && user.getCity() != null && !user.getCity().isBlank()) {
            filterValue = user.getCity();
            filterType = "city";
        }

        List<LeaderboardEntryDto> leaderboard = leaderboardService.getLeaderboard(filterType, filterValue, user);
        long userRank = leaderboardService.getUserRank(user);

        model.addAttribute("currentUser", user);
        model.addAttribute("leaderboard", leaderboard);
        model.addAttribute("userRank", userRank);
        model.addAttribute("selectedFilterType", filterType);
        model.addAttribute("selectedFilterValue", filterValue != null ? filterValue : "ALL");
        model.addAttribute("availableCities", leaderboardService.getAvailableCities());
        model.addAttribute("availableStates", leaderboardService.getAvailableStates());
        model.addAttribute("availableCountries", leaderboardService.getAvailableCountries());

        return "leaderboard";
    }
}
