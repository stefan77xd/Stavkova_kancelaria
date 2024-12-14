package org.example.possibleoutcome;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.codegen.maven.example.tables.records.PossibleOutcomesRecord;
import org.junit.jupiter.api.*;
import org.sqlite.SQLiteDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;
import static org.junit.jupiter.api.Assertions.*;

class PossibleOutcomeDAOTest {

    private static Connection connection;
    private static DSLContext dslContext;
    private static PossibleOutcomeDAO possibleOutcomeDAO;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        SQLiteDataSource dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite::memory:");
        connection = dataSource.getConnection();
        dslContext = DSL.using(connection, SQLDialect.SQLITE);
        possibleOutcomeDAO = new PossibleOutcomeDAO(dslContext);

        dslContext.createTable(POSSIBLE_OUTCOMES)
                .column(POSSIBLE_OUTCOMES.OUTCOME_ID, POSSIBLE_OUTCOMES.OUTCOME_ID.getDataType())
                .column(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.EVENT_ID.getDataType())
                .column(POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.RESULT_NAME.getDataType())
                .column(POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.ODDS.getDataType())
                .column(POSSIBLE_OUTCOMES.STATUS, POSSIBLE_OUTCOMES.STATUS.getDataType())
                .constraints(
                        DSL.primaryKey(POSSIBLE_OUTCOMES.OUTCOME_ID)
                )
                .execute();
    }

    @AfterAll
    static void tearDownDatabase() throws SQLException {
        connection.close();
    }

    @BeforeEach
    void clearDatabase() {
        dslContext.deleteFrom(POSSIBLE_OUTCOMES).execute();
    }

    @Test
    void testGetPossibleOutcomesByEventId() {
        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                .values(1, "Win", 1.5, "upcoming")
                .values(1, "Lose", 2.5, "upcoming")
                .execute();

        List<PossibleOutcome> outcomes = possibleOutcomeDAO.getPossibleOutcomesByEventId(1);

        assertEquals(2, outcomes.size());
        assertEquals("Win", outcomes.get(0).getResultName());
        assertEquals("Lose", outcomes.get(1).getResultName());
    }

    @Test
    void testFetchAllWinningOutcomes() {
        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                .values(1, "Win", 1.5, "winning")
                .values(1, "Lose", 2.5, "loosing")
                .execute();

        List<String> winningOutcomes = possibleOutcomeDAO.fetchAllWinningOutcomes(1);

        assertEquals(1, winningOutcomes.size());
        assertEquals("Win", winningOutcomes.get(0));
    }

    @Test
    void testCreatePossibleOutcome() {
        possibleOutcomeDAO.createPossibleOutcome(1, "Win", "1.5");

        PossibleOutcomesRecord record = dslContext.selectFrom(POSSIBLE_OUTCOMES).fetchOne();
        assertNotNull(record);
        assertEquals(1, record.getEventId());
        assertEquals("Win", record.getResultName());
        assertEquals(1.5, record.getOdds());
        assertEquals("upcoming", record.getStatus());
    }

    @Test
    void testSetOutcomesToLoosing() {
        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                .values(1, "Win", 1.5, "upcoming")
                .execute();

        possibleOutcomeDAO.setOutcomesToLoosing(1);

        PossibleOutcomesRecord record = dslContext.selectFrom(POSSIBLE_OUTCOMES).fetchOne();
        assertEquals("loosing", record.getStatus());
    }

    @Test
    void testSetOutcomeToWinning() {
        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .columns(POSSIBLE_OUTCOMES.OUTCOME_ID, POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                .values(1, 1, "Win", 1.5, "upcoming")
                .execute();

        possibleOutcomeDAO.setOutcomeToWinning(1);

        PossibleOutcomesRecord record = dslContext.selectFrom(POSSIBLE_OUTCOMES).fetchOne();
        assertEquals("winning", record.getStatus());
    }

    @Test
    void testProcessTicket() {
        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .columns(POSSIBLE_OUTCOMES.OUTCOME_ID, POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                .values(1, 1, "Win", 1.5, "upcoming")
                .execute();

        PossibleOutcomesRecord record = possibleOutcomeDAO.processTicket(1);

        assertNotNull(record);
        assertEquals(1, record.getOutcomeId());
        assertEquals("Win", record.getResultName());
    }

    @Test
    void testNoWinningOutcomes() {
        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                .values(1, "Lose", 2.5, "loosing")
                .execute();

        List<String> winningOutcomes = possibleOutcomeDAO.fetchAllWinningOutcomes(1);

        assertTrue(winningOutcomes.isEmpty());
    }

    @Test
    void testInvalidOutcomeId() {
        assertThrows(IllegalArgumentException.class, () -> possibleOutcomeDAO.setOutcomeToWinning(999));
    }

    @Test
    void testMultipleWinningOutcomes() {
        dslContext.insertInto(POSSIBLE_OUTCOMES)
                .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                .values(1, "Win", 1.5, "winning")
                .values(1, "Another Win", 1.8, "winning")
                .execute();

        List<String> winningOutcomes = possibleOutcomeDAO.fetchAllWinningOutcomes(1);

        assertEquals(2, winningOutcomes.size());
        assertTrue(winningOutcomes.contains("Win"));
        assertTrue(winningOutcomes.contains("Another Win"));
    }
}
