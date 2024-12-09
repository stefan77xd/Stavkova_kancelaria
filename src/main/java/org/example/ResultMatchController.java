package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.SportEvent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;

public class ResultMatchController {

    @FXML
    private Label eventName;

    @FXML
    private VBox outcomeContainer;

    private SportEvent sportEvent;

    // Set the sport event, fetch outcomes and populate VBox
    public void setSportEvent(SportEvent sportEvent) {
        this.sportEvent = sportEvent;
        eventName.setText(sportEvent.getEventName());

        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);

            // Fetch all winning outcomes for the event
            List<String> resultNames = create.select(POSSIBLE_OUTCOMES.RESULT_NAME)
                    .from(POSSIBLE_OUTCOMES)
                    .where(POSSIBLE_OUTCOMES.EVENT_ID.eq((int) sportEvent.getEventId()))
                    .and(POSSIBLE_OUTCOMES.STATUS.eq(StatusForOutcomes.winning.name()))
                    .fetch(POSSIBLE_OUTCOMES.RESULT_NAME);

            // Dynamically add Labels for each result
            for (String resultName : resultNames) {
                Label outcomeLabel = new Label(resultName);
                outcomeContainer.getChildren().add(outcomeLabel);
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
