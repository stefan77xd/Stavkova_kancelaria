package org.example.security;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.AlertFactory;
import org.example.Factory;
import org.example.user.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;
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
    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();
    private final AlertFactory A = new AlertFactory();
    @FXML
    void SubmitValues() {
        String username = RegistryUsernameTextField.getText().trim();
        String email = RegistryEmailTextField.getText().trim();
        String password1 = RegistryPassword1TextField.getText();
        String password2 = RegistryPassword2TextField.getText();

        if (username.isEmpty() || email.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
            A.showAlert("Chyba", "Vyplnte všetky polia.", "warning", Alert.AlertType.WARNING);
            return;
        }
        if (!isValidEmail(email)) {
            A.showAlert("Chyba", "Zadajte platnú emailovú adresu.", "warning", Alert.AlertType.WARNING);
            return;
        }
        if (password1.length() < 8) {
            A.showAlert("Chyba", "Heslo musí mať aspoň 8 znakov.", "warning", Alert.AlertType.WARNING);
            return;
        }
        if (!password1.equals(password2)) {
            A.showAlert("Chyba", "Heslá sa nezhodujú.", "warning", Alert.AlertType.WARNING);
            return;
        }
        if (userDAO.userExists(username, email)) {
            A.showAlert("Chyba", "Toto používateľské meno, alebo email už existuje.", "warning", Alert.AlertType.WARNING);
            return;
        }
        String hashedPassword = BCrypt.hashpw(password1, BCrypt.gensalt());
        userDAO.insertUser(username, hashedPassword, email);
        A.showAlert("Výborne", "Registrácia prebehla v poriadku.", "success", Alert.AlertType.INFORMATION);
        closeRegistryView();
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
                SubmitValues();
            }
        });
        RegistryPassword2TextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SubmitValues();
            }
        });
        RegistryEmailTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SubmitValues();
            }
        });
        RegistryUsernameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                SubmitValues();
            }
        });
    }
}
