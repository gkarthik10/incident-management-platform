package com.karthik.incidentmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    // Defense in depth: even if a User entity is ever serialized directly
    // in the future, the hash must never leave the server.
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}