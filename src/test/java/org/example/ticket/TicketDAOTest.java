package org.example.ticket;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.codegen.maven.example.tables.records.TicketsRecord;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.*;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.jooq.codegen.maven.example.Tables.*;
import static org.jooq.codegen.maven.example.tables.SportEvents.SPORT_EVENTS;
import static org.jooq.codegen.maven.example.tables.Users.USERS;
import static org.jooq.impl.DSL.*;
import static org.junit.jupiter.api.Assertions.*;

class TicketDAOTest {
    private DSLContext dslContext;
    private TicketDAO ticketDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = createInMemoryDatabase();
        connection = dataSource.getConnection();
        dslContext = DSL.using(connection);
        ticketDAO = new TicketDAO(dslContext);
        createTicketTable();
        createSportEventTable();
        createPossibleOutcomeTable();
        createUserTable();

        dslContext.insertInto(SPORT_EVENTS)
                .set(SPORT_EVENTS.EVENT_NAME, "Soccer Match")
                .set(SPORT_EVENTS.START_TIME, LocalDateTime.now().plusDays(1))
                .set(SPORT_EVENTS.SPORT_TYPE, "Soccer")
                .set(SPORT_EVENTS.STATUS, "upcoming")
                .set(SPORT_EVENTS.VISIBILITY, "visible")
                .execute();

        dslContext.insertInto(USERS)
                .set(USERS.USERNAME, "john_doe")
                .set(USERS.EMAIL, "john@example.com")
                .set(USERS.PASSWORD, "password")
                .execute();

        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .set(POSSIBLE_OUTCOMES.RESULT_NAME, "Win")
                .set(POSSIBLE_OUTCOMES.EVENT_ID, 1)
                .execute();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    private DataSource createInMemoryDatabase() {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        return dataSource;
    }

    private void createTicketTable() {
        dslContext.createTableIfNotExists(TICKETS)
                .column(TICKETS.TICKET_ID, org.jooq.impl.SQLDataType.INTEGER.identity(true))
                .column(TICKETS.USER_ID, org.jooq.impl.SQLDataType.INTEGER)
                .column(TICKETS.OUTCOME_ID, org.jooq.impl.SQLDataType.INTEGER)
                .column(TICKETS.STATUS, org.jooq.impl.SQLDataType.VARCHAR(20))
                .column(TICKETS.STAKE, org.jooq.impl.SQLDataType.DOUBLE)
                .column(TICKETS.BALANCE_UPDATED, org.jooq.impl.SQLDataType.BOOLEAN.defaultValue(DSL.field(DSL.raw("0"), SQLDataType.BOOLEAN)))
                .primaryKey(TICKETS.TICKET_ID)
                .constraints(
                        DSL.constraint("fk_tickets_user_id")
                                .foreignKey(TICKETS.USER_ID)
                                .references(USERS, USERS.USER_ID),
                        DSL.constraint("fk_tickets_outcome_id")
                                .foreignKey(TICKETS.OUTCOME_ID)
                                .references(POSSIBLE_OUTCOMES, POSSIBLE_OUTCOMES.OUTCOME_ID)
                )
                .execute();
    }


    public void createSportEventTable() {
        dslContext.createTableIfNotExists(SPORT_EVENTS)
                .column(SPORT_EVENTS.EVENT_ID, SQLDataType.INTEGER.identity(true))
                .column(SPORT_EVENTS.EVENT_NAME, SQLDataType.VARCHAR(100).nullable(false))
                .column(SPORT_EVENTS.START_TIME, SQLDataType.LOCALDATETIME.nullable(false))
                .column(SPORT_EVENTS.SPORT_TYPE, SQLDataType.VARCHAR(50).nullable(false))
                .column(SPORT_EVENTS.STATUS, SQLDataType.VARCHAR(10).nullable(false))
                .column(SPORT_EVENTS.VISIBILITY, SQLDataType.VARCHAR(10)
                        .nullable(false)
                        .defaultValue(DSL.field("'visible'", SQLDataType.VARCHAR)))
                .constraints(
                        DSL.constraint("pk_sport_events").primaryKey(SPORT_EVENTS.EVENT_ID),
                        DSL.constraint("chk_status").check(SPORT_EVENTS.STATUS.in("upcoming", "finished")),
                        DSL.constraint("chk_visibility").check(SPORT_EVENTS.VISIBILITY.in("visible", "hidden"))
                )
                .execute();
    }

    private void createPossibleOutcomeTable() {
        dslContext.createTableIfNotExists(POSSIBLE_OUTCOMES)
                .column(POSSIBLE_OUTCOMES.OUTCOME_ID, org.jooq.impl.SQLDataType.INTEGER.identity(true))
                .column(POSSIBLE_OUTCOMES.RESULT_NAME, org.jooq.impl.SQLDataType.VARCHAR(50))
                .column(POSSIBLE_OUTCOMES.EVENT_ID, org.jooq.impl.SQLDataType.INTEGER)
                .primaryKey(POSSIBLE_OUTCOMES.OUTCOME_ID)
                .constraints(
                        DSL.constraint("fk_possible_outcomes_event_id")
                                .foreignKey(POSSIBLE_OUTCOMES.EVENT_ID)
                                .references(SPORT_EVENTS, SPORT_EVENTS.EVENT_ID)
                )
                .execute();
    }

