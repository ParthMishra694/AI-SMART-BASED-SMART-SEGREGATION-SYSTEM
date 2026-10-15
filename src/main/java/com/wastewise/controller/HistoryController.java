package com.wastewise.controller;

import com.wastewise.entity.User;
import com.wastewise.entity.WasteAnalysis;
import com.wastewise.service.UserService;
import com.wastewise.service.WasteAnalysisService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/history")
public class HistoryController {

    private final WasteAnalysisService wasteAnalysisService;
    private final UserService userService;

    public HistoryController(WasteAnalysisService wasteAnalysisService, UserService userService) {
        this.wasteAnalysisService = wasteAnalysisService;
        this.userService = userService;
    }

    @GetMapping
    public String historyPage(@RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "q", required = false) String query,
                              Model model,
                              Authentication auth) {
        User user = userService.resolveUser(auth);

        List<WasteAnalysis> analyses;
        if (query != null && !query.trim().isEmpty()) {
            analyses = wasteAnalysisService.searchHistory(user, query);
        } else if (category != null && !category.equalsIgnoreCase("ALL")) {
            analyses = wasteAnalysisService.filterByCategory(user, category);
        } else {
            analyses = wasteAnalysisService.getUserHistory(user);
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("analyses", analyses);
        model.addAttribute("selectedCategory", category != null ? category : "ALL");
        model.addAttribute("searchQuery", query != null ? query : "");
        model.addAttribute("totalCount", wasteAnalysisService.countUserAnalyses(user));

        return "history";
    }

    @GetMapping("/{id}")
    public String viewHistoryDetail(@PathVariable("id") Long id, Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        WasteAnalysis analysis = wasteAnalysisService.getAnalysisById(id).orElseThrow();

        if (!analysis.getUser().getId().equals(user.getId()) && !"ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/history";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("analysis", analysis);
        return "history-detail";
    }

    @PostMapping("/delete/{id}")
    public String deleteAnalysis(@PathVariable("id") Long id,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.resolveUser(auth);
        boolean deleted = wasteAnalysisService.deleteAnalysis(id, user);

        if (deleted) {
            redirectAttributes.addFlashAttribute("successMessage", "Analysis record deleted. Note: Your earned EcoPoints ledger remains intact and preserved.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete record.");
        }
        return "redirect:/history";
    }
}
