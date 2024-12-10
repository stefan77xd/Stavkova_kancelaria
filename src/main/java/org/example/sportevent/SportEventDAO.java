package org.example.sportevent;

import org.example.ConfigReader;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import static org.jooq.codegen.maven.example.Tables.*;

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


}

