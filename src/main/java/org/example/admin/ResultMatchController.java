package org.example.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.ConfigReader;
import org.example.Factory;
import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
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

    private final PossibleOutcomeDAO possibleOutcomeDAO = Factory.INSTANCE.getPossibleOutcomeDAO();

    // Set the sport event, fetch outcomes and populate VBox
    public void setSportEvent(SportEvent sportEvent) {
        this.sportEvent = sportEvent;
        eventName.setText(sportEvent.getEventName());
            // Fetch all winning outcomes for the event
            List<String> resultNames = possibleOutcomeDAO.fetchAllWinningOutcomes((int) sportEvent.getEventId());

            // Dynamically add Labels for each result
            for (String resultName : resultNames) {
                Label outcomeLabel = new Label(resultName);
                outcomeContainer.getChildren().add(outcomeLabel);
            }
    }
}
