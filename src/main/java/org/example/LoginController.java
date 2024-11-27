package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
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

    private AuthDao AuthDao = new SQLiteAuthDAO();

    @FXML
    void Login(ActionEvent event) {
        var usernameOrEmail = usernameTextField.getText();
        var password = passwordTextField.getText();

        Principal principal;
        try {
            principal = AuthDao.authenticate(usernameOrEmail, password);
        } catch (AuthenticationException e) {
            return;
        }

        Auth.INSTANCE.setPrincipal(principal);
    }

}


