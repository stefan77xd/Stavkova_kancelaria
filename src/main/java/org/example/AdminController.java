package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import org.example.Controller;
import org.example.security.Auth;


public class AdminController {

    @FXML
    private Label welcomeSign;

    @FXML
    private void logout() {
        try {
            // Clear the authentication data
            Auth.INSTANCE.setPrincipal(null);

            // Close the current stage
            Stage currentStage = (Stage) welcomeSign.getScene().getWindow();
            currentStage.close();

            // Load the new view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view.fxml"));
            Parent root = loader.load();

            // Create a new stage
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            // Add stylesheets if necessary
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

            // Set stage properties
            stage.setTitle("Stávková kancelária");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/icon.png"))));
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        welcomeSign.setText("Vitaj " + Auth.INSTANCE.getPrincipal().getUsername());
    }

    @FXML
    void AddSportEvent(ActionEvent event) {
        openAddSportWindow();

    }
    @FXML
    void ShowEvents(ActionEvent event) {
        openListWindow();
    }
    private void openListWindow(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/listView.fxml"));
            Parent root = loader.load();

            // Get the LoginController from the loader
            ListViewController listViewController = loader.getController();



            // Create a new scene with the root node
            Scene scene = new Scene(root);


            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Eventy");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }



    private void openAddSportWindow(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/addSportEvent.fxml"));
            Parent root = loader.load();

            // Get the LoginController from the loader
            AddSportController addSportController = loader.getController();



            // Create a new scene with the root node
            Scene scene = new Scene(root);


            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("New event");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


}
