package com.wastewise.controller;

import com.wastewise.agent.WasteAgentService;
import com.wastewise.entity.User;
import com.wastewise.entity.WasteAnalysis;
import com.wastewise.service.UserService;
import com.wastewise.service.WasteAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/scan")
public class WasteScanController {

    private static final Logger log = LoggerFactory.getLogger(WasteScanController.class);

    private final WasteAgentService wasteAgentService;
    private final WasteAnalysisService wasteAnalysisService;
    private final UserService userService;

    public WasteScanController(WasteAgentService wasteAgentService,
                               WasteAnalysisService wasteAnalysisService,
                               UserService userService) {
        this.wasteAgentService = wasteAgentService;
        this.wasteAnalysisService = wasteAnalysisService;
        this.userService = userService;
    }

    @GetMapping
    public String scanPage(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        model.addAttribute("currentUser", user);
        return "scan";
    }

    @PostMapping("/analyze")
    public String analyzeWastePhoto(@RequestParam("image") MultipartFile file,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        User user = userService.resolveUser(auth);

        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select or take a photo of a waste item.");
            return "redirect:/scan";
        }

        try {
            WasteAnalysis analysis = wasteAgentService.processWasteScan(user, file);
            return "redirect:/scan/result/" + analysis.getId();
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/scan";
        } catch (Exception e) {
            log.error("Error analyzing waste photo", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to analyze photo: " + e.getMessage());
            return "redirect:/scan";
        }
    }

    @GetMapping("/result/{id}")
    public String scanResult(@PathVariable("id") Long id, Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        WasteAnalysis analysis = wasteAnalysisService.getAnalysisById(id).orElseThrow();

        // Enforce privacy: user can only view their own scan result (or admin)
        if (!analysis.getUser().getId().equals(user.getId()) && !"ROLE_ADMIN".equals(user.getRole())) {
            return "redirect:/dashboard";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("analysis", analysis);
        return "scan-result";
    }
}
