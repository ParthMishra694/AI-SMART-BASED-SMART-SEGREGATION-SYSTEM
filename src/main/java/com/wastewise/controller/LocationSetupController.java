package com.wastewise.controller;

import com.wastewise.entity.User;
import com.wastewise.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LocationSetupController {

    private final UserService userService;

    public LocationSetupController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/setup-location")
    public String setupLocationPage(Model model, Authentication auth) {
        User user = userService.resolveUser(auth);
        // If already set up, redirect to dashboard
        if (user.isLocationSetupComplete()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("currentUser", user);
        return "setup-location";
    }

    @PostMapping("/setup-location")
    public String saveLocation(@RequestParam("city") String city,
                               @RequestParam("state") String state,
                               @RequestParam("country") String country,
                               Authentication auth,
                               RedirectAttributes redirectAttributes) {
        User user = userService.resolveUser(auth);
        userService.setupLocation(user, city, state, country);
        redirectAttributes.addFlashAttribute("successMessage",
                "Welcome! Your location has been set to " + user.getLocationDisplay());
        return "redirect:/dashboard";
    }
}
