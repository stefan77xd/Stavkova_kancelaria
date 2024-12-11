package org.example.security;

import lombok.Data;
import org.example.user.Role;

@Data
public class Principal {
    private Integer id;
    private String email;
    private String username;
    private Role role;
    private Double balance;

}
