package org.example.possibleoutcome;

import org.example.ConfigReader;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.jooq.codegen.maven.example.Tables.*;
public class PossibleOutcomeDAO {

    public List<PossibleOutcome> getPossibleOutcomesByEventId(long eventId) {
        List<PossibleOutcome> outcomes = new ArrayList<>();

        // Load configuration from config.properties (assuming it's already set up like in SportEventDAO)
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            // Use jOOQ with the SQLite connection
            DSLContext create = DSL.using(connection);

            // Query the possible_outcomes table for outcomes matching the eventId
            Result<Record5<Integer, Integer, String, BigDecimal, String>> result =
                    create.select(
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
                outcome.setOdds((record.getValue( POSSIBLE_OUTCOMES.ODDS).doubleValue()));
                outcome.setStatusForOutcomes(StatusForOutcomes.valueOf(record.getValue(POSSIBLE_OUTCOMES.STATUS)));
                outcomes.add(outcome);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch possible outcomes: " + e.getMessage());
        }

        return outcomes;
    }
}
