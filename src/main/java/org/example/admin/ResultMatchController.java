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

    public void setSportEvent(SportEvent sportEvent) {
        this.sportEvent = sportEvent;
        eventName.setText(sportEvent.getEventName());
            List<String> resultNames = possibleOutcomeDAO.fetchAllWinningOutcomes( sportEvent.getEventId());

            for (String resultName : resultNames) {
                Label outcomeLabel = new Label(resultName);
                outcomeContainer.getChildren().add(outcomeLabel);
            }
    }
}
