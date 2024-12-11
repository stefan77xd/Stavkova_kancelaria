package org.example.user;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class User {
    private Integer userId;
    private String username;
    private String password;
    private String email;
    private BigDecimal balance;
    private Role role;
    }



