package org.example.security;

import org.example.exceptions.AuthenticationException;

public class sqlliteDao implements AuthDao {
    @Override
    public Principal authenticate(String usernameOrEmail, String password) throws AuthenticationException {
return null;
    }
}
