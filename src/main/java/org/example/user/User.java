package org.example.user;

import lombok.Data;

@Data
public class User {
    private long userId;
    private String username;
    private String password;
    private String email;
    private double balance;
    private Role role;
    }



