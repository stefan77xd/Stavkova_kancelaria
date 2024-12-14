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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.Setter;
import org.example.AlertFactory;
import org.example.AppThemeConfig;
import org.example.Controller;
import org.example.Factory;
import org.example.admin.AdminController;
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
    private final AlertFactory A = new AlertFactory();
    String theme = AppThemeConfig.getTheme();

    @FXML
    void Login() {
        var usernameOrEmail = usernameTextField.getText();
        var password = passwordTextField.getText();
        Principal principal;
        try {
            principal = AuthDao.authenticate(usernameOrEmail, password);
        } catch (AuthenticationException e) {
            countOfAttempts++;
            javafx.application.Platform.runLater(this::showAlert);
            recoverMail.setText("Zabudli ste heslo? Obnovte si heslo cez email tu.");
            recoverMail.setStyle("-fx-text-fill: #d22424;");
            return;
        }
        var role = principal.getRole();
        Auth.INSTANCE.setPrincipal(principal);

        Stage stage = (Stage) usernameTextField.getScene().getWindow();
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
                Login();
            }
        });
        usernameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
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
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
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
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
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
        A.showAlert("Pozor", "Zlé používateľské meno, alebo heslo.", "warning", Alert.AlertType.WARNING);
    }
    @FXML
    void openRecoverWindow() {
        if (countOfAttempts>=1){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/emailView.fxml"));
                Parent root = loader.load();
                EmailController emailController = loader.getController();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
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


