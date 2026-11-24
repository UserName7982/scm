package com.scm.repositories;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scm.entity.Contact;
import com.scm.entity.User;

@Repository
public interface contactrepo extends JpaRepository<Contact, String> {
    
    Page<Contact> findByUser(User user ,Pageable pageable);

    @Query("SELECT c FROM Contact c WHERE c.user.id = :userid")
    List<Contact> findByUserid(@Param("userid") String userid);

    Page<Contact> findByUserAndNameContainingIgnoreCase(User user,String name,Pageable pageable);

    Page<Contact> findByUserAndEmailContainingIgnoreCase(User user,String email,Pageable pageable);

    Page<Contact> findByUserAndPhonenoContainingIgnoreCase(User user,String phoneno,Pageable pageable);

    Page<Contact> findByUserAndFavouriteTrue(User user, Pageable pageable);

    long countByUser(User user);

    long countByUserAndFavouriteTrue(User user);

    List<Contact> findByUserOrderByNameAsc(User user);

    boolean existsByUserAndEmailIgnoreCase(User user, String email);

    boolean existsByUserAndPhoneno(User user, String phoneno);

}
