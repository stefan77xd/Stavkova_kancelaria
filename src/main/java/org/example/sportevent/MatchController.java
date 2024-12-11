package org.example.sportevent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.Controller;
import org.example.Factory;
import org.example.possibleoutcome.PossibleOutcome;
import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.security.Auth;
import org.example.security.Principal;
import org.example.ticket.TicketDAO;
import org.example.user.UserDAO;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class MatchController {

    @FXML
    private Label eventNameLabel;

    @FXML
    private VBox outcomeVBox;
    @FXML
    private long eventId;
    @FXML
    private Label userInfo;
    PossibleOutcome selectedOutcome;

    private SportEvent sportEvent;


    private final PossibleOutcomeDAO possibleOutcomeDAO = Factory.INSTANCE.getPossibleOutcomeDAO();

    private final TicketDAO ticketDAO = Factory.INSTANCE.getTicketDAO();

    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();

    @Setter
    private Controller mainController;

    public void setSportEvent(SportEvent sportEvent) {
        this.sportEvent = sportEvent;
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
            loadPossibleOutcomes(sportEvent.getStatus().equals(StatusForEvent.finished));
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
                      selectedOutcome = outcome;
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

                // Ensure only two digits after the decimal point
                if (newValue.contains(".") && newValue.substring(newValue.indexOf(".") + 1).length() > 2) {
                    betAmountField.setText(oldValue);  // Restore the old value if there are more than two digits after the decimal point
                }

                updateEventualWin(betAmountField.getText(), selectedOdds.get(), eventualWinLabel);
            });

            outcomeVBox.getChildren().add(inputRow);

            HBox buttonRow = new HBox();
            buttonRow.setSpacing(10);
            buttonRow.setAlignment(Pos.CENTER);

            Button placeBetButton = new Button("Staviť");
            placeBetButton.setPrefWidth(fixedWidth);
            placeBetButton.setStyle("-fx-font-size: 16; -fx-cursor: hand;");
            placeBetButton.getStyleClass().add("bet-button");

            placeBetButton.setOnAction(event -> {
                    if (selectedOdds.get()!= 0 && !betAmountField.getText().isEmpty() && !betAmountField.getText().equals(".")) {
                        Double betAmount = Double.valueOf(betAmountField.getText()); // Replace with your actual bet amount
                        Double balance = Auth.INSTANCE.getPrincipal().getBalance();
                        if (betAmount <= balance && betAmount >= 0) {
                            if (!LocalDateTime.now().isAfter(sportEvent.getStartTime())) {
                                try {
                                    placeBet(betAmount);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                showAlert("Zápas už začal");
                            }
                        } else {
                            showAlert("Nedostatok prostriedkov");
                        }
                    }
            });

            if (Auth.INSTANCE.getPrincipal() == null) {
                placeBetButton.setDisable(true);
            }

            buttonRow.getChildren().add(placeBetButton);
            outcomeVBox.getChildren().add(buttonRow);
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

    private void placeBet(Double betAmount) throws SQLException {
        Principal principal = Auth.INSTANCE.getPrincipal();
        Integer userID = principal.getId();

            userDAO.updateBalanceAndStat(userID, betAmount);
            userDAO.updateStatistics();
            ticketDAO.insertTicket(userID, (int) selectedOutcome.getOutcomeId(), betAmount);

        Auth.INSTANCE.getPrincipal().setBalance(Auth.INSTANCE.getPrincipal().getBalance() - betAmount);
        userInfo.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\n Zostatok: " + Auth.INSTANCE.getPrincipal().getBalance());
        if (mainController != null) {
            mainController.updateBalance();
        }
    }



    private void updateEventualWin(String betAmount, double odds, Label eventualWinLabel) {
        try {
            double bet = Double.parseDouble(betAmount);
            double eventualWin = bet * odds;
            eventualWinLabel.setText(String.format("%.2f", eventualWin));
        } catch (NumberFormatException e) {
            eventualWinLabel.setText("0.0");
        }
    }

    private void showAlert(String text) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Pozor!");
        alert.setHeaderText(text);
        alert.getDialogPane().setStyle("-fx-background-color: #303030;");
        alert.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
            }
        });
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/warning.png"))));
        alert.showAndWait();
    }
}
