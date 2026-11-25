package com.scm.repositories;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scm.entity.Contact;
import com.scm.entity.ContactGroup;
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

    Page<Contact> findByUserAndContactGroup(User user, ContactGroup group, Pageable pageable);

    @Query(value = "select c from Contact c where c.user = :user and :tag member of c.tags",
            countQuery = "select count(c) from Contact c where c.user = :user and :tag member of c.tags")
    Page<Contact> findByUserAndTag(@Param("user") User user, @Param("tag") String tag, Pageable pageable);

    @Query("select distinct t from Contact c join c.tags t where c.user = :user order by t")
    List<String> findDistinctTags(@Param("user") User user);

    long countByContactGroup(ContactGroup group);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Contact c set c.contactGroup = null where c.contactGroup = :group")
    void clearGroup(@Param("group") ContactGroup group);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Contact c set c.contactGroup = null where c.user = :user")
    void clearGroupsForUser(@Param("user") User user);

    List<Contact> findByUserAndFollowUpDateLessThanEqualOrderByFollowUpDateAsc(User user, LocalDate date);

    List<Contact> findByUserAndFollowUpDateBetweenOrderByFollowUpDateAsc(User user, LocalDate from, LocalDate to);

    long countByUserAndFollowUpDateLessThanEqual(User user, LocalDate date);

    List<Contact> findByUserAndLastContactedAtBeforeOrderByLastContactedAtAsc(User user, LocalDateTime cutoff, Pageable pageable);

}
