package org.example;

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

public class SportEventDAO {

    public List<SportEvent> getAllSportEvents() {
        List<SportEvent> events = new ArrayList<>();

        // Načítanie konfigurácie z config.properties
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");
        String dbUsername = config.getProperty("db.username");
        String dbPassword = config.getProperty("db.password");

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            DSLContext create = DSL.using(connection);

            Result<Record> result = create.fetch("SELECT event_id, event_name, start_time, sport_type, status FROM sport_events");

            for (Record record : result) {

                SportEvent event = new SportEvent();


                event.setEventId(record.getValue("event_id", Long.class));
                event.setEventName(record.getValue("event_name", String.class));
                event.setStartTime(record.getValue("start_time", LocalDateTime.class));
                event.setSportType(record.getValue("sport_type", String.class));
               // event.setStatus(StatusForEvent.valueOf(record.getValue("status", String.class))); // Konverzia String na enum


                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch sport events: " + e.getMessage());
        }

        return events;
    }
}
