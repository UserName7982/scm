package com.scm.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** One logged touchpoint with a contact (a call, message, meeting...). */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "contact_id")
    @JsonIgnore
    private Contact contact;

    @Enumerated(EnumType.STRING)
    private InteractionType type;

    @Column(length = 500)
    private String note;

    private LocalDateTime occurredAt;
}
