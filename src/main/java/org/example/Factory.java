package org.example;

import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.security.SQLiteAuthDAO;
import org.example.sportevent.SportEventDAO;
import org.example.statistics.StatisticsDAO;
import org.example.ticket.TicketDAO;
import org.example.user.UserDAO;
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
    private volatile UserDAO userDAO;
    private volatile SQLiteAuthDAO sqLiteAuthDAO;

    private final Object lock = new Object();
    private volatile DSLContext dslContext;

    // zdroj https://gitlab.science.upjs.sk/paz1c-2024/attender/-/blob/c09/src/main/java/sk/upjs/ics/Factory.java?ref_type=heads

    public DSLContext getSQLiteDSLContext() {
        if (dslContext == null) {
            synchronized (lock) {
                if (dslContext == null) {
                    try {
                        Properties config = ConfigReader.loadProperties("config.properties");
                        String dbUrl = config.getProperty("db.url");
                        Connection connection = DriverManager.getConnection(dbUrl);
                        dslContext = DSL.using(connection, SQLDialect.SQLITE);
                    } catch (SQLException e) {
                        throw new RuntimeException("Error connecting to the database", e);
                    }
                }
            }
        }
        return dslContext;
    }

    public SportEventDAO getSportEventDAO() {
        if (sportEventDAO == null) {
            synchronized (lock) {
                if (sportEventDAO == null) {
                    sportEventDAO = new SportEventDAO(getSQLiteDSLContext());
                }
            }
        }
        return sportEventDAO;
    }

    public PossibleOutcomeDAO getPossibleOutcomeDAO() {
        if (possibleOutcomeDAO == null) {
            synchronized (lock) {
                if (possibleOutcomeDAO == null) {
                    possibleOutcomeDAO = new PossibleOutcomeDAO(getSQLiteDSLContext());
                }
            }
        }
        return possibleOutcomeDAO;
    }

    public TicketDAO getTicketDAO() {
        if (ticketDAO == null) {
            synchronized (lock) {
                if (ticketDAO == null) {
                    ticketDAO = new TicketDAO(getSQLiteDSLContext());
                }
            }
        }
        return ticketDAO;
    }

    public StatisticsDAO getStatisticsDAO() {
        if (statisticsDAO == null) {
            synchronized (lock) {
                if (statisticsDAO == null) {
                    statisticsDAO = new StatisticsDAO(getSQLiteDSLContext());
                }
            }
        }
        return statisticsDAO;
    }

    public UserDAO getUserDAO() {
        if (userDAO == null) {
            synchronized (lock) {
                if (userDAO == null) {
                    userDAO = new UserDAO(getSQLiteDSLContext());
                }
            }
        }
        return userDAO;
    }

    public SQLiteAuthDAO getSqLiteAuthDAO() {
        if (sqLiteAuthDAO == null) {
            synchronized (lock) {
                if (sqLiteAuthDAO == null) {
                    sqLiteAuthDAO = new SQLiteAuthDAO(getSQLiteDSLContext());
                }
            }
        }
        return sqLiteAuthDAO;
    }
}


