package org.example.user;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.mindrot.jbcrypt.BCrypt;

import static org.jooq.codegen.maven.example.Tables.TICKETS;
import static org.jooq.codegen.maven.example.Tables.USERS;
public class UserDAO {
    private final DSLContext dslContext;

    //zdroj https://www.jooq.org/doc/latest/manual/getting-started/tutorials/jooq-in-7-steps/
    public UserDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }
    public boolean userExists(String username, String email) {
        return dslContext.fetchExists(
                DSL.selectOne()
                        .from(USERS)
                        .where(USERS.USERNAME.eq(username).or(USERS.EMAIL.eq(email)))
        );
    }

    public void insertUser(String username, String hashedPassword, String email) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Neplatné používateľské meno");
        }
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Neplatné heslo");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Neplatný email");
        }
        dslContext.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .set(USERS.PASSWORD, hashedPassword)
                .set(USERS.EMAIL, email)
                .set(USERS.BALANCE, 0.0)
                .set(USERS.ROLE, "user")
                .execute();
    }

    public String findEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Neplatný email");
        }
        return dslContext.select(USERS.EMAIL).from(USERS).where(USERS.EMAIL.eq(email)).fetchOneInto(String.class);
    }

    public void updatePassword(String newPassword, String email) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Neplatné heslo");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Neplatný email");
        }
        dslContext.update(USERS).set(USERS.PASSWORD, newPassword).where(USERS.EMAIL.eq(email)).execute();
    }

    public void updateStatistics() {
        dslContext.update(USERS)
                .set(USERS.AVERAGE_BET,
                        DSL.case_()
                                .when(USERS.TOTAL_BETS.isNotNull().and(USERS.TOTAL_BETS.gt(0)),
                                        DSL.round(
                                                USERS.TOTAL_STAKES.cast(Double.class).divide(USERS.TOTAL_BETS.cast(Double.class))
                                        ))
                                .otherwise(DSL.val(0.0)))
                .execute();
    }

    public void updateBalanceAndStat(int userID, Double betAmount) {
        if (betAmount == null) {
            throw new IllegalArgumentException("Neplatná suma");
        }
        dslContext.update(USERS)
                .set(USERS.BALANCE, USERS.BALANCE.minus(betAmount))
                .set(USERS.TOTAL_BETS, USERS.TOTAL_BETS.plus(1))
                .set(USERS.MAX_BET,
                        DSL.when(USERS.MAX_BET.lessThan(betAmount), betAmount)
                                .otherwise(USERS.MAX_BET))
                .set(USERS.TOTAL_STAKES, USERS.TOTAL_STAKES.plus(betAmount))

                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public void addBalance(int userID, Double amountValue) {
        if (amountValue == null) {
            throw new IllegalArgumentException("Neplatná suma");
        }
        dslContext.update(USERS)
                .set(USERS.BALANCE, USERS.BALANCE.plus(amountValue))
                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public Double getBalance(int userID) {
        return dslContext.select(USERS.BALANCE)
                .from(USERS)
                .where(USERS.USER_ID.eq(userID))
                .fetchOneInto(Double.class);
    }

    public void updateBalanceWithTicket(Double stake, Double odds, int userID) {
        dslContext.update(USERS)
                .set(USERS.BALANCE, USERS.BALANCE.plus(stake * odds))
                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public void updateWinRateAndTotalWinnings(Double roundedWinRate, Double totalWinnings, int userID) {
        dslContext.update(USERS)
                .set(USERS.WIN_RATE, roundedWinRate)
                .set(USERS.TOTAL_WINNINGS, totalWinnings)
                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public void deleteUser(String username, int userID) {
        dslContext.deleteFrom(TICKETS).where(TICKETS.USER_ID.eq(userID)).execute();

        dslContext.deleteFrom(USERS).where(USERS.USERNAME.eq(username)).execute();
    }

    public void changeUsername(int userID, String username) {
        dslContext.update(USERS)
                .set(USERS.USERNAME, username)
                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public void changePassword(int userID, String password) {
        dslContext.update(USERS)
                .set(USERS.PASSWORD, BCrypt.hashpw(password, BCrypt.gensalt()))
                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public boolean menoExists(String text) {
        return dslContext.fetchExists(
                DSL.selectOne()
                        .from(USERS)
                        .where(USERS.USERNAME.eq(text)))
                ;
    }
}
