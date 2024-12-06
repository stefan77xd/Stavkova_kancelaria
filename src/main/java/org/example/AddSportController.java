package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.sportevent.StatusForEvent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;
import static org.jooq.codegen.maven.example.Tables.SPORT_EVENTS;

public class AddSportController {

    @FXML
    private DatePicker DatePicker;

    @FXML
    private TextField eventName;

    @FXML
    private TextField odds1;

    @FXML
    private TextField odds2;

    @FXML
    private TextField odds3;

    @FXML
    private TextField resultName1;

    @FXML
    private TextField resultName2;

    @FXML
    private TextField resultName3;

    @FXML
    private TextField sportType;

    @FXML
    private TextField time;

    @Setter
    public AdminController adminController;


    @FXML
    void Add(ActionEvent event) {
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);


            LocalDate date = DatePicker.getValue();
            if (date == null) {
                throw new IllegalArgumentException("Dátum nesmie byť prázdny.");
            }

            LocalTime parsedTime = parseTime(time.getText());
            String startTime = date.atTime(parsedTime).toString();


            create.insertInto(SPORT_EVENTS)
                    .columns(SPORT_EVENTS.EVENT_NAME, SPORT_EVENTS.START_TIME, SPORT_EVENTS.SPORT_TYPE, SPORT_EVENTS.STATUS)
                    .values(eventName.getText(), LocalDateTime.parse(startTime), sportType.getText(), StatusForEvent.upcoming.name())
                    .execute();
            int eventId = create.fetchOne("SELECT last_insert_rowid()").into(int.class);
            if (isNotEmpty(resultName1.getText()) && isNotEmpty(odds1.getText())) {
                create.insertInto(POSSIBLE_OUTCOMES)
                        .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                        .values(eventId, resultName1.getText(), BigDecimal.valueOf(Double.parseDouble(odds1.getText())), StatusForOutcomes.upcoming.name())
                        .execute();
            }
            if (isNotEmpty(resultName2.getText()) && isNotEmpty(odds2.getText())) {
                create.insertInto(POSSIBLE_OUTCOMES)
                        .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                        .values(eventId, resultName2.getText(), BigDecimal.valueOf(Double.parseDouble(odds2.getText())), StatusForOutcomes.upcoming.name())
                        .execute();
            }
            if (isNotEmpty(resultName3.getText()) && isNotEmpty(odds3.getText())) {
                create.insertInto(POSSIBLE_OUTCOMES)
                        .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                        .values(eventId, resultName3.getText(), BigDecimal.valueOf(Double.parseDouble(odds3.getText())), StatusForOutcomes.upcoming.name())
                        .execute();
            }
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informácia");
            alert.setHeaderText("Športová udalosť bola pridaná.");
            alert.showAndWait();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            adminController.updateTabs();



        } catch (IllegalArgumentException e) {
            System.err.println("Chyba: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("SQL chyba: " + e.getMessage());
        }
    }


    private boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }


    private LocalTime parseTime(String timeText) {
        try {
            return LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Čas musí byť vo formáte HH:mm (napr. 14:30).");
        }
    }
}
