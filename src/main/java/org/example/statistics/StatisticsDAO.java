package org.example.statistics;

import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.Result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class StatisticsDAO {
    private final DSLContext dslContext;

    // Constructor to accept DSLContext
    public StatisticsDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<Statistics> getUsersStats(Integer userId) {
        List<Statistics> stats = new ArrayList<>();

        try {
            // Fetch statistics from the users table
            Result<Record6<Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> result = dslContext.select(
                            USERS.TOTAL_BETS,
                            USERS.TOTAL_STAKES,
                            USERS.TOTAL_WINNINGS,
                            USERS.WIN_RATE,
                            USERS.AVERAGE_BET,
                            USERS.MAX_BET
                    )
                    .from(USERS)
                    .where(USERS.USER_ID.eq(userId))
                    .fetch();

            for (Record6<Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal> record : result) {
                Statistics stat = new Statistics();
                stat.setTotalBets(record.get(USERS.TOTAL_BETS));  // TOTAL_BETS is an Integer
                stat.setTotalStakes(record.get(USERS.TOTAL_STAKES).doubleValue());  // TOTAL_STAKES is a BigDecimal
                stat.setTotalWinnings(record.get(USERS.TOTAL_WINNINGS).doubleValue());  // TOTAL_WINNINGS is a BigDecimal
                stat.setWinRate(record.get(USERS.WIN_RATE).doubleValue());  // WIN_RATE is a BigDecimal
                stat.setAverageBet(record.get(USERS.AVERAGE_BET).doubleValue());  // AVERAGE_BET is a BigDecimal
                stat.setMaxBet(record.get(USERS.MAX_BET).doubleValue());  // MAX_BET is a BigDecimal

                stats.add(stat);
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch user statistics: " + e.getMessage());
        }

        return stats;
    }
}
