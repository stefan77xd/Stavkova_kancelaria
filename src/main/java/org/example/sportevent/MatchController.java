package org.example.sportevent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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
        if (Auth.INSTANCE.getPrincipal() != null && !sportEvent.getStatus().equals(StatusForEvent.finished)) {
            userInfo.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\n Zostatok: " + Auth.INSTANCE.getPrincipal().getBalance());
        }
        if (sportEvent != null) {
            // Set event name and make it responsive
            eventNameLabel.setText(sportEvent.getEventName());
            eventNameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14;"); // Adjust font size if needed
            eventNameLabel.setWrapText(true);  // Allow text to wrap if too long

            eventId = sportEvent.getEventId();

            // Check if the event is finished
            if (sportEvent.getStatus().equals(StatusForEvent.finished)) {
                loadPossibleOutcomes(true);
            } else {
                loadPossibleOutcomes(false);
            }
        }
    }



    private void loadPossibleOutcomes(Boolean finished) {
        // Clear any previous outcomes
        outcomeVBox.getChildren().clear();

        // Set alignment of VBox to center the content vertically and horizontally
        outcomeVBox.setAlignment(Pos.CENTER);
        outcomeVBox.setSpacing(10);  // Space between each row (HBox)

        if (eventId > 0) {
            List<PossibleOutcome> possibleOutcomes = possibleOutcomeDAO.getPossibleOutcomesByEventId(eventId);

            // Define a fixed width for all elements (adjust this value as needed)
            double fixedWidth = 200;
            double maxWidth = 250;  // Max width for the outcome label

            // Variables to hold selected odds and bet amount
            final DoubleProperty selectedOdds = new SimpleDoubleProperty(0.0);
            final TextField betAmountField = new TextField();
            final Label eventualWinLabel = new Label("0.0");

            // HBox to show selected odds and eventual win
            HBox oddsWinRow = new HBox();
            oddsWinRow.setSpacing(10);
            oddsWinRow.setAlignment(Pos.CENTER);
            oddsWinRow.setVisible(false);  // Initially hidden

            Label selectedOddsLabel = new Label("Kurz: ");
            Label oddsValueLabel = new Label();
            oddsValueLabel.textProperty().bind(selectedOdds.asString("%.2f"));

            oddsWinRow.getChildren().addAll(selectedOddsLabel, oddsValueLabel, new Label("EV. Výhra: "), eventualWinLabel);

            // Add all outcomes with buttons
            ToggleGroup toggleGroup = new ToggleGroup(); // To ensure single selection

            for (PossibleOutcome outcome : possibleOutcomes) {
                // Create an HBox for each outcome
                HBox outcomeRow = new HBox();
                outcomeRow.setSpacing(10);
                outcomeRow.setAlignment(Pos.CENTER);  // Center label and button horizontally in HBox

                // Create a label for the resultName
                Label outcomeLabel = new Label(outcome.getResultName());
                outcomeLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16;");
                outcomeLabel.setMaxWidth(maxWidth);  // Set max width
                outcomeLabel.setWrapText(true);  // Allow text to wrap if too long

                // Create a button for the odds
                ToggleButton oddsButton = new ToggleButton(String.valueOf(outcome.getOdds()));
                oddsButton.getStyleClass().add("odds-button");
                oddsButton.setPrefWidth(100);
                if (finished) {
                    oddsButton.setDisable(true);
                }  // Set a fixed width for the button
                oddsButton.setToggleGroup(toggleGroup);  // Ensure single selection

                // Handle selection of odds
                oddsButton.setOnAction(event -> {
                    selectedOdds.set(outcome.getOdds());
                    if (!finished) {
                        oddsWinRow.setVisible(true);  // Show the HBox with odds and eventual win
                    }
                    updateEventualWin(betAmountField.getText(), selectedOdds.get(), eventualWinLabel);
                });

                // Add label and button to the HBox
                outcomeRow.getChildren().addAll(outcomeLabel, oddsButton);

                // Add the HBox to the VBox
                outcomeVBox.getChildren().add(outcomeRow);
            }

            // Create an HBox for the input field (bet amount)
            HBox inputRow = new HBox();
            inputRow.setSpacing(10);
            inputRow.setAlignment(Pos.CENTER);

            // TextField for bet input
            betAmountField.setPromptText("Vklad");
            betAmountField.setPrefWidth(fixedWidth);  // Set width to match label + button width
            inputRow.getChildren().add(betAmountField);

            // Add listener to update eventual win when the bet amount is changed
            betAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
                // Allow digits and only one decimal point
                if (!newValue.matches("\\d*\\.?\\d*")) {
                    betAmountField.setText(newValue.replaceAll("[^\\d.]", ""));
                }

                // Ensure only one decimal point is allowed
                if (newValue.indexOf('.') != newValue.lastIndexOf('.')) {
                    betAmountField.setText(oldValue);  // Restore the old value if more than one decimal point
                }

                updateEventualWin(betAmountField.getText(), selectedOdds.get(), eventualWinLabel);
            });



            // Add the input field row to the VBox
            outcomeVBox.getChildren().add(inputRow);

            // Create an HBox for the 'Place Bet' button
            HBox buttonRow = new HBox();
            buttonRow.setSpacing(10);
            buttonRow.setAlignment(Pos.CENTER);

            // Create a 'Place Bet' button
            Button placeBetButton = new Button("Staviť");
            placeBetButton.setPrefWidth(fixedWidth);  // Set width to match label + button width
            placeBetButton.setStyle("-fx-font-size: 16; -fx-cursor: hand;");
            placeBetButton.getStyleClass().add("bet-button");

            if (Auth.INSTANCE.getPrincipal() == null) {
                placeBetButton.setDisable(true);
            }

            // Add the button to the HBox
            buttonRow.getChildren().add(placeBetButton);

            // Add the button row to the VBox
            outcomeVBox.getChildren().add(buttonRow);

            // Add the odds and eventual win row to the VBox (initially hidden)
            outcomeVBox.getChildren().add(oddsWinRow);

            if (finished) {
                betAmountField.setVisible(false);
                placeBetButton.setVisible(false);
                oddsWinRow.setVisible(false);
            }

        } else {
            System.out.println("Invalid eventId.");
        }
    }


    // Helper method to update eventual win calculation
    private void updateEventualWin(String betAmount, double odds, Label eventualWinLabel) {
        try {
            double bet = Double.parseDouble(betAmount);
            double eventualWin = bet * odds;
            eventualWinLabel.setText(String.format("%.2f", eventualWin));
        } catch (NumberFormatException e) {
            eventualWinLabel.setText("0.0");  // Set to 0 if input is invalid
        }
    }

}
