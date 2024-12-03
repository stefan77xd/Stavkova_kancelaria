package org.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import org.example.Controller;

public class AdminController {

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
