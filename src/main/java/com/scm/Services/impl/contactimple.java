package com.scm.Services.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.scm.Services.contactservice;
import com.scm.entity.Contact;
import com.scm.entity.User;
import com.scm.helper.ResourcesNotFound;
import com.scm.repositories.contactrepo;

@Service
public class contactimple implements contactservice {

    // Only these fields may be used for sorting (prevents 500 errors from bad ?sortby= values)
    private static final Set<String> SORTABLE = Set.of("name", "email", "phoneno", "favourite");

    @Autowired
    private contactrepo databaseconnectivity;

    private Pageable buildPageable(int page, int size, String sortby, String direction) {
        String field = SORTABLE.contains(sortby) ? sortby : "name";
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(field).descending() : Sort.by(field).ascending();
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        return PageRequest.of(safePage, safeSize, sort);
    }

    @Override
    public Contact save(Contact contact) {
        String contactid = UUID.randomUUID().toString();
        contact.setId(contactid);
        return databaseconnectivity.save(contact);
    }

    @Override
    public Contact update(Contact contact) {
        var contactold = databaseconnectivity.findById(contact.getId())
                .orElseThrow(() -> new ResourcesNotFound("contact not found"));
        contactold.setId(contact.getId());
        contactold.setName(contact.getName());
        contactold.setAddress(contact.getAddress());
        contactold.setEmail(contact.getEmail());
        contactold.setFavourite(contact.isFavourite());
        contactold.setPhoneno(contact.getPhoneno());
        contactold.setPicture(contact.getPicture());
        contactold.setDiscreption(contact.getDiscreption());
        contactold.setContactimagepublicid(contact.getContactimagepublicid());

        return databaseconnectivity.save(contactold);
    }

    @Override
    public Contact getbyid(String id) {
        return databaseconnectivity.findById(id).orElseThrow(() -> new ResourcesNotFound("Contact not found"));
    }

    @Override
    public void delete(String id) {
        var contact = databaseconnectivity.findById(id)
                .orElseThrow(() -> new ResourcesNotFound("Contact not found"));
        databaseconnectivity.delete(contact);
    }

    @Override
    public List<Contact> getallconContacts() {
        return databaseconnectivity.findAll();
    }

    @Override
    public List<Contact> getbyUserid(String id) {
        return databaseconnectivity.findByUserid(id);
    }

    @Override
    public Page<Contact> getByuser(User user, int page, int pagesize, String sortby, String direction) {
        return databaseconnectivity.findByUser(user, buildPageable(page, pagesize, sortby, direction));
    }

    @Override
    public Page<Contact> searchByName(String name, int page, int size, String sortby, String direction, User user) {
        return databaseconnectivity.findByUserAndNameContainingIgnoreCase(user, name,
                buildPageable(page, size, sortby, direction));
    }

    @Override
    public Page<Contact> searchByEmail(String email, int page, int size, String sortby, String direction, User user) {
        return databaseconnectivity.findByUserAndEmailContainingIgnoreCase(user, email,
                buildPageable(page, size, sortby, direction));
    }

    @Override
    public Page<Contact> searchByPhoneno(String phoneno, int page, int size, String sortby, String direction,
            User user) {
        return databaseconnectivity.findByUserAndPhonenoContainingIgnoreCase(user, phoneno,
                buildPageable(page, size, sortby, direction));
    }
}
