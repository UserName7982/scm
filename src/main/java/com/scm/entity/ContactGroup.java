package com.scm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** A named group (e.g. Family, Work) that belongs to one user. */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "name" }))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactGroup {

    @Id
    private String id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;
}
