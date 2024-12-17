package org.example.sportevent;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.codegen.maven.example.Tables;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static org.jooq.codegen.maven.example.tables.SportEvents.SPORT_EVENTS;

public class SportEventDAO {
    private final DSLContext dslContext;
    public SportEventDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    //zdroj https://www.jooq.org/doc/latest/manual/getting-started/tutorials/jooq-in-7-steps/
    public List<SportEvent> getAllSportEvents() {
        List<SportEvent> events = new ArrayList<>();
        Result<Record5<Integer, String, LocalDateTime, String, String>> result = dslContext
                .select(SPORT_EVENTS.EVENT_ID, SPORT_EVENTS.EVENT_NAME, SPORT_EVENTS.START_TIME, SPORT_EVENTS.SPORT_TYPE, SPORT_EVENTS.STATUS)
                .from(SPORT_EVENTS)
                .where(SPORT_EVENTS.VISIBILITY.eq("visible"))
                .orderBy(SPORT_EVENTS.START_TIME.asc())
                .fetch();

        for (Record record : result) {
            SportEvent event = new SportEvent();
            event.setEventId(record.getValue(SPORT_EVENTS.EVENT_ID));
            event.setEventName(record.getValue(SPORT_EVENTS.EVENT_NAME));
            event.setStartTime(record.getValue(SPORT_EVENTS.START_TIME));
            event.setSportType(record.getValue(SPORT_EVENTS.SPORT_TYPE));
            event.setStatus(StatusForEvent.valueOf(record.getValue(SPORT_EVENTS.STATUS)));
            events.add(event);
        }

        return events;
    }

    public void hideEvent(int eventId) {
        dslContext.update(SPORT_EVENTS)
                .set(SPORT_EVENTS.VISIBILITY, "hidden")
                .where(SPORT_EVENTS.EVENT_ID.eq(eventId))
                .execute();
    }

    public int createEvent(String eventName, String startTime, String sportType) {
        if (eventName == null || eventName.isEmpty()) {
            throw new IllegalArgumentException("Neplatný názov eventu");
        }
        if (startTime == null || startTime.isEmpty()) {
            throw new IllegalArgumentException("Neplatný čas eventu");
        }
        if (sportType == null || sportType.isEmpty()) {
            throw new IllegalArgumentException("Neplatný športový typ");
        }

        try {
            LocalDateTime parsedStartTime = LocalDateTime.parse(startTime);
            return dslContext.insertInto(Tables.SPORT_EVENTS)
                    .set(Tables.SPORT_EVENTS.EVENT_NAME, eventName)
                    .set(Tables.SPORT_EVENTS.START_TIME, parsedStartTime)
                    .set(Tables.SPORT_EVENTS.SPORT_TYPE, sportType)
                    .set(Tables.SPORT_EVENTS.STATUS, StatusForEvent.upcoming.name())
                    .returning(Tables.SPORT_EVENTS.EVENT_ID)
                    .fetchOne()
                    .getValue(Tables.SPORT_EVENTS.EVENT_ID);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Neplatný formát dátumu", e);
        }
    }

    public void updateEventStatus(int eventID) {
        boolean eventExists = dslContext.fetchExists(
                DSL.selectOne().from(Tables.SPORT_EVENTS)
                        .where(Tables.SPORT_EVENTS.EVENT_ID.eq(eventID))
        );

        if (!eventExists) {
            throw new IllegalArgumentException("Event nebol nájdený");
        }

        dslContext.update(Tables.SPORT_EVENTS)
                .set(Tables.SPORT_EVENTS.STATUS, StatusForEvent.finished.name())
                .where(Tables.SPORT_EVENTS.EVENT_ID.eq(eventID))
                .execute();
    }
}

