package org.example.ticket;

import org.example.ConfigReader;
import org.example.sportevent.SportEvent;
import org.example.sportevent.StatusForEvent;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TicketDAO {
    public List<Ticket> getUsersTickets(Long userId) {
        List<Ticket> tickets = new ArrayList<>();

        // Load configuration from config.properties
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            // Use jOOQ with SQLite connection
            DSLContext create = DSL.using(connection);

            // Fetch data from the SQLite sport_events table
            Result<Record> result = create.fetch(
                    "SELECT ticket_id, user_id, outcome_id, status, stake FROM tickets WHERE user_id = ?",
                    userId
            );

            for (Record record : result) {
                Ticket event = new Ticket();

                event.setTicketId(record.getValue("ticket_id", Long.class));
                event.setUserId(record.getValue("user_id", Long.class));
                event.setOutcomeId(record.getValue("outcome_id", Long.class));
                event.setStatus(record.getValue("status", StatusForTicket.class));
                event.setStake(record.getValue("stake", Double.class));

                tickets.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch tickets: " + e.getMessage());
        }

        return tickets;
    }
}
