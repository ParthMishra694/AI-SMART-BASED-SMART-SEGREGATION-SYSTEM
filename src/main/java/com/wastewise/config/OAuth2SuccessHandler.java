package com.wastewise.config;

import com.wastewise.entity.User;
import com.wastewise.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles the successful OAuth2/OIDC login and ensures the Google user
 * is provisioned in the database. Redirects to location setup if not done,
 * otherwise straight to /dashboard.
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserService userService;

    public OAuth2SuccessHandler(@Lazy UserService userService) {
        this.userService = userService;
        setDefaultTargetUrl("/dashboard");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        String targetUrl = "/dashboard";

        if (authentication.getPrincipal() instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            if (email != null) {
                User user = userService.findOrCreateGoogleUser(email, name);
                String picture = oAuth2User.getAttribute("picture");
                if (picture != null && user != null) {
                    request.getSession().setAttribute("googleProfilePic", picture);
                }
                // Redirect to location setup if user hasn't set their location yet
                if (user != null && !user.isLocationSetupComplete()) {
                    targetUrl = "/setup-location";
                }
            }
        }
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
