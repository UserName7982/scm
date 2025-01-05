package com.scm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.scm.Services.contactservice;
import com.scm.entity.Contact;

@RestController
@RequestMapping("/api")
public class apicontrol {

    @Autowired
    private contactservice contactservice;

    @GetMapping("/contacts/{contactid}")
    public Contact geContact(@PathVariable("contactid") String contactid){
        return contactservice.getbyid(contactid);
    }
}
