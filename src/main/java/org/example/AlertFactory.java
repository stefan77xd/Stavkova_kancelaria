package org.example;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;



public class AlertFactory {

    public boolean darkTheme = true;
    public void showAlert(String title, String message, String icon, javafx.scene.control.Alert.AlertType type) {
        if (darkTheme) {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(message);

            alert.getDialogPane().setStyle("-fx-background-color: #303030;");
            alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                    alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                }
            });
            Stage stage1 = (Stage) alert.getDialogPane().getScene().getWindow();
            stage1.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/" + icon + ".png"))));
            alert.showAndWait();
        } else {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(message);

            alert.getDialogPane().setStyle("-fx-background-color: #303030;");
            alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                    alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                }
            });
            Stage stage1 = (Stage) alert.getDialogPane().getScene().getWindow();
            stage1.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/" + icon + ".png"))));
            alert.showAndWait();
        }
    }
}
