package com.scm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.scm.Services.contactservice;
import com.scm.Services.services;
import com.scm.entity.Contact;
import com.scm.entity.User;
import com.scm.helper.Helper;
import com.scm.helper.ResourcesNotFound;

@RestController
@RequestMapping("/api")
public class apicontrol {

    @Autowired
    private contactservice contactservice;

    @Autowired
    private services uServices;

    @GetMapping("/contacts/{contactid}")
    public Contact geContact(@PathVariable("contactid") String contactid, Authentication authentication) {
        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        Contact contact = contactservice.getbyid(contactid);
        // Only the owner may read a contact
        if (contact.getUser() == null || !contact.getUser().getUserid().equals(user.getUserid())) {
            throw new ResourcesNotFound("Contact not found");
        }
        return contact;
    }
}
