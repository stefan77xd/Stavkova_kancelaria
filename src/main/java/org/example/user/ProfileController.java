package org.example.user;

import com.sun.glass.ui.ClipboardAssistance;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.example.Controller;
import org.example.Factory;
import org.example.security.Auth;

public class ProfileController {
    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();
    @FXML
    Label userName;
    public Stage stage;

    public Controller controller;

    @FXML
    private Button changePasswordButton;

    @FXML
    private Button changeUsernameButton;

    @FXML
    private Button deleteAccountButton;

    @FXML
    void changePassword(ActionEvent event) {
        deleteAccountButton.setDisable(true);
        changeUsernameButton.setDisable(true);
        menoHeslo1.setDisable(false);
        menoHeslo2.setDisable(false);
        tlacidloPotvrd.setDisable(false);

        meno = false;

    }

    @FXML
    private TextField menoHeslo1;

    @FXML
    private TextField menoHeslo2;

    @FXML
    private Button tlacidloPotvrd;

    boolean meno;

    @FXML
    void changeUsername(ActionEvent event) {
        deleteAccountButton.setDisable(true);
        changePasswordButton.setDisable(true);
        menoHeslo1.setDisable(false);
        menoHeslo2.setDisable(false);
        tlacidloPotvrd.setDisable(false);
        meno = true;


    }

    @FXML
    void deleteAccount(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Zmazat účet");
        alert.setHeaderText("Ste si istý že chcete zmazať svoj účet?");

        ButtonType yesButton = new ButtonType("Áno");
        ButtonType noButton = new ButtonType("Nie");
        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                String username = Auth.INSTANCE.getPrincipal().getUsername();
                Auth.INSTANCE.setPrincipal(null);
                userDAO.deleteUser(username);
                controller.loginoruser.setText("Login/Register");
                alert.close();
                stage.close();
            } else {
                alert.close();
            }
        });
    }

    @FXML
    void submit(ActionEvent event) {
        changePasswordButton.setDisable(false);
        changeUsernameButton.setDisable(false);
        deleteAccountButton.setDisable(false);
        if (meno) {
            if (menoHeslo1.getText().equals(menoHeslo2.getText())) {
                int userID = Auth.INSTANCE.getPrincipal().getId();
                if (userDAO.menoExists(menoHeslo1.getText())) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Pozor!");
                    alert.setHeaderText("Toto meno uz existuje!");
                    alert.showAndWait();
                    return;
                } else {

                    userDAO.changeUsername(userID, menoHeslo1.getText());
                    menoHeslo1.setDisable(true);
                    menoHeslo2.setDisable(true);
                    tlacidloPotvrd.setDisable(true);
                    Auth.INSTANCE.getPrincipal().setUsername(menoHeslo1.getText());
                    userName.setText(Auth.INSTANCE.getPrincipal().getUsername());
                    controller.onLoginSuccess();
                    menoHeslo2.clear();
                    menoHeslo1.clear();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Pozor!");
                alert.setHeaderText("Mená sa nezhodujú!");
                alert.showAndWait();
            }
        } else {
            if (menoHeslo1.getText().equals(menoHeslo2.getText())) {
                if (menoHeslo1.getText().length() < 8) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Pozor!");
                    alert.setHeaderText("Heslo musi byt dlhsie ako 8 znakov!");
                    alert.showAndWait();

                } else {
                    int userID = Auth.INSTANCE.getPrincipal().getId();
                    userDAO.changePassword(userID, menoHeslo1.getText());
                    menoHeslo1.setDisable(true);
                    menoHeslo2.setDisable(true);
                    tlacidloPotvrd.setDisable(true);
                    menoHeslo2.clear();
                    menoHeslo1.clear();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Pozor!");
                alert.setHeaderText("Heslá sa nezhodujú!");
                alert.showAndWait();
            }
        }

    }


    @FXML
    void initialize() {
        userName.setText(Auth.INSTANCE.getPrincipal().getUsername());


    }

}
