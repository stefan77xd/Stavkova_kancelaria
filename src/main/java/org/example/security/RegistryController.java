package org.example.security;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.ConfigReader;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    void SubmitValues(ActionEvent event) {
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

            if (userExists(connection, username, email)) {
                showAlert("Pozor", "Toto uzivatelske meno alebo email uz existuje.");
                return;
            }


            String hashedPassword = BCrypt.hashpw(password1, BCrypt.gensalt());


            insertUser(connection, username, hashedPassword, email);


            showAlert("Výborne", "Registrácia prebehla v poriadku!");
            closeRegistryView(event);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred while processing your request.");
        }
    }

    private boolean userExists(Connection connection, String username, String email) throws SQLException {
        String query = "SELECT 1 FROM users WHERE username = ? OR email = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void insertUser(Connection connection, String username, String hashedPassword, String email) throws SQLException {
        String insertQuery = "INSERT INTO users (username, password, email, balance, role) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, email);
            statement.setDouble(4, 0.0);
            statement.setString(5, "user");
            statement.executeUpdate();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.getDialogPane().setStyle("-fx-background-color: #303030;");
        alert.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
            }
        });
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/warning.png")));
        alert.showAndWait();
    }



    private void closeRegistryView(ActionEvent event) {

        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}