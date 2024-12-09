package org.example;

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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Data;
import lombok.Setter;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.SportEvent;
import org.example.sportevent.StatusForEvent;
import org.example.ticket.StatusForTicket;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import static org.jooq.codegen.maven.example.Tables.*;

public class EventPreviewController {

    SportEvent sportEvent;

    @FXML
    Label Name;

    @FXML
    private VBox checkboxContainer;

    @Setter
    public AdminController adminController;

    @FXML
    void Action(ActionEvent event) throws SQLException {
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);

            // Reset all outcomes to "loosing" first
            create.update(POSSIBLE_OUTCOMES)
                    .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.loosing.name())
                    .where(POSSIBLE_OUTCOMES.EVENT_ID.eq((int) sportEvent.getEventId()))
                    .execute();

            for (var node : checkboxContainer.getChildren()) {
                if (node instanceof HBox hBox) {
                    CheckBox checkBox = (CheckBox) hBox.getChildren().get(0);
                    Integer outcomeId = (Integer) checkBox.getUserData();

                    if (outcomeId != null && checkBox.isSelected()) {
                        // Update the selected outcome to "winning"
                        create.update(POSSIBLE_OUTCOMES)
                                .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.winning.name())
                                .where(POSSIBLE_OUTCOMES.OUTCOME_ID.eq(outcomeId))
                                .execute();
                    }
                }
            }

            // Update event status
            create.update(SPORT_EVENTS)
                    .set(SPORT_EVENTS.STATUS, StatusForEvent.finished.name())
                    .where(SPORT_EVENTS.EVENT_ID.eq((int) sportEvent.getEventId()))
                    .execute();

            // Fetch tickets related to the event
            var tickets = create.select(TICKETS.fields())
                    .from(TICKETS)
                    .join(POSSIBLE_OUTCOMES)
                    .on(TICKETS.OUTCOME_ID.eq(POSSIBLE_OUTCOMES.OUTCOME_ID))
                    .where(POSSIBLE_OUTCOMES.EVENT_ID.eq((int) sportEvent.getEventId()))
                    .fetchInto(TICKETS);

            // Collect affected user IDs
            Set<Integer> affectedUserIds = new HashSet<>();

            // Process each ticket based on outcome status
            for (var ticket : tickets) {
                var outcome = create.selectFrom(POSSIBLE_OUTCOMES)
                        .where(POSSIBLE_OUTCOMES.OUTCOME_ID.eq(ticket.getOutcomeId()))
                        .fetchOne();

                if (outcome == null) {
                    System.out.println("Outcome not found for ticket: " + ticket.getTicketId());
                    continue;
                }

                // Add the affected user to the set
                affectedUserIds.add(ticket.getUserId());

                // Determine if the outcome was winning or losing
                if (Objects.equals(outcome.getStatus(), StatusForOutcomes.winning.name())) {
                    // Update ticket status to "won" and adjust user's balance
                    create.update(TICKETS)
                            .set(TICKETS.STATUS, StatusForTicket.won.name())
                            .where(TICKETS.TICKET_ID.eq(ticket.getTicketId()))
                            .execute();

                    create.update(USERS)
                            .set(USERS.BALANCE, USERS.BALANCE.plus(ticket.getStake().multiply(outcome.getOdds())))
                            .where(USERS.USER_ID.eq(ticket.getUserId()))
                            .execute();
                } else {
                    // Update ticket status to "lost"
                    create.update(TICKETS)
                            .set(TICKETS.STATUS, StatusForTicket.lost.name())
                            .where(TICKETS.TICKET_ID.eq(ticket.getTicketId()))
                            .execute();
                }
            }

            // Call method to calculate and update user stats
            updateUserStats(affectedUserIds, create);

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

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }


    void updateUserStats(Set<Integer> userIds, DSLContext create) throws SQLException {
        for (Integer userId : userIds) {
            // Fetch all tickets for the user
            var userTickets = create.selectFrom(TICKETS)
                    .where(TICKETS.USER_ID.eq(userId))
                    .fetch();

            int totalTickets = userTickets.size();
            int wonTickets = 0;
            BigDecimal totalWinnings = BigDecimal.ZERO;

            for (var ticket : userTickets) {
                if (ticket.getStatus().equals(StatusForTicket.won.name())) {
                    wonTickets++;
                    var outcome = create.selectFrom(POSSIBLE_OUTCOMES)
                            .where(POSSIBLE_OUTCOMES.OUTCOME_ID.eq(ticket.getOutcomeId()))
                            .fetchOne();

                    totalWinnings = totalWinnings.add(ticket.getStake().multiply(outcome.getOdds()));
                }
            }

            // Calculate win rate
            double winRate = (totalTickets > 0) ? (double) wonTickets / totalTickets : 0.0;

            // Round win rate to 2 decimal places
            BigDecimal roundedWinRate = BigDecimal.valueOf(winRate).setScale(2, RoundingMode.HALF_UP);

            // Update the user with rounded win rate and total winnings
            create.update(USERS)
                    .set(USERS.WIN_RATE, roundedWinRate)
                    .set(USERS.TOTAL_WINNINGS, totalWinnings)
                    .where(USERS.USER_ID.eq(userId))
                    .execute();
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
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);

            // Fetch outcomes for the event
            var results = create.select(POSSIBLE_OUTCOMES.OUTCOME_ID, POSSIBLE_OUTCOMES.RESULT_NAME)
                    .from(POSSIBLE_OUTCOMES)
                    .where(POSSIBLE_OUTCOMES.EVENT_ID.eq(eventID))
                    .fetch();

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

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
