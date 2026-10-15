package com.wastewise.controller;

import com.wastewise.entity.User;
import com.wastewise.entity.UserChallenge;
import com.wastewise.service.ChallengeService;
import com.wastewise.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ChallengeController {

    private final ChallengeService challengeService;
    private final UserService userService;

    public ChallengeController(ChallengeService challengeService, UserService userService) {
        this.challengeService = challengeService;
        this.userService = userService;
    }

    @GetMapping("/challenges")
    public String challenges(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        List<UserChallenge> userChallenges = challengeService.getUserChallenges(user);

        model.addAttribute("currentUser", user);
        model.addAttribute("userChallenges", userChallenges);
        model.addAttribute("completedCount", challengeService.getCompletedCount(user));

        return "challenges";
    }
}
