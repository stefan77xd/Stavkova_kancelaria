package org.example.security;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.ConfigReader;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jooq.codegen.maven.example.Tables.USERS;

public class RegistryController {

    @FXML
    private TextField RegistryEmailTextField;

    @FXML
    private TextField RegistryPassword1TextField;

    @FXML
    private TextField RegistryPassword2TextField;

    @FXML
    private TextField RegistryUsernameTextField;

    @Setter
    private LoginController loginController;

    @FXML
    void SubmitValues() {
        // Get user inputs
        String username = RegistryUsernameTextField.getText().trim();
        String email = RegistryEmailTextField.getText().trim();
        String password1 = RegistryPassword1TextField.getText();
        String password2 = RegistryPassword2TextField.getText();

        // Validate inputs
        if (username.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            showAlert("Chyba", "Vyplnte všetky polia");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Chyba", "Zadajte platnú e-mailovú adresu.");
            return;
        }

        if (password1.length() < 8) {
            showAlert("Chyba", "Heslo musí mat najmenej 8 znakov.");
            return;
        }

        if (!password1.equals(password2)) {
            showAlert("Chyba", "Heslá sa nezhodujú.");
            return;
        }


        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            // Use jOOQ with the SQLite connection
            DSLContext create = DSL.using(connection);

            if (userExists(create, username, email)) {
                showAlert("Pozor", "Toto uzivatelske meno alebo email uz existuje.");
                return;
            }
            String hashedPassword = BCrypt.hashpw(password1, BCrypt.gensalt());
            insertUser(create, username, hashedPassword, email);
            showAlert("Výborne", "Registrácia prebehla v poriadku!");
            closeRegistryView();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            showAlert("Error", "An error occurred while processing your request.");
        }
    }

    private boolean userExists(DSLContext create, String username, String email) {
        return create.fetchExists(
                DSL.selectOne()
                        .from(USERS)
                        .where(USERS.USERNAME.eq(username).or(USERS.EMAIL.eq(email)))
        );
    }


    private void insertUser(DSLContext create, String username, String hashedPassword, String email) {
        create.insertInto(USERS)
                .set(USERS.USERNAME, username)
                .set(USERS.PASSWORD, hashedPassword)
                .set(USERS.EMAIL, email)
                .set(USERS.BALANCE, BigDecimal.ZERO)
                .set(USERS.ROLE, "user")
                .execute();
    }


    private void showAlert(String title, String message) {
        Alert.AlertType alertType = title.equals("Výborne") ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #303030;");
        alert.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
            }
        });
        Stage stage = (Stage) RegistryPassword2TextField.getScene().getWindow();
        String iconPath = title.equals("Výborne") ? "/icons/success.png" : "/icons/warning.png";
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        alert.showAndWait();
        if (title.equals("Výborne")) {
            stage.close();
        }

    }



    private void closeRegistryView() {

        Stage stage = (Stage) RegistryPassword2TextField.getScene().getWindow();
        stage.close();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    @FXML
    void initialize() {
        RegistryPassword1TextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Trigger the login when Enter key is pressed
                SubmitValues();
            }
        });
        RegistryPassword2TextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Trigger the login when Enter key is pressed
                SubmitValues();
            }
        });
        RegistryEmailTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Trigger the login when Enter key is pressed
                SubmitValues();
            }
        });
        RegistryUsernameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Trigger the login when Enter key is pressed
                SubmitValues();
            }
        });
    }
}
