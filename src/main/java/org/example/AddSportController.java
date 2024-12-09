package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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
    private TextField sportType;

    @FXML
    private TextField time;

    @Setter
    public AdminController adminController;

    @FXML
    private VBox resultFieldsContainer;

    @FXML
    private VBox oddsFieldsContainer;


    public void initialize() {
        // Add the first two fields for results and odds
        resultFieldsContainer.setSpacing(10);
        oddsFieldsContainer.setSpacing(10);
        addNewResultAndOddsField();
        addNewResultAndOddsField();
    }

    private void addNewResultAndOddsField() {
        TextField resultField = new TextField();
        resultField.getStyleClass().add("resultFields");
        resultField.setPromptText("Názov výsledku:");
        TextField oddsField = new TextField();
        oddsField.getStyleClass().add("resultFields");
        oddsField.setPromptText("Kurz:");

        resultFieldsContainer.getChildren().add(resultField);
        oddsFieldsContainer.getChildren().add(oddsField);

        oddsField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow digits and only one decimal point
            if (!newValue.matches("\\d*\\.?\\d*")) {
                oddsField.setText(newValue.replaceAll("[^\\d.]", ""));
            }

            // Ensure only one decimal point is allowed
            if (newValue.indexOf('.') != newValue.lastIndexOf('.')) {
                oddsField.setText(oldValue);  // Restore the old value if more than one decimal point
            }

            // Ensure only two digits after the decimal point
            if (newValue.contains(".") && newValue.substring(newValue.indexOf(".") + 1).length() > 2) {
                oddsField.setText(oldValue);  // Restore the old value if there are more than two digits after the decimal point
            }
        });

        // Add listener to check when the field is filled
        resultField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                if (isLastResultField(resultField)) {
                    addNewResultAndOddsField(); // Add new field when the current one is filled
                }
            }
        });
    }

    private boolean isLastResultField(TextField field) {
        return resultFieldsContainer.getChildren().indexOf(field) == resultFieldsContainer.getChildren().size() - 1;
    }

    @FXML
    void Add(ActionEvent event) {
        // Your existing code for adding the event
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

            for (int i = 0; i < resultFieldsContainer.getChildren().size(); i++) {
                TextField resultField = (TextField) resultFieldsContainer.getChildren().get(i);
                TextField oddsField = (TextField) oddsFieldsContainer.getChildren().get(i);

                if (isNotEmpty(resultField.getText()) && isNotEmpty(oddsField.getText())) {
                    create.insertInto(POSSIBLE_OUTCOMES)
                            .columns(POSSIBLE_OUTCOMES.EVENT_ID, POSSIBLE_OUTCOMES.RESULT_NAME, POSSIBLE_OUTCOMES.ODDS, POSSIBLE_OUTCOMES.STATUS)
                            .values(eventId, resultField.getText(), BigDecimal.valueOf(Double.parseDouble(oddsField.getText())), StatusForOutcomes.upcoming.name())
                            .execute();
                }
            }

            // Inform the user
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Informácia");
            alert.setHeaderText("Športová udalosť bola pridaná.");
            alert.showAndWait();

            // Close the window
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
