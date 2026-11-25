package com.scm.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.scm.entity.Contact;
import com.scm.entity.Interaction;

@Repository
public interface interactionrepo extends JpaRepository<Interaction, String> {

    List<Interaction> findByContactOrderByOccurredAtDesc(Contact contact);

    Optional<Interaction> findFirstByContactOrderByOccurredAtDesc(Contact contact);

    long countByContact(Contact contact);
}
