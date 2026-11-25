package com.scm.entity;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import java.util.Set;
import java.util.TreeSet;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Contact {

    @Id
    private String id;
    private String name;
    private String email;
    private String phoneno;
    private String address;
    private String picture;
    private String discreption;
    private boolean favourite=false;
    private String contactimagepublicid;
    @ManyToOne
    @JsonIgnore
    private User user;
      @OneToMany(mappedBy = "contact",cascade = CascadeType.ALL,fetch = FetchType.EAGER,orphanRemoval = true)
    private List<SocialLinks> links=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "group_id")
    private ContactGroup contactGroup;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "contact_tags", joinColumns = @JoinColumn(name = "contact_id"))
    @Column(name = "tag", length = 30)
    private Set<String> tags = new TreeSet<>();
}