    private void createUserTable() {
        dslContext.createTableIfNotExists(USERS)
                .column(USERS.USER_ID, SQLDataType.INTEGER.identity(true))
                .column(USERS.USERNAME, SQLDataType.VARCHAR(50).nullable(false))
                .column(USERS.PASSWORD, SQLDataType.VARCHAR(60).nullable(false))
                .column(USERS.EMAIL, SQLDataType.VARCHAR(100).nullable(false))
                .column(USERS.BALANCE, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.ROLE, SQLDataType.VARCHAR(10).nullable(false).defaultValue(DSL.field("'user'", SQLDataType.VARCHAR)))
                .column(USERS.TOTAL_BETS, SQLDataType.INTEGER.nullable(false).defaultValue(DSL.field("0", SQLDataType.INTEGER)))
                .column(USERS.TOTAL_STAKES, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.TOTAL_WINNINGS, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.WIN_RATE, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.AVERAGE_BET, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .column(USERS.MAX_BET, SQLDataType.DOUBLE.nullable(false).defaultValue(DSL.field("0", SQLDataType.DOUBLE)))
                .constraints(
                        primaryKey(USERS.USER_ID),
                        unique(USERS.USERNAME),
                        unique(USERS.EMAIL),
                        check(USERS.ROLE.in("user", "admin"))
                )
                .execute();
    }



    @Test
    void testInsertTicket() {
        ticketDAO.insertTicket(1, 1, 50.0);

        List<Ticket> tickets = ticketDAO.getUsersTickets(1);
        assertEquals(1, tickets.size());
        assertEquals(50.0, tickets.get(0).getStake());
        assertEquals(StatusForTicket.pending.name(), tickets.get(0).getStatus().name());
    }

    @Test
    void testUpdateTicketStatusToWon() {
        ticketDAO.insertTicket(1, 1, 50.0);

        Ticket ticket = ticketDAO.getUsersTickets(1).get(0);

        ticketDAO.updateTicketStatusToWon(ticket.getTicketId());

        Ticket updatedTicket = ticketDAO.getUsersTickets(1).get(0);
        assertEquals(StatusForTicket.won.name(), updatedTicket.getStatus().name());
    }

    @Test
    void testUpdateTicketStatusToLost() {
        ticketDAO.insertTicket(1, 1, 50.0);

        Ticket ticket = ticketDAO.getUsersTickets(1).get(0);

        ticketDAO.updateTicketStatusToLost(ticket.getTicketId());

        Ticket updatedTicket = ticketDAO.getUsersTickets(1).get(0);
        assertEquals(StatusForTicket.lost.name(), updatedTicket.getStatus().name());
    }


    @Test
    void testGetUsersTickets() {
        ticketDAO.insertTicket(1, 1, 50.0);
        ticketDAO.insertTicket(1, 1, 100.0);

        List<Ticket> tickets = ticketDAO.getUsersTickets(1);
        assertEquals(2, tickets.size());

        assertEquals(50.0, tickets.get(0).getStake());
        assertEquals(100.0, tickets.get(1).getStake());
    }

    @Test
    void testFetchTicketsRealtedToEvent() {
        ticketDAO.insertTicket(1, 1, 50.0);

        Result<TicketsRecord> tickets = ticketDAO.fetchTicketsRelatedToEvent(1);
        assertEquals(1, tickets.size());
        assertEquals(50.0, tickets.get(0).getStake());
    }

    @Test
    void testFetchTicketsForUser() {
        ticketDAO.insertTicket(1, 1, 50.0);

        Result<TicketsRecord> tickets = ticketDAO.fetchTicketsForUser(1);
        assertEquals(1, tickets.size());
        assertEquals(50.0, tickets.get(0).getStake());
    }

    @Test
    void testInsertTicketWithInvalidUser() {
        assertThrows(IllegalArgumentException.class, () -> ticketDAO.insertTicket(999, 1, 50.0));
    }

    @Test
    void testInsertTicketWithInvalidOutcome() {
        assertThrows(IllegalArgumentException.class, () -> ticketDAO.insertTicket(1, 999, 50.0));
    }

    @Test
    void testInsertMultipleTicketsWithDifferentStakes() {
        ticketDAO.insertTicket(1, 1, 50.0);
        ticketDAO.insertTicket(1, 1, 100.0);
        ticketDAO.insertTicket(1, 1, 200.0);

        List<Ticket> tickets = ticketDAO.getUsersTickets(1);

        assertEquals(3, tickets.size());
        assertEquals(50.0, tickets.get(0).getStake());
        assertEquals(100.0, tickets.get(1).getStake());
        assertEquals(200.0, tickets.get(2).getStake());
    }

    @Test
    void testGetUsersTicketsWhenNoTicketsExist() {
        List<Ticket> tickets = ticketDAO.getUsersTickets(2);
        assertTrue(tickets.isEmpty());
    }

    @Test
    void testInsertTicketWithValidData() {
        ticketDAO.insertTicket(1, 1, 50.0);

        List<Ticket> tickets = ticketDAO.getUsersTickets(1);

        assertEquals(1, tickets.size());
        assertEquals(50.0, tickets.get(0).getStake());
        assertEquals(StatusForTicket.pending.name(), tickets.get(0).getStatus().name());
    }

    @Test
    void testFetchTicketsRealtedToNonExistentEvent() {
        Result<TicketsRecord> tickets = ticketDAO.fetchTicketsRelatedToEvent(999);
        assertTrue(tickets.isEmpty());
    }

}
