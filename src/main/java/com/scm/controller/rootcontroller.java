package com.scm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.scm.Services.services;
import com.scm.entity.User;
import com.scm.helper.Helper;

@ControllerAdvice
public class rootcontroller {

    
    Logger logger = LoggerFactory.getLogger(Usercontroller.class);
    @Autowired
    private services userService;  // Injecting the services implementation

    @ModelAttribute
    public void getLoggedinUserinformation(Model model,Authentication authentication){
        if(authentication==null){
            return;
        }

        String name = Helper.getUserByEmail(authentication);
        logger.info("User logged in: {}", name);

        User user = userService.getUserByEmailsUser(name);  // Using the injected service
        System.out.println(user.getName());
        model.addAttribute("email", name);
        model.addAttribute("user", user);
        model.addAttribute("name", user.getName());
    }
   
}
