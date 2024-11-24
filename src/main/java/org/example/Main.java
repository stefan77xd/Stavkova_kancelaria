package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        var loader = new FXMLLoader(getClass().getResource("view.fxml"));
        Parent rootPane = loader.load();
        var scene = new Scene(rootPane);

        // Set the title of the application window
        stage.setTitle("Stávková kancelária");

        // Set the scene
        stage.setScene(scene);

        // Load and set the application icon (Make sure the icon is placed in the resources folder)
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/icon.png")));

        // Show the stage
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
