package com.wastewise.controller;

import com.wastewise.entity.User;
import com.wastewise.service.UserService;
import com.wastewise.service.WasteAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    private final UserService userService;
    private final WasteAnalysisService wasteAnalysisService;

    public HomeController(UserService userService, WasteAnalysisService wasteAnalysisService) {
        this.userService = userService;
        this.wasteAnalysisService = wasteAnalysisService;
    }

    @GetMapping("/")
    public String index(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            model.addAttribute("username", auth.getName());
            Optional<User> userOpt = userService.findByUsername(auth.getName())
                    .or(() -> userService.findByEmail(auth.getName()));
            userOpt.ifPresent(u -> {
                model.addAttribute("currentUser", u);
                model.addAttribute("username", u.getUsername());
            });
        }
        model.addAttribute("globalDistribution", wasteAnalysisService.getGlobalCategoryDistribution());
        return "index";
    }

    @GetMapping("/rewards")
    public String greenRewards(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userService.findByUsername(auth.getName())
                    .or(() -> userService.findByEmail(auth.getName()));
            userOpt.ifPresent(u -> model.addAttribute("currentUser", u));
        }
        return "rewards";
    }
}
