package com.habibi.authservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String fullName;

    @Enumerated(EnumType.STRING)
    private Role role;

    // getter setter
}