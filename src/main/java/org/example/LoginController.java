package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.exceptions.AuthenticationException;
import org.example.security.Auth;
import org.example.security.AuthDao;
import org.example.security.Principal;
import org.example.security.SQLiteAuthDAO;
import org.example.exceptions.AuthenticationException;

public class LoginController {

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Label invalidLogin;

    private AuthDao AuthDao = new SQLiteAuthDAO();

    @Setter
    private Controller mainController;

    @FXML
    void Login(ActionEvent event) {

        var usernameOrEmail = usernameTextField.getText();
        var password = passwordTextField.getText();

        Principal principal;
        try {
            principal = AuthDao.authenticate(usernameOrEmail, password);
        } catch (AuthenticationException e) {
            javafx.application.Platform.runLater(() -> {
                invalidLogin.setText("Invalid credentials");
            });
            return;
        }



        Auth.INSTANCE.setPrincipal(principal);

        if (mainController != null) {
            mainController.onLoginSuccess();
        }

        closeLoginView(event);

    }


    private void closeLoginView(ActionEvent event) {
        // Get the current stage from the event source and close it
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

}


