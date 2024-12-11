package org.example.user;

import lombok.Data;

@Data
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String email;
    private Double balance;
    private Role role;
    }



