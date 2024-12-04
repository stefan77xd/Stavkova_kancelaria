package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.SportEvent;
import org.example.sportevent.StatusForEvent;
import org.example.ticket.StatusForTicket;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.*;

public class EventPreviewController {
    SportEvent sportEvent;
    @FXML
    Text Name;


    @FXML
    CheckBox checkbox1;

    @FXML
    CheckBox checkbox2;

    @FXML
    CheckBox checkbox3;


    @FXML
    void Action(ActionEvent event) throws SQLException {
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);

            // Aktualizácia výsledkov pre POSSIBLE_OUTCOMES
            if (checkbox1.isSelected()) {
                create.update(POSSIBLE_OUTCOMES)
                        .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.winning.name())
                        .where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox1.getText()))
                        .execute();
            } else if (!checkbox1.getText().isEmpty()) {
                create.update(POSSIBLE_OUTCOMES)
                        .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.loosing.name())
                        .where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox1.getText()))
                        .execute();
            }

            if (checkbox2.isSelected()) {
                create.update(POSSIBLE_OUTCOMES)
                        .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.winning.name())
                        .where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox2.getText()))
                        .execute();
            } else if (!checkbox2.getText().isEmpty()) {
                create.update(POSSIBLE_OUTCOMES)
                        .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.loosing.name())
                        .where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox2.getText()))
                        .execute();
            }

            if (checkbox3.isSelected()) {
                create.update(POSSIBLE_OUTCOMES)
                        .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.winning.name())
                        .where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox3.getText()))
                        .execute();
            } else if (!checkbox3.getText().isEmpty()) {
                create.update(POSSIBLE_OUTCOMES)
                        .set(POSSIBLE_OUTCOMES.STATUS, StatusForOutcomes.loosing.name())
                        .where(POSSIBLE_OUTCOMES.RESULT_NAME.eq(checkbox3.getText()))
                        .execute();
            }

            // Aktualizácia statusu SPORT_EVENTS
            create.update(SPORT_EVENTS)
                    .set(SPORT_EVENTS.STATUS, StatusForEvent.finished.name())
                    .where(SPORT_EVENTS.EVENT_ID.eq((int) sportEvent.getEventId()))
                    .execute();

            // Získanie všetkých tiketov spojených s daným eventom cez OUTCOME
            var tickets = create.select(TICKETS.fields())
                    .from(TICKETS)
                    .join(POSSIBLE_OUTCOMES)
                    .on(TICKETS.OUTCOME_ID.eq(POSSIBLE_OUTCOMES.OUTCOME_ID))
                    .where(POSSIBLE_OUTCOMES.EVENT_ID.eq((int) sportEvent.getEventId()))
                    .fetchInto(TICKETS);

            for (var ticket : tickets) {
                var outcome = create.selectFrom(POSSIBLE_OUTCOMES)
                        .where(POSSIBLE_OUTCOMES.OUTCOME_ID.eq(ticket.getOutcomeId()))
                        .fetchOne();

                if (outcome == null) {
                    System.out.println("Outcome not found for ticket: " + ticket.getTicketId());
                    continue;
                }

                // Zisti, či outcome vyhral alebo prehral
                if (Objects.equals(outcome.getStatus(), StatusForOutcomes.winning.name())) {
                    // Nastav tiket ako výherný a pridaj výhru do používateľovho zostatku
                    create.update(TICKETS)
                            .set(TICKETS.STATUS, StatusForTicket.won.name())
                            .where(TICKETS.TICKET_ID.eq(ticket.getTicketId()))
                            .execute();

                    create.update(USERS)
                            .set(USERS.BALANCE, USERS.BALANCE.plus(ticket.getStake().multiply(outcome.getOdds())))
                            .where(USERS.USER_ID.eq(ticket.getUserId()))
                            .execute();
                } else {

                    create.update(TICKETS)
                            .set(TICKETS.STATUS, StatusForTicket.lost.name())
                            .where(TICKETS.TICKET_ID.eq(ticket.getTicketId()))
                            .execute();
                }
            }
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Výborne");
        alert.setHeaderText("Event bol vyhodnoteny.");
        alert.showAndWait();
    }


}
