package org.example.sportevent;

import org.example.ConfigReader;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.codegen.maven.example.Tables;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.jooq.codegen.maven.example.Tables.*;
import static org.jooq.codegen.maven.example.tables.SportEvents.SPORT_EVENTS;

public class SportEventDAO {
    private final DSLContext dslContext;

    public SportEventDAO(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<SportEvent> getAllSportEvents() {
        List<SportEvent> events = new ArrayList<>();
        // Fetch data using the shared DSLContext
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
        return dslContext.insertInto(Tables.SPORT_EVENTS)
                .set(Tables.SPORT_EVENTS.EVENT_NAME, eventName)
                .set(Tables.SPORT_EVENTS.START_TIME, LocalDateTime.parse(startTime))
                .set(Tables.SPORT_EVENTS.SPORT_TYPE, sportType)
                .set(Tables.SPORT_EVENTS.STATUS, StatusForEvent.upcoming.name())
                .returning(Tables.SPORT_EVENTS.EVENT_ID)
                .fetchOne()
                .getValue(Tables.SPORT_EVENTS.EVENT_ID);
    }

    public void updateEventStatus(int eventID) {
        dslContext.update(Tables.SPORT_EVENTS)
                .set(Tables.SPORT_EVENTS.STATUS, StatusForEvent.finished.name())
                .where(Tables.SPORT_EVENTS.EVENT_ID.eq(eventID))
                .execute();
    }


}

