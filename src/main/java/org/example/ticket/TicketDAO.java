package org.example.ticket;

import org.jooq.DSLContext;
import org.jooq.Record11;
import org.jooq.Result;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.codegen.maven.example.Tables.*;

public class TicketDAO {
    private final DSLContext dslContext;

    // Constructor to accept DSLContext
    public TicketDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<Ticket> getUsersTickets(Integer userId) {
        List<Ticket> tickets = new ArrayList<>();

        try {
            // Fetch data from the SQLite tickets table
            Result<Record11<Integer, Integer, Integer, String, BigDecimal, String, Integer, String, LocalDateTime, String, String>> result = dslContext.select(
                            TICKETS.TICKET_ID,
                            TICKETS.USER_ID,
                            TICKETS.OUTCOME_ID,
                            TICKETS.STATUS.as("ticket_status"),
                            TICKETS.STAKE,
                            POSSIBLE_OUTCOMES.RESULT_NAME.as("outcome_name"),
                            POSSIBLE_OUTCOMES.EVENT_ID,
                            SPORT_EVENTS.EVENT_NAME.as("event_name"),
                            SPORT_EVENTS.START_TIME.as("start_time"),
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
                ticket.setStatus(StatusForTicket.valueOf(String.valueOf(record.get("ticket_status"))));
                ticket.setStake(record.get(TICKETS.STAKE).doubleValue());

                ticket.setResultName(String.valueOf(record.get("outcome_name")));
                ticket.setEventName(String.valueOf(record.get("event_name")));
                ticket.setEventStartTime((LocalDateTime) record.get("start_time"));
                tickets.add(ticket);
            }

        } catch (Exception e) {
            System.err.println("Failed to fetch tickets: " + e.getMessage());
        }

        return tickets;
    }
}
