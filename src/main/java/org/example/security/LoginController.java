package org.example.security;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.example.AdminController;
import org.example.Controller;
import org.example.exceptions.AuthenticationException;
import org.example.user.Role;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField usernameTextField;

    private final AuthDao AuthDao = new SQLiteAuthDAO();

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
            javafx.application.Platform.runLater(this::showAlert);
            return;
        }

        var role = principal.getRole();
        Auth.INSTANCE.setPrincipal(principal);

        if (role == Role.user) {
            if (mainController != null) {
                mainController.onLoginSuccess();
            }
            closeLoginView(event); // Zatvorí len okno prihlasovania
        } else if (role == Role.admin) {
            closeAllWindows();
            closeLoginView(event);
            openAdminWindow();

        }
    }

    private void openAdminWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/adminView.fxml"));
            Parent root = loader.load();

            // Get the LoginController from the loader
            AdminController adminController = loader.getController();



            // Create a new scene with the root node
            Scene scene = new Scene(root);


            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Admin");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }



    private void closeLoginView(ActionEvent event) {
        // Get the current stage from the event source and close it
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
        stage.close();
    }


    public void closeAllWindows() {
        for (Window window : Window.getWindows()) {
            if (window instanceof Stage) {
                ((Stage) window).close();
            }
        }
    }
    public void OpenRegistryWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/registryView.fxml"));
            Parent root = loader.load();

            // Get the LoginController from the loader
            RegistryController registryController = loader.getController();

            // Pass the main controller to the login controller
            registryController.setLoginController(this);

            // Create a new scene with the root node
            Scene scene = new Scene(root);


            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Register");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    private void showAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Pozor!");
        alert.setHeaderText("Zlé použivateľské meno alebo heslo!");
        alert.getDialogPane().setStyle("-fx-background-color: #303030;");
        alert.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
            }
        });
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/warning.png"))));
        alert.showAndWait();
    }
}


