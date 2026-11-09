package com.scm.Services.impl;

import java.util.List;
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

    @Autowired
    private contactrepo databaseconnectivity;

    @Override
    public Contact save(Contact contact) {
        String contactid=UUID.randomUUID().toString();
        contact.setId(contactid);
       return databaseconnectivity.save(contact);
    }

    @Override
    public Contact update(Contact contact) {
       var contactold=databaseconnectivity.findById(contact.getId()).orElseThrow(()-> new ResourcesNotFound("contact not found"));
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
       return databaseconnectivity.findById(id).orElseThrow(()->new ResourcesNotFound("Contact not found"));
    }

    @Override
    public void delete(String id) {
      var contact=databaseconnectivity.findById(id).orElseThrow(()->new ResourcesNotFound("Contact not found"));
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
    public Page<Contact> getByuser(User user,int page,int pagesize, String sortby, String direction) {
   
      Sort sort=direction.equals("desc")? Sort.by(sortby).descending(): Sort.by(sortby).ascending();
      Pageable pageable=PageRequest.of(page, pagesize,sort);
      return databaseconnectivity.findByUser(user,pageable);
    }

    @Override
    public Page<Contact> searchByName(String name, int page, int size, String sortby, String direction, User user) {
        Sort sort = direction.equals("desc") ? Sort.by(sortby).descending() : Sort.by(sortby).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return databaseconnectivity.findByUserAndNameContaining(user, name, pageable);
    }
    
    @Override
    public Page<Contact> searchByEmail(String email, int page, int size, String sortby, String direction, User user) {
        Sort sort = direction.equals("desc") ? Sort.by(sortby).descending() : Sort.by(sortby).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return databaseconnectivity.findByUserAndEmailContaining(user, email, pageable);
    }
    
    @Override
    public Page<Contact> searchByPhoneno(String phoneno, int page, int size, String sortby, String direction, User user) {
        Sort sort = direction.equals("desc") ? Sort.by(sortby).descending() : Sort.by(sortby).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return databaseconnectivity.findByUserAndPhonenoContaining(user, phoneno, pageable);
    }
    

 

   



}
