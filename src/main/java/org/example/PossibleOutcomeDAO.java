package org.example;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
            Result<Record> result = create.fetch("SELECT outcome_id, event_id, result_name, odds, status FROM possible_outcomes WHERE event_id = ?", eventId);

            // Iterate through the results and map them to PossibleOutcome objects
            for (Record record : result) {
                PossibleOutcome outcome = new PossibleOutcome();

                outcome.setOutcomeId(record.getValue("outcome_id", Long.class));
                outcome.setEventId(record.getValue("event_id", Long.class));
                outcome.setResultName(record.getValue("result_name", String.class));
                outcome.setOdds(record.getValue("odds", Double.class));
                outcome.setStatusForOutcomes(StatusForOutcomes.valueOf(record.getValue("status", String.class)));

                outcomes.add(outcome);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch possible outcomes: " + e.getMessage());
        }

        return outcomes;
    }
}
