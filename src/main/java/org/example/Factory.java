package org.example;

import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.sportevent.SportEventDAO;
import org.example.statistics.StatisticsDAO;
import org.example.ticket.TicketDAO;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public enum Factory {
    INSTANCE;

    private volatile SportEventDAO sportEventDAO;
    private volatile PossibleOutcomeDAO possibleOutcomeDAO;
    private volatile TicketDAO ticketDAO;
    private volatile StatisticsDAO statisticsDAO;

    private final Object lock = new Object();
    private volatile DSLContext dslContext;

    // Method to get SQLite DSLContext
    public DSLContext getSQLiteDSLContext() {
        if (dslContext == null) {
            synchronized (lock) {
                if (dslContext == null) {
                    // Load the configuration properties from config.properties
                    Properties config = ConfigReader.loadProperties("config.properties");
                    String dbUrl = config.getProperty("db.url");

                    // Establish a connection using DriverManager and the SQLite DB URL
                    try (Connection connection = DriverManager.getConnection(dbUrl)) {
                        // Initialize the jOOQ DSLContext for SQLite
                        dslContext = DSL.using(connection, SQLDialect.SQLITE);
                    } catch (SQLException e) {
                        // Handle SQL exceptions, e.g., logging or rethrowing
                        throw new RuntimeException("Error connecting to the database", e);
                    }
                }
            }
        }
        return dslContext;
    }

    // Method to get SportEventDAO using SQLite DSLContext
    public SportEventDAO getSportEventDAO() {
        if (sportEventDAO == null) {
            synchronized (lock) {
                if (sportEventDAO == null) {
                    sportEventDAO = new SportEventDAO();
                }
            }
        }
        return sportEventDAO;
    }

    // Method to get PossibleOutcomeDAO using SQLite DSLContext
    public PossibleOutcomeDAO getPossibleOutcomeDAO() {
        if (possibleOutcomeDAO == null) {
            synchronized (lock) {
                if (possibleOutcomeDAO == null) {
                    possibleOutcomeDAO = new PossibleOutcomeDAO();
                }
            }
        }
        return possibleOutcomeDAO;
    }

    // Method to get TicketDAO using SQLite DSLContext
    public TicketDAO getTicketDAO() {
        if (ticketDAO == null) {
            synchronized (lock) {
                if (ticketDAO == null) {
                    ticketDAO = new TicketDAO();
                }
            }
        }
        return ticketDAO;
    }

    // Method to get StatisticsDAO using SQLite DSLContext
    public StatisticsDAO getStatisticsDAO() {
        if (statisticsDAO == null) {
            synchronized (lock) {
                if (statisticsDAO == null) {
                    statisticsDAO = new StatisticsDAO();
                }
            }
        }
        return statisticsDAO;
    }
}


