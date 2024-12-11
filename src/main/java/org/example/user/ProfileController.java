package org.example.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.example.security.Auth;
public class ProfileController {
    @FXML
    Label userName;
    @FXML
    void changePassword(ActionEvent event) {
    }

    @FXML
    void changeUsername(ActionEvent event) {
    }
    @FXML
    void deleteAccount(ActionEvent event) {
    }
    @FXML
    void initialize() {
        userName.setText(Auth.INSTANCE.getPrincipal().getUsername());
    }
}
