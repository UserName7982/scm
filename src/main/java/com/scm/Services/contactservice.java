package com.scm.Services;

import java.util.List;

import org.springframework.data.domain.Page;

import com.scm.entity.Contact;
import com.scm.entity.ContactGroup;
import com.scm.entity.User;


public interface contactservice {

    Contact save(Contact contact);

    List<Contact> getallconContacts();

    Contact update(Contact contact);

    Contact getbyid(String id);

    void delete(String id);

    Page<Contact> searchByName(String name,int page, int size,String sortby,String direction,User user);
    Page<Contact> searchByEmail(String email,int page, int size,String sortby,String direction,User user);
    Page<Contact> searchByPhoneno(String phoneno,int page, int size,String sortby,String direction,User user);


    List<Contact> getbyUserid(String id);

    
    Page<Contact> getByuser(User user,int page,int pagesize,String sortby, String direction);

    Contact toggleFavourite(String id);

    Page<Contact> getFavourites(User user, int page, int pagesize, String sortby, String direction);

    long countByUser(User user);

    long countFavourites(User user);

    List<Contact> getAllForUser(User user);

    boolean isDuplicate(User user, String email, String phoneno);

    Page<Contact> getByGroup(User user, ContactGroup group, int page, int pagesize, String sortby, String direction);

    Page<Contact> getByTag(User user, String tag, int page, int pagesize, String sortby, String direction);

    List<String> getDistinctTags(User user);

}
