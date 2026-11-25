package com.scm.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.Services.RelationshipService;
import com.scm.Services.contactservice;
import com.scm.Services.services;
import com.scm.entity.User;
import com.scm.helper.Helper;
import com.scm.helper.ResourcesNotFound;

@Controller
@RequestMapping("/user")
public class Usercontroller {

    @Autowired
    private RelationshipService relationships;

    @Autowired
    private contactservice contactservice;
    
    @Autowired
    private services uServices;

    Logger logger = LoggerFactory.getLogger(Usercontroller.class);

    @RequestMapping(value = "/dashboard")
    public String dashboard(Authentication authentication,Model model) {

        String username = Helper.getUserByEmail(authentication);
        User user = uServices.getUserByEmailsUser(username);

       
        model.addAttribute("totalContacts", contactservice.countByUser(user));
        model.addAttribute("dueCount", relationships.dueCount(user));
        model.addAttribute("dueContacts", relationships.dueOrOverdue(user).stream().limit(5).toList());
        model.addAttribute("favouriteCount", contactservice.countFavourites(user));
        model.addAttribute("favouriteContacts", contactservice.getFavourites(user, 0, 5, "name", "asc").getContent());

        return "user/dashboard";
    }

    @RequestMapping(value = "/profile")
    public String profile(Authentication authentication, Model model) {

        return "user/profile";
    }
    @RequestMapping("/delete/{userid}")
        public String delete(@PathVariable("userid") String id, Authentication authentication){
            User current = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
            if (!current.getUserid().equals(id)) {
                throw new ResourcesNotFound("User not found");
            }
            uServices.deleteUser(id);
            return "redirect:/logout";
        }
    }

