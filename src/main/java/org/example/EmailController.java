package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
                alert.showAndWait();
            } else {
                String token = TokenGenerator.generateToken();
                String subject = "Resetovanie hesla";
                String messageContent = "Použite tento token na resetovanie hesla: " + token + "Šťastné a veselé Vianoce prajú Štefan Malik a Lukáš Varga.";

                try {
                    EmailSender.sendEmail(email, subject, messageContent);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Výborne!");
                    alert.setHeaderText("Email bol odoslaný!");
                    alert.showAndWait();
                } catch (Exception e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Chyba!");
                    alert.setHeaderText("Email sa nepodarilo odoslať.");
                    alert.showAndWait();
                }
            }
        }
    }

    public class TokenGenerator {
        public static String generateToken() {
            return UUID.randomUUID().toString();
        }
    }



}
