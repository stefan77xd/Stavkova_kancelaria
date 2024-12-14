package org.example;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Objects;


public class AlertFactory {


    String theme = AppThemeConfig.getTheme();

    public void showAlert(String title, String message, String icon, javafx.scene.control.Alert.AlertType type) {
        if (theme.equals("/css/dark-theme.css")) {
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

            alert.getDialogPane().setStyle("-fx-background-color: #f9f9f9;");
            alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #f9f9f9;");
                    alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: black;");
                }
            });
            Stage stage1 = (Stage) alert.getDialogPane().getScene().getWindow();
            stage1.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/" + icon + ".png"))));
            alert.showAndWait();
        }
    }
}
