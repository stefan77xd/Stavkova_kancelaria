package org.example.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.AlertFactory;
import org.example.Factory;
import org.example.possibleoutcome.PossibleOutcomeDAO;
import org.example.sportevent.SportEventDAO;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

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
    private final SportEventDAO sportEventDAO = Factory.INSTANCE.getSportEventDAO();
    private final PossibleOutcomeDAO possibleOutcomeDAO = Factory.INSTANCE.getPossibleOutcomeDAO();
    private final AlertFactory A = new AlertFactory();
    public void initialize() {
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
            if (!newValue.matches("\\d*\\.?\\d*")) {
                oddsField.setText(newValue.replaceAll("[^\\d.]", ""));
            }

            if (newValue.indexOf('.') != newValue.lastIndexOf('.')) {
                oddsField.setText(oldValue);
            }

            if (newValue.contains(".") && newValue.substring(newValue.indexOf(".") + 1).length() > 2) {
                oddsField.setText(oldValue);
            }
        });

        resultField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.trim().isEmpty()) {
                if (isLastResultField(resultField)) {
                    addNewResultAndOddsField();
                }
            }
        });
    }

    private boolean isLastResultField(TextField field) {
        return resultFieldsContainer.getChildren().indexOf(field) == resultFieldsContainer.getChildren().size() - 1;
    }

    @FXML
    void Add(ActionEvent event) {
            LocalDate date = DatePicker.getValue();
            if (date == null) {
                A.showAlert("Upozornenie", "Dátum nesmie byť prázdny.", "warning", Alert.AlertType.WARNING);
                return;
            }
            if (time.getText() == null || time.getText().isEmpty()) {
                A.showAlert("Upozornenie", "Čas nesmie byť prázdny", "warning", Alert.AlertType.WARNING);
                return;
            }
            LocalTime parsedTime = parseTime(time.getText());
            if (parsedTime == null) {
                return;
            }
            String startTime = date.atTime(parsedTime).toString();

            int eventId = sportEventDAO.createEvent(eventName.getText(), startTime, sportType.getText());

            for (int i = 0; i < resultFieldsContainer.getChildren().size(); i++) {
                TextField resultField = (TextField) resultFieldsContainer.getChildren().get(i);
                TextField oddsField = (TextField) oddsFieldsContainer.getChildren().get(i);

                if (isNotEmpty(resultField.getText()) && isNotEmpty(oddsField.getText())) {
                    possibleOutcomeDAO.createPossibleOutcome(eventId, resultField.getText(), oddsField.getText());
                }
            }
        A.showAlert("Informácia", "Športová udalosť bola úspešne pridaná", "success", Alert.AlertType.INFORMATION);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.close();
            adminController.updateTabs();
    }

    private boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    private LocalTime parseTime(String timeText) {
        try {
            return LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            A.showAlert("Upozornenie", "Čas musí byť vo formáte HH:mm.", "warning", Alert.AlertType.WARNING);
            return null;
        }
    }

}
