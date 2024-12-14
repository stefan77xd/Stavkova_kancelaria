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
import org.example.Factory;
import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.ticket.StatusForTicket;
import org.example.ticket.TicketDAO;
import org.example.user.UserDAO;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;

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
    void Action(ActionEvent event) {
            possibleOutcomeDAO.setOutcomesToLoosing(sportEvent.getEventId());
            for (var node : checkboxContainer.getChildren()) {
                if (node instanceof HBox hBox) {
                    CheckBox checkBox = (CheckBox) hBox.getChildren().get(0);
                    Integer outcomeId = (Integer) checkBox.getUserData();
                    if (outcomeId != null && checkBox.isSelected()) {
                        possibleOutcomeDAO.setOutcomeToWinning(outcomeId);
                    }
                }
            }
            sportEventDAO.updateEventStatus(sportEvent.getEventId());
            var tickets = ticketDAO.fetchTicketsRelatedToEvent(sportEvent.getEventId());

            Set<Integer> affectedUserIds = new HashSet<>();

            for (var ticket : tickets) {
                var outcome = possibleOutcomeDAO.processTicket(ticket.getOutcomeId());
                if (outcome == null) {
                    continue;
                }
                affectedUserIds.add(ticket.getUserId());

                if (Objects.equals(outcome.getStatus(), StatusForOutcomes.winning.name())) {
                    ticketDAO.updateTicketStatusToWon(ticket.getTicketId());
                    userDAO.updateBalanceWithTicket(ticket.getStake(), outcome.getOdds(), ticket.getUserId());
                } else {
                    ticketDAO.updateTicketStatusToLost(ticket.getTicketId());
                }
            }

            updateUserStats(affectedUserIds);

            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Proces bol vykonaný");
                alert.setHeaderText(null);
                alert.setContentText("Výsledky boli úspešne zapísané!");
                alert.getDialogPane().setStyle("-fx-background-color: #303030;");

                alert.getDialogPane().lookup(".content").setStyle("-fx-text-fill: white;");

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

                alert.showAndWait().ifPresent(response -> {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close();
                    adminController.updateTabs();
                });
            });
    }


    public void updateUserStats(Set<Integer> userIds) {
        for (Integer userId : userIds) {
            var userTickets = ticketDAO.fetchTicketsForUser(userId);

            double totalTickets = userTickets.size();
            double wonTickets = 0.0;
            double totalWinnings = 0.0;
            for (var ticket : userTickets) {
                if (ticket.getStatus().equals(StatusForTicket.won.name())) {
                    wonTickets++;
                    var outcome = possibleOutcomeDAO.getTicketOutcome(ticket.getOutcomeId());
                    totalWinnings += ticket.getStake() * outcome.getOdds();
                }
            }
            double winRate = (totalTickets > 0) ? wonTickets/totalTickets : 0.0;
            Double roundedWinRate = Math.round(winRate * 100.0) / 100.0;
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
            System.err.println("SportEvent je null.");
            return;
        }

        int eventID = sportEvent.getEventId();
        var results = possibleOutcomeDAO.fetchOutcomesForEvent(eventID);
        checkboxContainer.getChildren().clear();

        for (var record : results) {
            HBox hBox = new HBox(10);
            CheckBox checkBox = new CheckBox();
            checkBox.setUserData(record.get(POSSIBLE_OUTCOMES.OUTCOME_ID));
            Label label = new Label(record.get(POSSIBLE_OUTCOMES.RESULT_NAME));
            hBox.getChildren().addAll(checkBox, label);
            checkboxContainer.getChildren().add(hBox);
        }
    }
}
