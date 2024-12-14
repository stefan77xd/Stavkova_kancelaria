package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        String theme = AppThemeConfig.getTheme();
        var loader = new FXMLLoader(getClass().getResource("view.fxml"));
        Parent rootPane = loader.load();
        var scene = new Scene(rootPane);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
        stage.setTitle("Stávková kancelária");
        stage.setScene(scene);
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/icon.png"))));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
