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
                .column(USERS.BALANCE, SQLDataType.NUMERIC(10, 2).nullable(false).defaultValue(DSL.field("0", SQLDataType.NUMERIC)))
                .column(USERS.ROLE, SQLDataType.VARCHAR(10).nullable(false).defaultValue(DSL.field("'user'", SQLDataType.VARCHAR)))
                .column(USERS.TOTAL_BETS, SQLDataType.INTEGER.nullable(false).defaultValue(DSL.field("0", SQLDataType.INTEGER)))
                .column(USERS.TOTAL_STAKES, SQLDataType.NUMERIC(10, 2).nullable(false).defaultValue(DSL.field("0", SQLDataType.NUMERIC)))
                .column(USERS.TOTAL_WINNINGS, SQLDataType.NUMERIC(10, 2).nullable(false).defaultValue(DSL.field("0", SQLDataType.NUMERIC)))
                .column(USERS.WIN_RATE, SQLDataType.NUMERIC(3, 2).nullable(false).defaultValue(DSL.field("0", SQLDataType.NUMERIC)))
                .column(USERS.AVERAGE_BET, SQLDataType.NUMERIC(10, 2).nullable(false).defaultValue(DSL.field("0", SQLDataType.NUMERIC)))
                .column(USERS.MAX_BET, SQLDataType.NUMERIC(10, 2).nullable(false).defaultValue(DSL.field("0", SQLDataType.NUMERIC)))
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

        // Update the total_bets and total_stakes using the dslContext
        dslContext.update(USERS)
                .set(USERS.TOTAL_BETS, 10)
                .set(USERS.TOTAL_STAKES, BigDecimal.valueOf(200))
                .where(USERS.USERNAME.eq("john_doe"))
                .execute();

        // Call updateStatistics to calculate and update average_bet
        userDAO.updateStatistics();

        BigDecimal averageBet = dslContext
                .select(USERS.AVERAGE_BET)
                .from(USERS)
                .where(USERS.USERNAME.eq("john_doe"))
                .fetchOneInto(BigDecimal.class);

        assertEquals(BigDecimal.valueOf(20.00).setScale(2), averageBet);
    }

    @Test
    void testUpdateBalanceAndStat() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        userDAO.updateBalanceAndStat(1, 50);

        BigDecimal balance = userDAO.getBalance(1);
        assertEquals(BigDecimal.valueOf(-50.00).setScale(2), balance);

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
        userDAO.addBalance(1, 100);

        BigDecimal balance = userDAO.getBalance(1);
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), balance);
    }

    @Test
    void testGetBalance() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        BigDecimal balance = userDAO.getBalance(1);
        assertEquals(BigDecimal.ZERO.setScale(2), balance);

        userDAO.addBalance(1, 100);
        BigDecimal updatedBalance = userDAO.getBalance(1);
        assertEquals(BigDecimal.valueOf(100.00).setScale(2), updatedBalance);
    }

    @Test
    void testUpdateBalanceWithTicket() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        userDAO.updateBalanceWithTicket(BigDecimal.valueOf(50), BigDecimal.valueOf(2.5), 1);

        BigDecimal balance = userDAO.getBalance(1);
        assertEquals(BigDecimal.valueOf(125.00).setScale(2), balance); // 50 * 2.5 = 125
    }

    @Test
    void testUpdateWinRateAndTotalWinnings() {
        userDAO.insertUser("john_doe", "password", "john@example.com");
        userDAO.updateWinRateAndTotalWinnings(BigDecimal.valueOf(0.75), BigDecimal.valueOf(1000), 1);

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

        assertEquals(BigDecimal.valueOf(0.75).setScale(2), winRate);
        assertEquals(BigDecimal.valueOf(1000.00).setScale(2), totalWinnings);
    }
}
