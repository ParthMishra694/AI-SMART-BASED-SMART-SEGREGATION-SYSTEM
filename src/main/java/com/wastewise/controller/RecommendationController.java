package com.wastewise.controller;

import com.wastewise.entity.User;
import com.wastewise.service.RecommendationService;
import com.wastewise.service.UserService;
import com.wastewise.service.WasteAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final WasteAnalysisService wasteAnalysisService;
    private final UserService userService;

    public RecommendationController(RecommendationService recommendationService,
                                    WasteAnalysisService wasteAnalysisService,
                                    UserService userService) {
        this.recommendationService = recommendationService;
        this.wasteAnalysisService = wasteAnalysisService;
        this.userService = userService;
    }

    @GetMapping("/recommendations")
    public String recommendations(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        List<String> recommendations = recommendationService.generatePersonalizedRecommendations(user);
        Map<String, Long> categoryDistribution = wasteAnalysisService.getCategoryDistribution(user);

        model.addAttribute("currentUser", user);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("categoryDistribution", categoryDistribution);
        return "recommendations";
    }
}
