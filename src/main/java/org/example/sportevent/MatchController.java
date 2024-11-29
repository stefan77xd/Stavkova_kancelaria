package org.example.sportevent;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.possibleoutcome.PossibleOutcome;
import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.security.Auth;

import java.util.List;

public class MatchController {

    @FXML
    private Label eventNameLabel;

    @FXML
    private VBox outcomeVBox;  // VBox where outcome labels will be placed

    @FXML
    private long eventId;

    @FXML
    private Label userInfo;

    private PossibleOutcomeDAO possibleOutcomeDAO = new PossibleOutcomeDAO();

    public void setSportEvent(SportEvent sportEvent) {
        if (Auth.INSTANCE.getPrincipal() != null) {
            userInfo.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\n Zostatok: " + Auth.INSTANCE.getPrincipal().getBalance());
        }
        if (sportEvent != null) {
            eventNameLabel.setText(sportEvent.getEventName());
            eventId = sportEvent.getEventId();
            loadPossibleOutcomes();
        }
    }

    private void loadPossibleOutcomes() {
        // Clear any previous outcomes
        outcomeVBox.getChildren().clear();

        if (eventId > 0) {
            List<PossibleOutcome> possibleOutcomes = possibleOutcomeDAO.getPossibleOutcomesByEventId(eventId);
            possibleOutcomes.forEach(outcome -> {
                // Create a new label for each outcome
                Label outcomeLabel = new Label(outcome.getResultName() + " - Odds: " + outcome.getOdds());
                outcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");

                // Add the label to the VBox
                outcomeVBox.getChildren().add(outcomeLabel);
            });
        } else {
            System.out.println("Invalid eventId.");
        }
    }
}
