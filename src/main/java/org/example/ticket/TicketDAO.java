package org.example.ticket;


import org.example.ConfigReader;
import org.jooq.DSLContext;
import org.jooq.Name;
import org.jooq.Record11;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.*;

public class TicketDAO {
    public List<Ticket> getUsersTickets(Integer userId) {
        List<Ticket> tickets = new ArrayList<>();

        // Load configuration from config.properties
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            // Use jOOQ with SQLite connection
            DSLContext create = DSL.using(connection);

            // Fetch data from the SQLite sport_events table
            Result<Record11<Integer, Integer, Integer, String, BigDecimal, String, Integer, String, LocalDateTime, String, String>> result = create.select(
                            TICKETS.TICKET_ID,
                            TICKETS.USER_ID,
                            TICKETS.OUTCOME_ID,
                            TICKETS.STATUS.as("ticket_status"),
                            TICKETS.STAKE,
                            POSSIBLE_OUTCOMES.RESULT_NAME.as("outcome_name"),
                            POSSIBLE_OUTCOMES.EVENT_ID,
                            SPORT_EVENTS.EVENT_NAME,
                            SPORT_EVENTS.START_TIME,
                            SPORT_EVENTS.SPORT_TYPE,
                            SPORT_EVENTS.STATUS.as("event_status")
                    )
                    .from(TICKETS)
                    .join(POSSIBLE_OUTCOMES).on(TICKETS.OUTCOME_ID.eq(POSSIBLE_OUTCOMES.OUTCOME_ID))
                    .join(SPORT_EVENTS).on(POSSIBLE_OUTCOMES.EVENT_ID.eq(SPORT_EVENTS.EVENT_ID))
                    .where(TICKETS.USER_ID.eq(userId))
                    .fetch();

            for (Record11<Integer, Integer, Integer, String, BigDecimal, String, Integer, String, LocalDateTime, String, String> record : result) {
                Ticket ticket = new Ticket();

                ticket.setTicketId(record.get(TICKETS.TICKET_ID));
                ticket.setUserId(record.get(TICKETS.USER_ID));
                ticket.setOutcomeId(record.get(TICKETS.OUTCOME_ID));
                ticket.setStatus(StatusForTicket.valueOf((String) record.get("ticket_status")));
                ticket.setStake(record.get(TICKETS.STAKE).doubleValue());
                ticket.setResultName(String.valueOf(record.get(POSSIBLE_OUTCOMES.RESULT_NAME)));

                System.out.println("Result Name: " + String.valueOf(record.get(POSSIBLE_OUTCOMES.RESULT_NAME)));

                tickets.add(ticket);
            }

        } catch (SQLException e) {
            System.err.println("Failed to fetch tickets: " + e.getMessage());
        }

        return tickets;
    }
}