package org.example.user;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class UserDAO {
    private final DSLContext dslContext;

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
        dslContext.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .set(USERS.PASSWORD, hashedPassword)
                .set(USERS.EMAIL, email)
                .set(USERS.BALANCE, BigDecimal.ZERO)
                .set(USERS.ROLE, "user")
                .execute();
    }

    public String findEmail(String email) {
        return dslContext.select(USERS.EMAIL).from(USERS).where(USERS.EMAIL.eq(email)).fetchOneInto(String.class);
    }

    public void updatePassword(String newPassword, String email) {
        dslContext.update(USERS).set(USERS.PASSWORD, newPassword).where(USERS.EMAIL.eq(email)).execute();
    }

    public void updateStatistics() {
        dslContext.update(USERS)
                .set(USERS.AVERAGE_BET,
                        DSL.case_()
                                .when(USERS.TOTAL_BETS.isNotNull().and(USERS.TOTAL_BETS.gt(0)),
                                        DSL.round(
                                                USERS.TOTAL_STAKES.cast(BigDecimal.class).divide(USERS.TOTAL_BETS.cast(BigDecimal.class))
                                        ))
                                .otherwise(DSL.val(BigDecimal.ZERO)))
                .execute();
    }

    public void updateBalanceAndStat(int userID, double betAmount) {
        dslContext.update(USERS)
                .set(USERS.BALANCE, USERS.BALANCE.minus(BigDecimal.valueOf(betAmount)))
                .set(USERS.TOTAL_BETS, USERS.TOTAL_BETS.plus(1))
                .set(USERS.MAX_BET,
                        DSL.when(USERS.MAX_BET.lessThan(BigDecimal.valueOf(betAmount)), BigDecimal.valueOf(betAmount))
                                .otherwise(USERS.MAX_BET))
                .set(USERS.TOTAL_STAKES, USERS.TOTAL_STAKES.plus(BigDecimal.valueOf(betAmount)))

                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public void addBalance(int userID, double amountValue) {
        dslContext.update(USERS)
                .set(USERS.BALANCE, USERS.BALANCE.plus(amountValue))
                .where(USERS.USER_ID.eq(userID))
                .execute();
    }

    public BigDecimal getBalance(int userID) {
        return dslContext.select(USERS.BALANCE)
                .from(USERS)
                .where(USERS.USER_ID.eq(userID))
                .fetchOneInto(BigDecimal.class);
    }

    public void updateBalanceWithTicket(BigDecimal stake, BigDecimal odds, int userID) {
        dslContext.update(USERS)
                .set(USERS.BALANCE, USERS.BALANCE.plus(stake.multiply(odds)))
                .where(USERS.USER_ID.eq(userID))
                .execute();

    }

    public void updateWinRateAndTotalWinnings(BigDecimal roundedWinRate, BigDecimal totalWinnings, int userID) {
        dslContext.update(USERS)
                .set(USERS.WIN_RATE, roundedWinRate)
                .set(USERS.TOTAL_WINNINGS, totalWinnings)
                .where(USERS.USER_ID.eq(userID))
                .execute();
    }
}
