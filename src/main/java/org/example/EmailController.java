package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class EmailController {

    @FXML
    private TextField emailField;


    @FXML
    void confirmEmail(ActionEvent event) throws SQLException {
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);
            String email = emailField.getText();
            String foundEmail = create.select(USERS.EMAIL).from(USERS).where(USERS.EMAIL.eq(email)).fetchOneInto(String.class);

            if (foundEmail == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Pozor!");
                alert.setHeaderText("Email neexistuje!");
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
                alert.showAndWait();
            } else {
                String token = TokenGenerator.generateToken();
                String subject = "Resetovanie hesla";
                String messageContent = "Použite tento token na resetovanie hesla: " + token + "\nŠťastné a veselé Vianoce prajú Štefan Malik a Lukáš Varga.";

                try {
                    EmailSender.sendEmail(email, subject, messageContent);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Výborne!");
                    alert.setHeaderText("Email bol odoslaný!");
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
                    Stage currentStage = (Stage) emailField.getScene().getWindow();
                    currentStage.close();
                    openTokenWindow(token, email);

                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Chyba!");
                    alert.setHeaderText("Email sa nepodarilo odoslať.");
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
                    alert.showAndWait();
                }
            }
        }
    }

    private void openTokenWindow(String token, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tokenView.fxml"));
            Parent root = loader.load();
            TokenController tokenController = loader.getController();
            tokenController.email=email;
            tokenController.token=token;
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Token");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/admin.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            tokenController.newPassword1.setEditable(false);
            tokenController.newPassword1.setDisable(true);
            tokenController.newPassword2.setEditable(false);
            tokenController.newPassword2.setDisable(true);
            tokenController.passwords.setDisable(true);


        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public class TokenGenerator {
        public static String generateToken() {
            return UUID.randomUUID().toString();
        }
    }



}
