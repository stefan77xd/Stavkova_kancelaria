package org.example.email;

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
import org.example.AlertFactory;
import org.example.Factory;
import org.example.user.UserDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class EmailController {

    @FXML
    private TextField emailField;

    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();
    private final AlertFactory A = new AlertFactory();


    @FXML
    void confirmEmail() {
        String email = emailField.getText();
        String foundEmail = userDAO.findEmail(email);

        if (foundEmail == null) {
            A.showAlert("Pozor", "Email neexistuje.", "warning", Alert.AlertType.WARNING);
        } else {
            String token = TokenGenerator.generateToken();
            String subject = "Resetovanie hesla";
            String messageContent = "Použite tento token na resetovanie hesla: " + token + "\nŠťastné a veselé Vianoce prajú Štefan Malik a Lukáš Varga.";

            try {
                EmailSender.sendEmail(email, subject, messageContent);
                A.showAlert("Výborne", "Email bol odoslaný.", "success", Alert.AlertType.INFORMATION);
                Stage currentStage = (Stage) emailField.getScene().getWindow();
                currentStage.close();
                openTokenWindow(token, email);

            } catch (Exception e) {
                e.printStackTrace();
                A.showAlert("Chyba", "Email sa nepodarilo odoslať.", "warning", Alert.AlertType.WARNING);
            }
        }
    }

    private void openTokenWindow(String token, String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/tokenView.fxml"));
            Parent root = loader.load();
            TokenController tokenController = loader.getController();
            tokenController.email = email;
            tokenController.token = token;
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Token");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/admin.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
            tokenController.newPassword1.setEditable(false);
            tokenController.newPassword1.setDisable(true);
            tokenController.newPassword2.setEditable(false);
            tokenController.newPassword2.setDisable(true);
            tokenController.passwords.setDisable(true);


        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public class TokenGenerator {
        public static String generateToken() {
            return UUID.randomUUID().toString();
        }
    }


}
