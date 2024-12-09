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
import java.util.Locale;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.*;

public class SQLiteAuthDAO implements AuthDao {

    private final String dbUrl;

    public SQLiteAuthDAO() {

        Properties config = ConfigReader.loadProperties("config.properties");
        this.dbUrl = config.getProperty("db.url");
    }

    @Override
    public Principal authenticate(String usernameOrEmail, String password) throws AuthenticationException {
        PrincipalWithPassword principalWithPassword = null;

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);
            Record record = (Record) create.select(USERS.USER_ID, USERS.EMAIL, USERS.USERNAME, USERS.ROLE, USERS.PASSWORD, USERS.BALANCE)
                    .from(USERS)
                    .where(USERS.USERNAME.eq(usernameOrEmail).or(USERS.EMAIL.eq(usernameOrEmail))).fetchOne();
            if (record == null) {
                throw new AuthenticationException("Invalid credentials.");
            }
            String role = record.getValue(USERS.ROLE);
            Principal principal = new Principal();
            principal.setId(Long.valueOf(record.getValue(USERS.USER_ID)));
            principal.setEmail(record.getValue(USERS.EMAIL));
            principal.setUsername(record.getValue(USERS.USERNAME));
            principal.setRole(Role.valueOf(role.toLowerCase()));
            principal.setBalance(record.getValue(USERS.BALANCE).doubleValue());

            principalWithPassword = new PrincipalWithPassword();
            principalWithPassword.setPrincipal(principal);
            principalWithPassword.setPassword(record.getValue(USERS.PASSWORD));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to authenticate user: " + e.getMessage(), e);
        }

        //Verify password
        if (!BCrypt.checkpw(password, principalWithPassword.getPassword())) {
            throw new AuthenticationException("Invalid credentials.");
        }
        System.out.println("GREAT SUCCESS");


        return principalWithPassword.getPrincipal();
    }

}
