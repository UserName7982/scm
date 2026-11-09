package com.scm.config;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.scm.entity.Providers;
import com.scm.entity.User;
import com.scm.helper.Helper;
import com.scm.helper.appconstant;
import com.scm.repositories.userRepo;

@Component
public class Aunthenticationhandler implements AuthenticationSuccessHandler {

    @Autowired
    private userRepo userRepo;

    Logger logger = LoggerFactory.getLogger(Aunthenticationhandler.class);

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        logger.info("Authentication successful");

        String email = Helper.getUserByEmail(authentication);
        String name = "";
        String picture = "default.jpg"; // Default picture for database login

        if (authentication.getPrincipal() instanceof DefaultOAuth2User) {
            // OAuth2 login
            DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();
            name = user.getAttribute("name").toString();
            picture = user.getAttribute("picture").toString();
            logger.info("Logged in with Google: email={}, name={}", email, name);
        } else if (authentication.getPrincipal() instanceof UserDetails) {
            // Database login
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            name = userDetails.getUsername(); // Assuming username is the name
            logger.info("Logged in with database: email={}", email);
        }

        // Create or update user
        User user1 = new User();
        user1.setName(name);
        user1.setEmail(email);
        user1.setPassword("password");
        user1.setProfilepic(picture);
        user1.setUserid(UUID.randomUUID().toString());
        user1.setEmailverified("true");
        user1.setProvider(Providers.GOOGLE); // You might need to set this conditionally
        user1.setProvidersuserid(email);
        user1.setEnabled(true);
        user1.setRoleList(List.of(appconstant.Role_User));
        user1.setAbout("This account was created using Google");

        User user2 = userRepo.findByEmail(email).orElse(null);
        if (user2 == null) {
            userRepo.save(user1);
            logger.info("User saved: {}", email);
        }

        new DefaultRedirectStrategy().sendRedirect(request, response, "/user/profile");
    }
}
