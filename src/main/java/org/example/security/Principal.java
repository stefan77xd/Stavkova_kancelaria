package org.example.security;

import lombok.Data;
import org.example.user.Role;

import java.math.BigDecimal;

@Data
public class Principal {

    private Integer id;
    private String email;
    private String username;
    private Role role;
    private Double balance;

}
