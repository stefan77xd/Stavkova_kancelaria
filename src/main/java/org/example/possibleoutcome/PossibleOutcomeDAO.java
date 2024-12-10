package org.example.possibleoutcome;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.Result;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.codegen.maven.example.Tables.*;

public class PossibleOutcomeDAO {
    private final DSLContext dslContext;

    public PossibleOutcomeDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<PossibleOutcome> getPossibleOutcomesByEventId(long eventId) {
        List<PossibleOutcome> outcomes = new ArrayList<>();
            Result<Record5<Integer, Integer, String, BigDecimal, String>> result =
                    dslContext.select(
                                    POSSIBLE_OUTCOMES.OUTCOME_ID,
                                    POSSIBLE_OUTCOMES.EVENT_ID,
                                    POSSIBLE_OUTCOMES.RESULT_NAME,
                                    POSSIBLE_OUTCOMES.ODDS,
                                    POSSIBLE_OUTCOMES.STATUS)
                            .from(POSSIBLE_OUTCOMES)
                            .where(POSSIBLE_OUTCOMES.EVENT_ID.eq((int) eventId))
                            .fetch();

            for (Record record : result) {
                PossibleOutcome outcome = new PossibleOutcome();
                outcome.setOutcomeId(record.get(POSSIBLE_OUTCOMES.OUTCOME_ID));
                outcome.setEventId(record.getValue(POSSIBLE_OUTCOMES.EVENT_ID));
                outcome.setResultName(record.getValue(POSSIBLE_OUTCOMES.RESULT_NAME));
                outcome.setOdds(record.getValue(POSSIBLE_OUTCOMES.ODDS).doubleValue());
                outcome.setStatusForOutcomes(StatusForOutcomes.valueOf(record.getValue(POSSIBLE_OUTCOMES.STATUS)));
                outcomes.add(outcome);
            }

        return outcomes;
    }

    public List<String> fetchAllWinningOutcomes(int eventID) {
        return dslContext.select(POSSIBLE_OUTCOMES.RESULT_NAME)
                .from(POSSIBLE_OUTCOMES)
                .where(POSSIBLE_OUTCOMES.EVENT_ID.eq(eventID))
                .and(POSSIBLE_OUTCOMES.STATUS.eq(StatusForOutcomes.winning.name()))
                .fetch(POSSIBLE_OUTCOMES.RESULT_NAME);
    }
}
