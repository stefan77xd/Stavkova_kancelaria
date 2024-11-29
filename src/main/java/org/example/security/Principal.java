package org.example.security;

import lombok.Data;
import org.example.user.Role;

@Data
public class Principal {

    private Long id;
    private String email;
    private String username;
    private Role role;
    private double balance;

}
