package org.example.admin;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.ConfigReader;
import org.example.Factory;
import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;
import org.example.ticket.StatusForTicket;
import org.example.ticket.Ticket;
import org.example.ticket.TicketDAO;
import org.example.user.UserDAO;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.codegen.maven.example.tables.records.TicketsRecord;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.jooq.codegen.maven.example.Tables.*;

public class EventPreviewController {

    SportEvent sportEvent;

    @FXML
    Label Name;

    @FXML
    private VBox checkboxContainer;

    @Setter
    public AdminController adminController;

    private final PossibleOutcomeDAO possibleOutcomeDAO = Factory.INSTANCE.getPossibleOutcomeDAO();
    private final SportEventDAO sportEventDAO = Factory.INSTANCE.getSportEventDAO();
    private final TicketDAO ticketDAO = Factory.INSTANCE.getTicketDAO();
    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();

    @FXML
    void Action(ActionEvent event) throws SQLException {

            possibleOutcomeDAO.setOutcomesToLoosing((int) sportEvent.getEventId());

            for (var node : checkboxContainer.getChildren()) {
                if (node instanceof HBox hBox) {
                    CheckBox checkBox = (CheckBox) hBox.getChildren().get(0);
                    Integer outcomeId = (Integer) checkBox.getUserData();

                    if (outcomeId != null && checkBox.isSelected()) {
                        possibleOutcomeDAO.setOutcomeToWinning(outcomeId);
                    }
                }
            }

            sportEventDAO.updateEventStatus((int) sportEvent.getEventId());


            var tickets = ticketDAO.fetchTicketsRealtedToEvent((int) sportEvent.getEventId());

            // Collect affected user IDs
            Set<Integer> affectedUserIds = new HashSet<>();

            // Process each ticket based on outcome status
            for (var ticket : tickets) {
                var outcome = possibleOutcomeDAO.processTicket(ticket.getOutcomeId());

                if (outcome == null) {
                    System.out.println("Outcome not found for ticket: " + ticket.getTicketId());
                    continue;
                }

                // Add the affected user to the set
                affectedUserIds.add(ticket.getUserId());

                // Determine if the outcome was winning or losing
                if (Objects.equals(outcome.getStatus(), StatusForOutcomes.winning.name())) {
                    ticketDAO.updateTicketStatusToWon(ticket.getTicketId());

                    userDAO.updateBalanceWithTicket(ticket.getStake(), outcome.getOdds(), ticket.getUserId());

                } else {
                    ticketDAO.updateTicketStatusToLost(ticket.getTicketId());
                }
            }

            // Call method to calculate and update user stats
            updateUserStats(affectedUserIds);

            // Show success alert and close window after alert is dismissed
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Process Completed");
                alert.setHeaderText(null);
                alert.setContentText("Výsledky boli úspešne zapísané!");
                alert.getDialogPane().setStyle("-fx-background-color: #303030;"); // Style the background color of the dialog

                // Style the content text to be white
                alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: white;");

                // Style the header and label (if header exists)
                alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        Node header = alert.getDialogPane().lookup(".header");
                        if (header != null) {
                            header.setStyle("-fx-background-color: #303030;");
                        }

                        Node label = alert.getDialogPane().lookup(".header .label");
                        if (label != null) {
                            label.setStyle("-fx-text-fill: white;");
                        }
                    }
                });


                Stage stage1 = (Stage) alert.getDialogPane().getScene().getWindow();
                stage1.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/success.png"))));

                // Wait for the alert to close, then close the current window and call updateTabs
                alert.showAndWait().ifPresent(response -> {
                    // Close the current window
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                    adminController.updateTabs();
                });
            });
    }


    void updateUserStats(Set<Integer> userIds) throws SQLException {
        for (Integer userId : userIds) {

            var userTickets = ticketDAO.fetchTicketsForUser(userId);

            int totalTickets = userTickets.size();
            int wonTickets = 0;
            BigDecimal totalWinnings = BigDecimal.ZERO;

            for (var ticket : userTickets) {
                if (ticket.getStatus().equals(StatusForTicket.won.name())) {
                    wonTickets++;
                    var outcome = possibleOutcomeDAO.getTicketOutcome(ticket.getOutcomeId());

                    totalWinnings = totalWinnings.add(ticket.getStake().multiply(outcome.getOdds()));
                }
            }

            // Calculate win rate
            double winRate = (totalTickets > 0) ? (double) wonTickets / totalTickets : 0.0;

            // Round win rate to 2 decimal places
            BigDecimal roundedWinRate = BigDecimal.valueOf(winRate).setScale(2, RoundingMode.HALF_UP);

            userDAO.updateWinRateAndTotalWinnings(roundedWinRate, totalWinnings, userId);
        }
    }


    public void setSportEvent(SportEvent sportEvent) {
        this.sportEvent = sportEvent;
        Name.setText(sportEvent.getEventName());
        loadPossibleOutcomes();
    }

    private void loadPossibleOutcomes() {
        if (sportEvent == null) {
            System.err.println("SportEvent is null. Cannot load outcomes.");
            return;
        }

        int eventID = (int) sportEvent.getEventId();
        var results = possibleOutcomeDAO.fetchOutcomesForEvent(eventID);

        // Clear existing checkboxes
        checkboxContainer.getChildren().clear();

        // Dynamically create checkboxes for each possible outcome
        for (var record : results) {
            HBox hBox = new HBox(10);
            CheckBox checkBox = new CheckBox();
            checkBox.setUserData(record.get(POSSIBLE_OUTCOMES.OUTCOME_ID)); // Store outcome ID
            Label label = new Label(record.get(POSSIBLE_OUTCOMES.RESULT_NAME));

            hBox.getChildren().addAll(checkBox, label);
            checkboxContainer.getChildren().add(hBox);
        }


    }
}
