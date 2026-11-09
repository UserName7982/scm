package com.scm.controller;

import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.Services.contactservice;
import com.scm.Services.imageservice;
import com.scm.Services.services;
import com.scm.Userform.contactform;
import com.scm.Userform.contactsearchform;
import com.scm.entity.Contact;
import com.scm.entity.User;
import com.scm.helper.Helper;
import com.scm.helper.Message;
import com.scm.helper.MessageType;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user/contacts")
public class contactcontroller {

    Logger logger = LoggerFactory.getLogger(contactcontroller.class);

    @Autowired
    private imageservice imageservice;

    @Autowired
    private contactservice contactservice;

    @Autowired
    private services uServices;

    @RequestMapping("/add")
    public String addcontact(Model model) {
        contactform fContactform = new contactform();
        model.addAttribute("contactform", fContactform);
        return "user/addcontact";
    }

    @RequestMapping(value = "/adds", method = RequestMethod.POST)
    public String savecontact(@Valid @ModelAttribute contactform ContactForm, BindingResult result, HttpSession session,
            Authentication authentication) {

        if (result.hasErrors()) {
            return "user/addcontact";
        } else {

            logger.info("file information: {}", ContactForm.getPicture().getOriginalFilename());

            String username = Helper.getUserByEmail(authentication);
            User user = uServices.getUserByEmailsUser(username);

            String filename = UUID.randomUUID().toString();
            String fileurl = imageservice.uploadimage(ContactForm.getPicture(), filename);

            Contact contact = new Contact();

            contact.setName(ContactForm.getName());
            contact.setEmail(ContactForm.getEmail());
            contact.setAddress(ContactForm.getAddress());
            contact.setPhoneno(ContactForm.getPhoneno());
            contact.setDiscreption(ContactForm.getDiscreption());
            contact.setFavourite(ContactForm.isFavourite());
            contact.setPicture(fileurl);
            contact.setUser(user);
            contact.setContactimagepublicid(filename);
            contactservice.save(contact);

            Message message = Message.builder().content("Contact Added Successfully").messageType(MessageType.green)
                    .build();
            session.setAttribute("message", message);
            return "redirect:/user/contacts/add";
        }
    }

    @RequestMapping
    public String viewcontact(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pagesize", defaultValue = "10") int pagesize,
            @RequestParam(value = "sortby", defaultValue = "name") String sortby,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {

        String username = Helper.getUserByEmail(authentication);
        User userByEmailsUser = uServices.getUserByEmailsUser(username);
        Page<Contact> pageContact = contactservice.getByuser(userByEmailsUser, page, pagesize, sortby, direction);
        model.addAttribute("pageContact", pageContact);
        model.addAttribute("contactsearchform", new contactsearchform());
        return "user/contacts";
    }

    @RequestMapping("/search")
    public String Search(
            @ModelAttribute contactsearchform contactsearchform,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortby", defaultValue = "name") String sortby,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {

        logger.info("field {} keyword {}", contactsearchform.getField(), contactsearchform.getKeyword());

        User user = uServices.getUserByEmailsUser(Helper.getUserByEmail(authentication));
        Page<Contact> pageContact = null;

        if (contactsearchform.getField().equals("email")) {
            pageContact = contactservice.searchByEmail(contactsearchform.getKeyword(), page, size, sortby, direction,
                    user);
        } else if (contactsearchform.getField().equals("name")) {
            pageContact = contactservice.searchByName(contactsearchform.getKeyword(), page, size, sortby, direction,
                    user);
        } else if (contactsearchform.getField().equals("phone")) {
            pageContact = contactservice.searchByPhoneno(contactsearchform.getKeyword(), page, size, sortby, direction,
                    user);
        }

        model.addAttribute("contactsearchform", contactsearchform);
        model.addAttribute("pageContact", pageContact);

        return "user/search";
    }

    @RequestMapping("/delete/{contactid}")
    public String delete(@PathVariable("contactid") String id) {
        contactservice.delete(id);
        logger.info("contactid {} deleted", id);
        return "redirect:/user/contacts";
    }

    @RequestMapping("/view/{contactid}")
    public String view(@PathVariable("contactid") String id,
            Model model) {
        var contact = contactservice.getbyid(id);
        contactform contacts = new contactform();
        contacts.setName(contact.getName());
        contacts.setAddress(contact.getAddress());
        contacts.setEmail(contact.getEmail());
        contacts.setPhoneno(contact.getPhoneno());
        contacts.setDiscreption(contact.getDiscreption());
        contacts.setFavourite(contact.isFavourite());
        contacts.setContactimg(contact.getPicture());
        model.addAttribute("contactform", contacts);
        model.addAttribute("contactid", id);
        return "user/update_contact_view";
    }

    @RequestMapping(value = "/update/{contactid}",method = RequestMethod.POST)
    public String update(@PathVariable("contactid") String id,
           @Valid @ModelAttribute contactform update,BindingResult bindingResult,
            Model model) {

                if(bindingResult.hasErrors()){
                    return "user/update_contact_view";
                }
                

                
        Contact contact = contactservice.getbyid(id);
        contact.setId(id);
        contact.setName(update.getName());
        contact.setEmail(update.getEmail());
        contact.setAddress(update.getAddress());
        contact.setDiscreption(update.getDiscreption());
        contact.setFavourite(update.isFavourite());
        contact.setPhoneno(update.getPhoneno());
        // contact.setPicture(update.getContactimg());
        // contactform contactss = new contactform();
        if (update.getPicture() != null && !update.getPicture().isEmpty()) {
            String filename = UUID.randomUUID().toString();
            String fileurl = imageservice.uploadimage(update.getPicture(), filename);
            contact.setContactimagepublicid(filename);
            contact.setPicture(fileurl);
            update.setContactimg(fileurl);
        }
        logger.info(update.getContactimg());
        contactservice.update(contact);
        model.addAttribute("message",
                Message.builder().content("Updated Successfully!!").messageType(MessageType.green).build());
        return "redirect:/user/contacts/view/"+id;
    }
}

