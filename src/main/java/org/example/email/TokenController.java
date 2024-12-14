package org.example.email;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.AlertFactory;
import org.example.Factory;
import org.example.user.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Objects;

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

    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();
    private final AlertFactory A = new AlertFactory();

    @FXML
    void SubmitAction(ActionEvent event) {
        if (!verified) {
            if (tokenField.getText().equals(token)) {
                verified = true;
                tokenField.setDisable(true);
                submitButton.setDisable(true);
                A.showAlert("Výborne", "Tokeny sa rovnajú, teraz zadajte heslo.", "success", Alert.AlertType.INFORMATION);
                newPassword1.setEditable(true);
                newPassword1.setDisable(false);
                newPassword2.setEditable(true);
                newPassword2.setDisable(false);
                passwords.setDisable(false);
                tokenField.setText(null);
            } else {
                A.showAlert("Pozor", "Tokeny sa nezhodujú.", "warning", Alert.AlertType.WARNING);
            }
        }
    }

    @FXML
    void submitPasswords() {
        if (newPassword1.getText().equals(newPassword2.getText())) {
            if (newPassword1.getText() != null && newPassword1.getText().length() >= 8) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                userDAO.updatePassword(BCrypt.hashpw(newPassword1.getText(), BCrypt.gensalt()), email);
                A.showAlert("Výborne", "Zmenili ste heslo.", "success", Alert.AlertType.INFORMATION);
                Stage currentStage = (Stage) submitButton.getScene().getWindow();
                currentStage.close();


            } else {
                A.showAlert("Pozor", "Heslo musí byť dlhšie, ako 8 znakov.", "warning", Alert.AlertType.WARNING);
            }

        } else {
            A.showAlert("Pozor", "Heslá sa nezhodujú.", "warning", Alert.AlertType.WARNING);
        }
    }
}
