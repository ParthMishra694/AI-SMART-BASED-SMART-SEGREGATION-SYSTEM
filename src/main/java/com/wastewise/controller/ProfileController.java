package com.wastewise.controller;

import com.wastewise.entity.User;
import com.wastewise.service.LeaderboardService;
import com.wastewise.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;
    private final LeaderboardService leaderboardService;

    public ProfileController(UserService userService, LeaderboardService leaderboardService) {
        this.userService = userService;
        this.leaderboardService = leaderboardService;
    }

    @GetMapping
    public String profilePage(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        model.addAttribute("currentUser", user);
        return "profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam("city") String city,
                                @RequestParam("state") String state,
                                @RequestParam("country") String country,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        User user = userService.resolveUser(auth);
        userService.updateProfile(user, city, state, country);
        redirectAttributes.addFlashAttribute("successMessage",
                "Location updated to: " + user.getLocationDisplay());
        return "redirect:/profile";
    }
}
