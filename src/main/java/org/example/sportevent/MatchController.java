package org.example.sportevent;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.AlertFactory;
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
    private final AlertFactory A = new AlertFactory();

    public void setSportEvent(SportEvent sportEvent) {
        this.sportEvent = sportEvent;
        if (Auth.INSTANCE.getPrincipal() != null && !sportEvent.getStatus().equals(StatusForEvent.finished)) {
            userInfo.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\n Zostatok: " + Auth.INSTANCE.getPrincipal().getBalance());
        }
        if (sportEvent != null) {
            eventNameLabel.setText(sportEvent.getEventName());
            eventNameLabel.setStyle("-fx-font-size: 14;");
            eventNameLabel.setWrapText(true);
            eventId = sportEvent.getEventId();
            loadPossibleOutcomes(sportEvent.getStatus().equals(StatusForEvent.finished));
        }
    }

    private void loadPossibleOutcomes(Boolean finished) {
        outcomeVBox.getChildren().clear();
        outcomeVBox.setAlignment(Pos.CENTER);
        outcomeVBox.setSpacing(10);

        if (eventId > 0) {
            List<PossibleOutcome> possibleOutcomes = possibleOutcomeDAO.getPossibleOutcomesByEventId(eventId);
            double fixedWidth = 200;
            double maxWidth = 250;

            final DoubleProperty selectedOdds = new SimpleDoubleProperty(0.0);
            final TextField betAmountField = new TextField();
            final Label eventualWinLabel = new Label("0.0");

            HBox oddsWinRow = new HBox();
            oddsWinRow.setSpacing(10);
            oddsWinRow.setAlignment(Pos.CENTER);
            oddsWinRow.setVisible(false);

            Label selectedOddsLabel = new Label("Kurz: ");
            Label oddsValueLabel = new Label();
            oddsValueLabel.textProperty().bind(selectedOdds.asString("%.2f"));

            oddsWinRow.getChildren().addAll(selectedOddsLabel, oddsValueLabel, new Label("EV. Výhra: "), eventualWinLabel);

            ToggleGroup toggleGroup = new ToggleGroup();
            for (PossibleOutcome outcome : possibleOutcomes) {
                HBox outcomeRow = new HBox();
                outcomeRow.setSpacing(10);
                outcomeRow.setAlignment(Pos.CENTER);

                Label outcomeLabel = new Label(outcome.getResultName());
                outcomeLabel.setStyle("-fx-font-size: 16;");
                outcomeLabel.setMaxWidth(maxWidth);
                outcomeLabel.setWrapText(true);

                ToggleButton oddsButton = new ToggleButton(String.valueOf(outcome.getOdds()));
                oddsButton.getStyleClass().add("odds-button");
                oddsButton.setPrefWidth(100);
                if (finished) {
                    oddsButton.setDisable(true);
                }
                oddsButton.setToggleGroup(toggleGroup);

                oddsButton.setOnAction(event -> {
                    selectedOdds.set(outcome.getOdds());
                    if (!finished) {
                        oddsWinRow.setVisible(true);
                    }
                    selectedOutcome = outcome;
                    updateEventualWin(betAmountField.getText(), selectedOdds.get(), eventualWinLabel);
                });
                outcomeRow.getChildren().addAll(outcomeLabel, oddsButton);
                outcomeVBox.getChildren().add(outcomeRow);
            }
            HBox inputRow = new HBox();
            inputRow.setSpacing(10);
            inputRow.setAlignment(Pos.CENTER);

            betAmountField.setPromptText("Vklad");
            betAmountField.setPrefWidth(fixedWidth);
            inputRow.getChildren().add(betAmountField);

            betAmountField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*\\.?\\d*")) {
                    betAmountField.setText(newValue.replaceAll("[^\\d.]", ""));
                }
                if (newValue.indexOf('.') != newValue.lastIndexOf('.')) {
                    betAmountField.setText(oldValue);
                }
                if (newValue.contains(".") && newValue.substring(newValue.indexOf(".") + 1).length() > 2) {
                    betAmountField.setText(oldValue);
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
                        Double betAmount = Double.valueOf(betAmountField.getText());
                        Double balance = Auth.INSTANCE.getPrincipal().getBalance();
                        if (betAmount <= balance && betAmount >= 0) {
                            if (!LocalDateTime.now().isAfter(sportEvent.getStartTime())) {
                                try {
                                    placeBet(betAmount);
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                A.showAlert("Chyba", "Zápas sa už začal.", "warning", Alert.AlertType.WARNING);
                            }
                        } else {
                            A.showAlert("Chyba", "Nedostatok prostriedkov.", "warning", Alert.AlertType.WARNING);
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
        Stage stage = (Stage) userInfo.getScene().getWindow();
        stage.close();
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
}
