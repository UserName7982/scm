  package com.scm.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

public class Helper {

    private static final Logger logger = LoggerFactory.getLogger(Helper.class);

    public static String getUserByEmail(Authentication authentication) {

        if (authentication instanceof OAuth2AuthenticationToken) {

            OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
            String id = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
            if (id.equalsIgnoreCase("google")) {
                logger.info("Login with Google");
                var principal = oAuth2AuthenticationToken.getPrincipal();
                var attributes = principal.getAttributes();
                logger.info("User attributes: {}", attributes);
                String email = attributes.get("email").toString();
                logger.info("User email: {}", email);
                return email;
            } else {
                logger.info("Login with another OAuth2 provider");
                return "";
            }
        } else {
            logger.info("Login with database");
            logger.info("User name: {}", authentication.getName());
            // Assume the username is the email for database login
            return authentication.getName();
        }
    }
}
