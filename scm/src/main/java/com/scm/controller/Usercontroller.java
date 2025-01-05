package com.scm.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.scm.Services.contactservice;
import com.scm.Services.services;
import com.scm.entity.Contact;
import com.scm.entity.User;
import com.scm.helper.Helper;

@Controller
@RequestMapping("/user")
public class Usercontroller {

    @Autowired
    private contactservice contactservice;
    
    @Autowired
    private services uServices;

    Logger logger = LoggerFactory.getLogger(Usercontroller.class);

    @RequestMapping(value = "/dashboard")
    public String dashboard(Authentication authentication,Model model) {

        String username = Helper.getUserByEmail(authentication);
        User user = uServices.getUserByEmailsUser(username);

       
        List<Contact> contact =  contactservice.getbyUserid(user.getUserid());
        model.addAttribute("pageContact", contact.size());

        return "user/dashboard";
    }

    @RequestMapping(value = "/profile")
    public String profile(Authentication authentication, Model model) {

        return "user/profile";
    }
    @RequestMapping("/delete/{userid}")
        public String delete(@PathVariable("userid") String id){
            uServices.deleteUser(id);
            return "redirect:/logout";
        }
    }

