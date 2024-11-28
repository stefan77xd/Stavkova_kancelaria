package org.example;
import lombok.Data;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Setter;
import org.example.exceptions.AuthenticationException;
import org.example.security.Auth;
import org.example.security.AuthDao;
import org.example.security.Principal;
import org.example.security.SQLiteAuthDAO;
import org.example.exceptions.AuthenticationException;

import java.io.IOException;

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
                showAlert("Pozor!", "Zlé použivateľské meno alebo heslo!");
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



    public void OpenRegistryWindow(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/registryView.fxml"));
            Parent root = loader.load();

            // Get the LoginController from the loader
            RegistryController registryController = loader.getController();

            // Pass the main controller to the login controller
            registryController.setLoginController(this);

            // Create a new scene with the root node
            Scene scene = new Scene(root);

            // Add the dark theme CSS stylesheet to the scene
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

            // Create the stage and set the scene
            Stage stage = new Stage();
            stage.setTitle("Registry View");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/login.png")));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL); // Modal window
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
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
}


