package org.example.statistics;

import org.jooq.DSLContext;
import org.jooq.Record6;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;

import static org.jooq.codegen.maven.example.Tables.USERS;
public class StatisticsDAO {
    private final DSLContext dslContext;
    public StatisticsDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    // zdroj https://www.jooq.org/doc/latest/manual/getting-started/tutorials/jooq-in-7-steps/
    public List<Statistics> getUsersStats(Integer userId) {
        List<Statistics> stats = new ArrayList<>();
        try {
            Result<Record6<Integer, Double, Double, Double, Double, Double>> result = dslContext.select(
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

            for (Record6<Integer, Double, Double, Double, Double, Double> record : result) {
                Statistics stat = new Statistics();
                stat.setTotalBets(record.get(USERS.TOTAL_BETS));
                stat.setTotalStakes(record.get(USERS.TOTAL_STAKES));
                stat.setTotalWinnings(record.get(USERS.TOTAL_WINNINGS));
                stat.setWinRate(record.get(USERS.WIN_RATE));
                stat.setAverageBet(record.get(USERS.AVERAGE_BET));
                stat.setMaxBet(record.get(USERS.MAX_BET));
                stats.add(stat);
            }
        } catch (Exception e) {
            System.err.println("Zlyhalo načítanie štatistiky: " + e.getMessage());
        }
        return stats;
    }
}
