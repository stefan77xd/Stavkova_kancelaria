package org.example.security;

import org.example.ConfigReader;
import org.example.exceptions.AuthenticationException;
import org.example.user.Role;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.mindrot.jbcrypt.BCrypt;



import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.Properties;

public class SQLiteAuthDAO implements AuthDao {

    private final String dbUrl;

    public SQLiteAuthDAO() {
        // Load configuration from config.properties
        Properties config = ConfigReader.loadProperties("config.properties");
        this.dbUrl = config.getProperty("db.url");
    }

    @Override
    public Principal authenticate(String usernameOrEmail, String password) throws AuthenticationException {
        PrincipalWithPassword principalWithPassword = null;

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);

            // Query to find the user by username or email
            Record record = create.fetchOne(
                    "SELECT user_id, email, username, role, password, balance FROM users WHERE username = ? OR email = ?",
                    usernameOrEmail, usernameOrEmail
            );

            if (record == null) {
                throw new AuthenticationException("Invalid credentials.");
            }

            // Map record to PrincipalWithPassword
            Principal principal = new Principal();
            principal.setId(record.getValue("user_id", Long.class));
            principal.setEmail(record.getValue("email", String.class));
            principal.setUsername(record.getValue("username", String.class));
            principal.setRole(Role.valueOf(record.getValue("role", String.class)));
            principal.setBalance(record.getValue("balance", Double.class));

            principalWithPassword = new PrincipalWithPassword();
            principalWithPassword.setPrincipal(principal);
            principalWithPassword.setPassword(record.getValue("password", String.class));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to authenticate user: " + e.getMessage(), e);
        }

        //Verify password
        if (principalWithPassword == null || !BCrypt.checkpw(password, principalWithPassword.getPassword())) {
            throw new AuthenticationException("Invalid credentials.");
        }
        System.out.println("GREAT SUCCESS");


        return principalWithPassword.getPrincipal();
    }
    // Method to encrypt a password using bcrypt
//    public void encryptPassword(String plainPassword) {
//        System.out.println(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
//    }

}
