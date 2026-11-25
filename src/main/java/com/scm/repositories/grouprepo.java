package com.scm.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.scm.entity.ContactGroup;
import com.scm.entity.User;

@Repository
public interface grouprepo extends JpaRepository<ContactGroup, String> {

    List<ContactGroup> findByUserOrderByNameAsc(User user);

    boolean existsByUserAndNameIgnoreCase(User user, String name);

    Optional<ContactGroup> findByUserAndNameIgnoreCase(User user, String name);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from ContactGroup g where g.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
