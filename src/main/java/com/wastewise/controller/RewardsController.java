package com.wastewise.controller;

import com.wastewise.entity.RewardTransaction;
import com.wastewise.entity.User;
import com.wastewise.service.RewardService;
import com.wastewise.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class RewardsController {

    private final RewardService rewardService;
    private final UserService userService;

    public RewardsController(RewardService rewardService, UserService userService) {
        this.rewardService = rewardService;
        this.userService = userService;
    }

    @GetMapping("/ecopoints")
    public String ecoPointsPage(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        List<RewardTransaction> transactions = rewardService.getUserTransactions(user);
        int recalculatedPoints = rewardService.getRecalculatedBalance(user);

        model.addAttribute("currentUser", user);
        model.addAttribute("transactions", transactions);
        model.addAttribute("recalculatedPoints", recalculatedPoints);
        return "ecopoints";
    }
}
