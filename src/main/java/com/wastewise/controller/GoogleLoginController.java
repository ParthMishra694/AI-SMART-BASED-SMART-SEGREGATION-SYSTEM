package com.wastewise.controller;

import com.wastewise.config.OAuth2Config;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Entry point for the "Continue with Google" button.
 * Only forwards to Spring Security's Google OAuth2 flow when a real client ID is present.
 */
@Controller
@RequestMapping("/oauth2")
public class GoogleLoginController {

    private final OAuth2Config oauth2Config;

    public GoogleLoginController(OAuth2Config oauth2Config) {
        this.oauth2Config = oauth2Config;
    }

    @GetMapping("/google")
    public String continueWithGoogle() {
        if (!oauth2Config.isGoogleConfigured()) {
            return "redirect:/login?error=oauth";
        }
        return "redirect:/oauth2/authorization/google";
    }
}
