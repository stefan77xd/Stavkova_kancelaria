package org.example.email;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.ConfigReader;
import org.jooq.DSLContext;
import org.jooq.True;
import org.jooq.codegen.maven.example.tables.Users;
import org.jooq.impl.DSL;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class TokenController {

    @FXML
    Button passwords;


    @FXML
    private TextField tokenField;

    @FXML
    TextField newPassword1;

    @FXML
    TextField newPassword2;

    String token;
    String email;

    @FXML
    private Button submitButton;

    private boolean verified = false;

    @FXML
    void SubmitAction(ActionEvent event) {
        if (!verified) {
            if (tokenField.getText().equals(token)) {
                verified = true;
                tokenField.setDisable(true);
                submitButton.setDisable(true);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Výborne!");
                alert.setHeaderText("Tokeny sa rovnajú, teraz zadajte nove heslo!");
                alert.getDialogPane().setStyle("-fx-background-color: #303030;");
                alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                        alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                    }
                });
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/success.png"))));
                alert.showAndWait();
                newPassword1.setEditable(true);
                newPassword1.setDisable(false);
                newPassword2.setEditable(true);
                newPassword2.setDisable(false);
                passwords.setDisable(false);
                tokenField.setText(null);
            }else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Pozor!");
                alert.setHeaderText("Tokeny sa nezhodujú!");
                alert.getDialogPane().setStyle("-fx-background-color: #303030;");
                alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                        alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                    }
                });
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/warning.png"))));
                alert.showAndWait();
            }
        }


    }

    @FXML
    void submitPasswords(ActionEvent event) throws SQLException {
        if (newPassword1.getText().equals(newPassword2.getText())) {
            if (newPassword1.getText() != null && newPassword1.getText().length() >= 8) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                Properties config = ConfigReader.loadProperties("config.properties");
                String dbUrl = config.getProperty("db.url");
                try (Connection connection = DriverManager.getConnection(dbUrl)) {
                    DSLContext create = DSL.using(connection);
                    create.update(USERS).set(USERS.PASSWORD, BCrypt.hashpw(newPassword1.getText(), BCrypt.gensalt())).where(USERS.EMAIL.eq(email)).execute();
                    alert.setTitle("Výborne!");
                    alert.setHeaderText("Zmenili ste heslo!");
                    alert.getDialogPane().setStyle("-fx-background-color: #303030;");
                    alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue) {
                            alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                            alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                        }
                    });
                    Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                    stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/success.png"))));
                    alert.showAndWait();
                    Stage currentStage = (Stage) submitButton.getScene().getWindow();
                    currentStage.close();
                }

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Pozor!");
                alert.setHeaderText("Heslo musi byt dlhsie ako 8 znakov!");
                alert.getDialogPane().setStyle("-fx-background-color: #303030;");
                alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue) {
                        alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                        alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                    }
                });
                Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/warning.png"))));
                alert.showAndWait();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Pozor!");
            alert.setHeaderText("Heslá sa nezhodujú!");
            alert.getDialogPane().setStyle("-fx-background-color: #303030;");
            alert.showingProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                    alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                }
            });
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/warning.png"))));
            alert.showAndWait();
        }
    }
}
