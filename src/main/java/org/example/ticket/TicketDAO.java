package org.example.ticket;

import org.jooq.DSLContext;
import org.jooq.Record11;
import org.jooq.Result;
import org.jooq.codegen.maven.example.tables.records.TicketsRecord;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.codegen.maven.example.Tables.*;

public class TicketDAO {
    private final DSLContext dslContext;
    public TicketDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }
    public List<Ticket> getUsersTickets(Integer userId) {
        List<Ticket> tickets = new ArrayList<>();
            Result<Record11<Integer, Integer, Integer, String, Double, String, Integer, String, LocalDateTime, String, String>> result = dslContext.select(
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

            for (Record11<Integer, Integer, Integer, String, Double, String, Integer, String, LocalDateTime, String, String> record : result) {
                Ticket ticket = new Ticket();

                ticket.setTicketId(record.get(TICKETS.TICKET_ID));
                ticket.setUserId(record.get(TICKETS.USER_ID));
                ticket.setOutcomeId(record.get(TICKETS.OUTCOME_ID));
                ticket.setStatus(StatusForTicket.valueOf(String.valueOf(record.get("ticket_status"))));
                ticket.setStake(record.get(TICKETS.STAKE));

                ticket.setResultName(String.valueOf(record.get("outcome_name")));
                ticket.setEventName(String.valueOf(record.get("event_name")));
                ticket.setEventStartTime((LocalDateTime) record.get("start_time"));
                tickets.add(ticket);
            }
        return tickets;
    }

    public void insertTicket(int userID, int outcomeID, Double betAmount) {
        dslContext.insertInto(TICKETS)
                .set(TICKETS.USER_ID, userID)
                .set(TICKETS.OUTCOME_ID, outcomeID)
                .set(TICKETS.STAKE, betAmount)
                .set(TICKETS.STATUS, StatusForTicket.pending.name())
                .execute();
    }

    public void updateTicketStatusToWon(int ticketID) {
        dslContext.update(TICKETS)
                .set(TICKETS.STATUS, StatusForTicket.won.name())
                .where(TICKETS.TICKET_ID.eq(ticketID))
                .execute();
    }

    public void updateTicketStatusToLost(int ticketID) {
        dslContext.update(TICKETS)
                .set(TICKETS.STATUS, StatusForTicket.lost.name())
                .where(TICKETS.TICKET_ID.eq(ticketID))
                .execute();
    }

    public Result<TicketsRecord> fetchTicketsRealtedToEvent(int evnetID) {
        return dslContext.select(TICKETS.fields())
                .from(TICKETS)
                .join(POSSIBLE_OUTCOMES)
                .on(TICKETS.OUTCOME_ID.eq(POSSIBLE_OUTCOMES.OUTCOME_ID))
                .where(POSSIBLE_OUTCOMES.EVENT_ID.eq(evnetID))
                .fetchInto(TICKETS);
    }

    public Result<TicketsRecord> fetchTicketsForUser(int userID) {
        return dslContext.selectFrom(TICKETS)
                .where(TICKETS.USER_ID.eq(userID))
                .fetch();
    }
}
