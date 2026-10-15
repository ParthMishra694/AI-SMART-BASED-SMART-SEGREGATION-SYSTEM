package com.wastewise.controller;

import com.wastewise.config.OAuth2Config;
import com.wastewise.dto.RegistrationDto;
import com.wastewise.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private static final String GOOGLE_SETUP_MESSAGE =
            "Google Sign-In is not configured. Add a real GOOGLE_CLIENT_ID "
                    + "(ending in .apps.googleusercontent.com) and GOOGLE_CLIENT_SECRET "
                    + "to application-local.properties, then restart the app.";

    private final UserService userService;
    private final OAuth2Config oauth2Config;

    public AuthController(UserService userService, OAuth2Config oauth2Config) {
        this.userService = userService;
        this.oauth2Config = oauth2Config;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            org.springframework.security.core.Authentication auth,
                            Model model) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("googleConfigured", oauth2Config.isGoogleConfigured());
        if (error != null) {
            if ("oauth".equalsIgnoreCase(error) || "google".equalsIgnoreCase(error)) {
                model.addAttribute("errorMessage", GOOGLE_SETUP_MESSAGE);
            } else {
                model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
            }
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out safely.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(org.springframework.security.core.Authentication auth, Model model) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("googleConfigured", oauth2Config.isGoogleConfigured());
        model.addAttribute("registrationDto", new RegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("registrationDto") RegistrationDto dto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("googleConfigured", oauth2Config.isGoogleConfigured());
            return "register";
        }

        try {
            userService.registerUser(dto);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("googleConfigured", oauth2Config.isGoogleConfigured());
            model.addAttribute("errorMessage", e.getMessage());
            return "register";
        }
    }
}
