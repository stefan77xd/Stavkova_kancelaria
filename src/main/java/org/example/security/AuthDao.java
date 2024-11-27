package org.example.security;

import org.example.exceptions.AuthenticationException;

public interface AuthDao {
    Principal authenticate(String usernameOrEmail, String password) throws AuthenticationException;
}
