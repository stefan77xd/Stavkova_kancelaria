package org.example;
import lombok.Data;



public class User {
    private long userId;
    private String username;
    private String password;
    private String email;
    private double balance;
    private Role role;

    public User(long userId, String username, String password, String email, double balance, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.balance = balance;
        this.role = role;

        }



    }



