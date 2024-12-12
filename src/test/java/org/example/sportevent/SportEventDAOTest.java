package org.example.sportevent;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.junit.jupiter.api.*;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.sql.Connection;
import java.sql.SQLException;

import static org.jooq.codegen.maven.example.tables.SportEvents.SPORT_EVENTS;
import static org.junit.jupiter.api.Assertions.*;

class SportEventDAOTest {
    private DSLContext dslContext;
    private SportEventDAO sportEventDAO;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = createInMemoryDatabase();
        connection = dataSource.getConnection();
        dslContext = DSL.using(connection);
        sportEventDAO = new SportEventDAO(dslContext);
        createSportEventTable();  // Using the provided schema
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

    @Test
    void testCreateEvent() {
        String eventName = "Football Match";
        String startTime = "2024-12-12T15:00:00";
        String sportType = "Football";

        int eventId = sportEventDAO.createEvent(eventName, startTime, sportType);

        assertTrue(eventId > 0);

        SportEvent event = dslContext
                .select(SPORT_EVENTS.EVENT_NAME, SPORT_EVENTS.START_TIME, SPORT_EVENTS.SPORT_TYPE)
                .from(SPORT_EVENTS)
                .where(SPORT_EVENTS.EVENT_ID.eq(eventId))
                .fetchOneInto(SportEvent.class);

        assertEquals(eventName, event.getEventName());
        assertEquals(LocalDateTime.parse(startTime), event.getStartTime());
        assertEquals(sportType, event.getSportType());
    }

    @Test
    void testGetAllSportEvents() {
        sportEventDAO.createEvent("Football Match", "2024-12-12T15:00:00", "Football");
        sportEventDAO.createEvent("Basketball Game", "2024-12-15T17:00:00", "Basketball");

        var events = sportEventDAO.getAllSportEvents();

        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(event -> event.getEventName().equals("Football Match")));
        assertTrue(events.stream().anyMatch(event -> event.getEventName().equals("Basketball Game")));
    }

    @Test
    void testHideEvent() {
        int eventId = sportEventDAO.createEvent("Football Match", "2024-12-12T15:00:00", "Football");

        sportEventDAO.hideEvent(eventId);

        String visibility = dslContext
                .select(SPORT_EVENTS.VISIBILITY)
                .from(SPORT_EVENTS)
                .where(SPORT_EVENTS.EVENT_ID.eq(eventId))
                .fetchOneInto(String.class);

        assertEquals("hidden", visibility);
    }

    @Test
    void testUpdateEventStatus() {
        int eventId = sportEventDAO.createEvent("Football Match", "2024-12-12T15:00:00", "Football");

        sportEventDAO.updateEventStatus(eventId);

        String status = dslContext
                .select(SPORT_EVENTS.STATUS)
                .from(SPORT_EVENTS)
                .where(SPORT_EVENTS.EVENT_ID.eq(eventId))
                .fetchOneInto(String.class);

        assertEquals("finished", status);
    }

    @Test
    void testCreateEventWithInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportEventDAO.createEvent("", "2024-12-12T15:00:00", "Football");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            sportEventDAO.createEvent("Basketball Game", "", "Basketball");
        });

        assertThrows(IllegalArgumentException.class, () -> {
            sportEventDAO.createEvent("Basketball Game", "invalid-time", "Basketball");
        });
    }

    @Test
    void testGetAllSportEventsWithNoEvents() {
        var events = sportEventDAO.getAllSportEvents();
        assertTrue(events.isEmpty());
    }

    @Test
    void testCreateMultipleEvents() {
        int eventId1 = sportEventDAO.createEvent("Football Match", "2024-12-12T15:00:00", "Football");
        int eventId2 = sportEventDAO.createEvent("Basketball Game", "2024-12-15T17:00:00", "Basketball");

        var events = sportEventDAO.getAllSportEvents();

        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(event -> event.getEventId() == eventId1));
        assertTrue(events.stream().anyMatch(event -> event.getEventId() == eventId2));
    }

    @Test
    void testHideNonExistentEvent() {
        assertDoesNotThrow(() -> {
            sportEventDAO.hideEvent(999);
        });
    }

    @Test
    void testUpdateEventStatusWithNonExistentEvent() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportEventDAO.updateEventStatus(999);
        });
    }

    @Test
    void testCreateEventWithNullStartTime() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportEventDAO.createEvent("Football Match", null, "Football");
        });
    }

    @Test
    void testCreateEventWithNullSportType() {
        assertThrows(IllegalArgumentException.class, () -> {
            sportEventDAO.createEvent("Football Match", "2024-12-12T15:00:00", null);
        });
    }

    @Test
    void testGetAllSportEventsWithHiddenEvents() {
        int eventId1 = sportEventDAO.createEvent("Football Match", "2024-12-12T15:00:00", "Football");
        int eventId2 = sportEventDAO.createEvent("Basketball Game", "2024-12-15T17:00:00", "Basketball");

        sportEventDAO.hideEvent(eventId1);

        var events = sportEventDAO.getAllSportEvents();

        assertEquals(1, events.size());
        assertTrue(events.stream().anyMatch(event -> event.getEventId() == eventId2));
        assertFalse(events.stream().anyMatch(event -> event.getEventId() == eventId1));
    }
}


