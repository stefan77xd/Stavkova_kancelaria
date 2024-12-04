package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;


public class ListViewController {
    private SportEventDAO sportEventDAO;
    @FXML
    private ListView<SportEvent> listOfSport;

    @FXML
    private void initialize() {
        sportEventDAO = new SportEventDAO();
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();
        List<SportEvent> upcomingEvents = new ArrayList<>();
        for (SportEvent sportEvent : sportEvents) {
            if (sportEvent != null && sportEvent.getStatus() == StatusForEvent.upcoming) {
                upcomingEvents.add(sportEvent);
            }
        }
        int itemCount = upcomingEvents.size();
        listOfSport.setPrefHeight(itemCount * 24);
        listOfSport.setItems(FXCollections.observableArrayList(upcomingEvents));

    }


    @FXML
    void selected(MouseEvent event) throws IOException, SQLException {
        System.out.println("klik" + listOfSport.getSelectionModel().getSelectedItem());
        openPreviewWindow(listOfSport.getSelectionModel().getSelectedItem());
    }


    private void openPreviewWindow(SportEvent sportEvent) throws IOException, SQLException {
        if (sportEvent == null) {
            System.err.println("SportEvent is null. Cannot open preview window.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eventPreview.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            System.err.println("Failed to load eventPreview.fxml: " + e.getMessage());
            throw e; // Rethrow for proper error reporting
        }

        EventPreviewController eventPreviewController = loader.getController();
        eventPreviewController.sportEvent = sportEvent;
        eventPreviewController.Name.setText(sportEvent.getEventName());

        var eventID = sportEvent.getEventId();


        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);


            var results = create.select(POSSIBLE_OUTCOMES.RESULT_NAME)
                    .from(POSSIBLE_OUTCOMES)
                    .where(POSSIBLE_OUTCOMES.EVENT_ID.eq((int) eventID)) // Ensure eventID is of compatible type
                    .fetch();

            Set<String> resultNames = new HashSet<>(results.into(String.class));
            System.out.println(resultNames);

            Iterator<String> iterator = resultNames.iterator();

            if (iterator.hasNext()) {
                eventPreviewController.checkbox1.setText(iterator.next());
            } else {
                eventPreviewController.checkbox1.setText("");
                eventPreviewController.checkbox1.setDisable(true);
            }

            if (iterator.hasNext()) {
                eventPreviewController.checkbox2.setText(iterator.next());
            } else {
                eventPreviewController.checkbox2.setText("");
                eventPreviewController.checkbox2.setDisable(true);
            }

            if (iterator.hasNext()) {
                eventPreviewController.checkbox3.setText(iterator.next());
            } else {
                eventPreviewController.checkbox3.setText("");
                eventPreviewController.checkbox3.setDisable(true);
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            throw e; // Rethrow to ensure visibility
        }

        // Create and show the new scene
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

        Stage stage = new Stage();
        stage.setTitle("Event Preview");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

}





