package org.example.security;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.example.Factory;
import org.example.admin.AdminController;
import org.example.Controller;
import org.example.email.EmailController;
import org.example.exceptions.AuthenticationException;
import org.example.user.Role;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label recoverMail;

    int countOfAttempts = 0;

    @FXML
    private TextField usernameTextField;

    private final AuthDao AuthDao = Factory.INSTANCE.getSqLiteAuthDAO();

    @Setter
    private Controller mainController;

    @FXML
    void Login() {
        var usernameOrEmail = usernameTextField.getText();
        var password = passwordTextField.getText();

        Principal principal;
        try {
            principal = AuthDao.authenticate(usernameOrEmail, password);
        } catch (AuthenticationException e) {
            countOfAttempts++;
            System.out.println(countOfAttempts);
            javafx.application.Platform.runLater(this::showAlert);
            recoverMail.setText("Zabudli ste heslo? Obnovte si heslo cez email tu.");
            recoverMail.setStyle("-fx-text-fill: #d22424;");
            return;
        }

        var role = principal.getRole();
        Auth.INSTANCE.setPrincipal(principal);

        Stage stage = (Stage) usernameTextField.getScene().getWindow();  // Get Stage from the TextField's Scene
        if (role == Role.user) {
            if (mainController != null) {
                mainController.onLoginSuccess();
            }
            stage.close();
        } else if (role == Role.admin) {
            closeAllWindows();
            stage.close();
            openAdminWindow();
        }
    }

    @FXML
    void initialize() {
        passwordTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Trigger the login when Enter key is pressed
                Login();
            }
        });
        usernameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                // Trigger the login when Enter key is pressed
                Login();
            }
        });
    }

    private void openAdminWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/adminView.fxml"));
            Parent root = loader.load();

            AdminController adminController = loader.getController();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Admin");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/admin.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
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

            RegistryController registryController = loader.getController();
            registryController.setLoginController(this);
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
    @FXML
    void openRecoverWindow(MouseEvent event) {
        if (countOfAttempts>=1){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/emailView.fxml"));
                Parent root = loader.load();
                EmailController emailController = loader.getController();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

                Stage stage = new Stage();
                stage.setTitle("Email");
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }

        }
    }


}


