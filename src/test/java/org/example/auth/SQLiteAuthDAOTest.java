package org.example.auth;

import org.example.exceptions.AuthenticationException;
import org.example.security.Principal;
import org.example.security.SQLiteAuthDAO;
import org.example.user.Role;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.jooq.codegen.maven.example.Tables.USERS;
import static org.junit.jupiter.api.Assertions.*;

class SQLiteAuthDAOTest {

    private static Connection connection;
    private static DSLContext dslContext;
    private static SQLiteAuthDAO authDAO;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        connection = dataSource.getConnection();
        dslContext = DSL.using(connection, SQLDialect.SQLITE);
        authDAO = new SQLiteAuthDAO(dslContext);

        dslContext.createTable(USERS)
                .column(USERS.USER_ID, USERS.USER_ID.getDataType())
                .column(USERS.EMAIL, USERS.EMAIL.getDataType())
                .column(USERS.USERNAME, USERS.USERNAME.getDataType())
                .column(USERS.ROLE, USERS.ROLE.getDataType())
                .column(USERS.PASSWORD, USERS.PASSWORD.getDataType())
                .column(USERS.BALANCE, USERS.BALANCE.getDataType())
                .constraints(
                        DSL.primaryKey(USERS.USER_ID)
                )
                .execute();
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        connection.close();
    }

    @BeforeEach
    void clearDatabase() {
        dslContext.deleteFrom(USERS).execute();
    }

    @Test
    void testAuthenticateValidUser() throws AuthenticationException {
        String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
        dslContext.insertInto(USERS)
                .columns(USERS.USER_ID, USERS.EMAIL, USERS.USERNAME, USERS.ROLE, USERS.PASSWORD, USERS.BALANCE)
                .values(1, "test@example.com", "testuser", "user", hashedPassword, 100.0)
                .execute();

        Principal principal = authDAO.authenticate("testuser", "password123");

        assertNotNull(principal);
        assertEquals(1, principal.getId());
        assertEquals("test@example.com", principal.getEmail());
        assertEquals("testuser", principal.getUsername());
        assertEquals(Role.user, principal.getRole());
        assertEquals(100.0, principal.getBalance());
    }

    @Test
    void testAuthenticateInvalidPassword() {
        String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
        dslContext.insertInto(USERS)
                .columns(USERS.USER_ID, USERS.EMAIL, USERS.USERNAME, USERS.ROLE, USERS.PASSWORD, USERS.BALANCE)
                .values(1, "test@example.com", "testuser", "user", hashedPassword, 100.0)
                .execute();

        assertThrows(AuthenticationException.class, () -> authDAO.authenticate("testuser", "wrongpassword"));
    }

    @Test
    void testAuthenticateInvalidUsernameOrEmail() {
        assertThrows(AuthenticationException.class, () -> authDAO.authenticate("nonexistent", "password123"));
    }

    @Test
    void testAuthenticateWithEmail() throws AuthenticationException {
        String hashedPassword = BCrypt.hashpw("password123", BCrypt.gensalt());
        dslContext.insertInto(USERS)
                .columns(USERS.USER_ID, USERS.EMAIL, USERS.USERNAME, USERS.ROLE, USERS.PASSWORD, USERS.BALANCE)
                .values(1, "test@example.com", "testuser", "user", hashedPassword, 100.0)
                .execute();

        Principal principal = authDAO.authenticate("test@example.com", "password123");

        assertNotNull(principal);
        assertEquals(1, principal.getId());
        assertEquals("test@example.com", principal.getEmail());
        assertEquals("testuser", principal.getUsername());
        assertEquals(Role.user, principal.getRole());
        assertEquals(100.0, principal.getBalance());
    }

    @Test
    void testAuthenticateNoUsersInDatabase() {
        assertThrows(AuthenticationException.class, () -> authDAO.authenticate("testuser", "password123"));
    }
}
