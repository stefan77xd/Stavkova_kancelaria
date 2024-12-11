package org.example.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.Factory;
import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.sportevent.SportEvent;

import java.util.List;

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
