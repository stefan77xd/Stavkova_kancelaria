package org.example.user;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.*;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

import static org.jooq.codegen.maven.example.tables.Users.USERS;
import static org.jooq.impl.DSL.*;
import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest {
    private DSLContext dslContext;
    private UserDAO userDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = createInMemoryDatabase();
        connection = dataSource.getConnection();
        dslContext = DSL.using(connection);
        userDAO = new UserDAO(dslContext);
        createUserTable();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    // zdroj https://github.com/dropwizard/dropwizard/issues/1500
    // https://www.sqlite.org/inmemorydb.html
    // https://www.jooq.org/doc/latest/manual/getting-started/tutorials/jooq-in-7-steps/

    private DataSource createInMemoryDatabase() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        return dataSource;
    }

    private void createUserTable() {
        dslContext.createTableIfNotExists(USERS)
                .column(USERS.USER_ID, SQLDataType.INTEGER.identity(true))
                .column(USERS.USERNAME, SQLDataType.VARCHAR(50).nullable(false))
                .column(USERS.PASSWORD, SQLDataType.VARCHAR(60).nullable(false))
                .column(USERS.EMAIL, SQLDataType.VARCHAR(100).nullable(false))
                .column(USERS.BALANCE, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.ROLE, SQLDataType.VARCHAR(10).nullable(false).defaultValue(DSL.field("'user'", SQLDataType.VARCHAR)))
                .column(USERS.TOTAL_BETS, SQLDataType.INTEGER.nullable(false).defaultValue(DSL.field("0", SQLDataType.INTEGER)))
                .column(USERS.TOTAL_STAKES, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.TOTAL_WINNINGS, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.WIN_RATE, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.AVERAGE_BET, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.MAX_BET, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .constraints(
                        primaryKey(USERS.USER_ID),
                        unique(USERS.USERNAME),
                        unique(USERS.EMAIL),
                        check(USERS.ROLE.in("user", "admin"))
                )
                .execute();
    }


    @Test
    void testUserExists() {
        userDAO.insertUser("john_doe", "hashedpassword", "john@example.com");
        assertTrue(userDAO.userExists("john_doe", "john@example.com"));
        assertFalse(userDAO.userExists("nonexistent", "nonexistent@example.com"));
    }

    @Test
    void testInsertUser() {
        userDAO.insertUser("jane_doe", "hashedpassword", "jane@example.com");
        String email = userDAO.findEmail("jane@example.com");
        assertEquals("jane@example.com", email);
    }

    @Test
    void testFindEmail() {
        userDAO.insertUser("john_doe", "hashedpassword", "john@example.com");
        String email = userDAO.findEmail("john@example.com");
        assertEquals("john@example.com", email);
    }

    @Test
    void testUpdatePassword() {
        userDAO.insertUser("john_doe", "oldpassword", "john@example.com");
        userDAO.updatePassword("newpassword", "john@example.com");

        String newPassword = dslContext
                .select(USERS.PASSWORD)
                .from(USERS)
                .where(USERS.EMAIL.eq("john@example.com"))
                .fetchOneInto(String.class);

        assertEquals("newpassword", newPassword);
    }

    @Test
    void testUpdateStatistics() {
        userDAO.insertUser("john_doe", "password", "john@example.com");

        dslContext.update(USERS)
                .set(USERS.TOTAL_BETS, 10)
                .set(USERS.TOTAL_STAKES, 200.0)
                .where(USERS.USERNAME.eq("john_doe"))
                .execute();

        userDAO.updateStatistics();

        BigDecimal averageBet = dslContext
                .select(USERS.AVERAGE_BET)
                .from(USERS)
                .where(USERS.USERNAME.eq("john_doe"))
                .fetchOneInto(BigDecimal.class);

        assertEquals(BigDecimal.valueOf(20.00), averageBet.setScale(1));
    }

    @Test
    void testUpdateBalanceAndStat() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        userDAO.updateBalanceAndStat(1, 50.0);

        Double balance = userDAO.getBalance(1);
        assertEquals(-50.00, balance);

        Integer totalBets = dslContext
                .select(USERS.TOTAL_BETS)
                .from(USERS)
                .where(USERS.USER_ID.eq(1))
                .fetchOneInto(Integer.class);

        assertEquals(1, totalBets);
    }

    @Test
    void testAddBalance() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        userDAO.addBalance(1, 100.0);

        Double balance = userDAO.getBalance(1);
        assertEquals(100.00, balance);
    }

    @Test
    void testGetBalance() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        Double balance = userDAO.getBalance(1);
        assertEquals(0.0, balance);

        userDAO.addBalance(1, 100.0);
        Double updatedBalance = userDAO.getBalance(1);
        assertEquals(100.0, updatedBalance);
    }

    @Test
    void testUpdateBalanceWithTicket() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        userDAO.updateBalanceWithTicket(50.0, 2.5, 1);

        Double balance = userDAO.getBalance(1);
        assertEquals(125.00, balance);
    }

    @Test
    void testUpdateWinRateAndTotalWinnings() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        userDAO.updateWinRateAndTotalWinnings(0.75, 1000.0, 1);

        BigDecimal winRate = dslContext
                .select(USERS.WIN_RATE)
                .from(USERS)
                .where(USERS.USER_ID.eq(1))
                .fetchOneInto(BigDecimal.class);

        BigDecimal totalWinnings = dslContext
                .select(USERS.TOTAL_WINNINGS)
                .from(USERS)
                .where(USERS.USER_ID.eq(1))
                .fetchOneInto(BigDecimal.class);

        assertEquals(BigDecimal.valueOf(0.75), winRate);
        assertEquals(BigDecimal.valueOf(1000.00), totalWinnings.setScale(1));
    }

    @Test
    void testInsertUserWithEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.insertUser("", "hashedpassword", "test@example.com");
        });
    }

    @Test
    void testInsertUserWithEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.insertUser("jane_doe", "hashedpassword", "");
        });
    }

    @Test
    void testInsertUserWithNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.insertUser(null, "hashedpassword", "test@example.com");
        });
    }

    @Test
    void testInsertUserWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.insertUser("jane_doe", "hashedpassword", null);
        });
    }

    @Test
    void testUpdatePasswordWithEmptyUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.updatePassword("newpassword", "");
        });
    }

    @Test
    void testUpdatePasswordWithNullUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.updatePassword("newpassword", null);
        });
    }

    @Test
    void testUpdatePasswordWithEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.updatePassword("newpassword", "");
        });
    }

    @Test
    void testUpdatePasswordWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.updatePassword("newpassword", null);
        });
    }

    @Test
    void testUpdateBalanceWithZeroAmount() {
        userDAO.insertUser("john_doe", "password", "john@example.com");

        userDAO.updateBalanceAndStat(1, 0.0);

        Double balance = userDAO.getBalance(1);
        assertEquals(0.0, balance);
    }

    @Test
    void testUpdateBalanceWithEmptyAmount() {
        userDAO.insertUser("john_doe", "password", "john@example.com");

        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.updateBalanceAndStat(1, null);
        });
    }

    @Test
    void testAddBalanceWithZeroAmount() {
        userDAO.insertUser("john_doe", "password", "john@example.com");

        userDAO.addBalance(1, 0.0);

        Double balance = userDAO.getBalance(1);
        assertEquals(0.0, balance);
    }

    @Test
    void testAddBalanceWithNullAmount() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.addBalance(1, null);
        });
    }

    @Test
    void testFindEmailWithEmptyEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.findEmail("");
        });
    }

    @Test
    void testFindEmailWithNullEmail() {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.findEmail(null);
        });
    }

    @Test
    void testUserExistsWithEmptyUsernameAndEmail() {
        assertFalse(userDAO.userExists("", ""));
    }

    @Test
    void testUserExistsWithNullUsernameAndEmail() {
        assertFalse(userDAO.userExists(null, null));
    }

    private void assertInvalidInputForInsertUser(String username, String password, String email) {
        assertThrows(IllegalArgumentException.class, () -> {
            userDAO.insertUser(username, password, email);
        });
    }

    @Test
    void testInsertUserWithEmptyInputs() {
        assertInvalidInputForInsertUser("", "hashedpassword", "test@example.com");
        assertInvalidInputForInsertUser("username", "hashedpassword", "");
        assertInvalidInputForInsertUser("username", "hashedpassword", null);
        assertInvalidInputForInsertUser(null, "hashedpassword", "test@example.com");
    }
}
