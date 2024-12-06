package org.example;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.example.security.Auth;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;


public class AdminController {

    @FXML
    private Label welcomeSign;

    @FXML
    private TabPane tabPane;  // Injected TabPane

    private SportEventDAO sportEventDAO;

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

        // Initialize SportEventDAO and load the sport events
        sportEventDAO = new SportEventDAO();
        loadSportsIntoTabs();
    }

    private void loadSportsIntoTabs() {
        // Get all upcoming sport events
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents().stream()
                .filter(event -> event.getStatus() == StatusForEvent.upcoming)
                .collect(Collectors.toList());

        // Create an "All" tab that displays all events
        Tab allTab = new Tab("All");
        ListView<SportEvent> allEventsListView = new ListView<>();

        // Set the items for the "All" tab and adjust its height
        allEventsListView.setItems(FXCollections.observableArrayList(sportEvents));
        int allItemCount = sportEvents.size();
        allEventsListView.setPrefHeight(allItemCount * 24); // Assuming 24 pixels per item

        // Handle event selection in the "All" tab
        allEventsListView.setOnMouseClicked(event -> {
            SportEvent selectedEvent = allEventsListView.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                try {
                    openEventPreviewWindow(selectedEvent); // Open event preview
                } catch (IOException e) {
                    System.err.println("Error opening event preview: " + e.getMessage());
                }
            }
        });

        VBox allVBox = new VBox(allEventsListView);
        allTab.setContent(allVBox);
        tabPane.getTabs().add(allTab); // Add the "All" tab first

        // Group sport events by sport type
        Map<String, List<SportEvent>> eventsBySportType = sportEvents.stream()
                .collect(Collectors.groupingBy(SportEvent::getSportType));

        // Create a tab for each sport type
        for (Map.Entry<String, List<SportEvent>> entry : eventsBySportType.entrySet()) {
            String sportType = entry.getKey();
            List<SportEvent> events = entry.getValue();

            Tab tab = new Tab(sportType);

            ListView<SportEvent> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(events));

            // Set the preferred height for the ListView based on the number of events
            int itemCount = events.size();
            listView.setPrefHeight(itemCount * 24); // Assuming 24 pixels per item

            // Handle event selection in sport-type-specific tabs
            listView.setOnMouseClicked(event -> {
                SportEvent selectedEvent = listView.getSelectionModel().getSelectedItem();
                if (selectedEvent != null) {
                    try {
                        openEventPreviewWindow(selectedEvent); // Open event preview
                    } catch (IOException e) {
                        System.err.println("Error opening event preview: " + e.getMessage());
                    }
                }
            });

            VBox vBox = new VBox(listView);
            tab.setContent(vBox);
            tabPane.getTabs().add(tab); // Add each sport-specific tab
        }
    }



    // Method to open the EventPreviewController
    private void openEventPreviewWindow(SportEvent sportEvent) throws IOException {
        if (sportEvent == null) {
            System.err.println("SportEvent is null. Cannot open preview window.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eventPreview.fxml"));
        Parent root = loader.load();

        EventPreviewController eventPreviewController = loader.getController();
        eventPreviewController.setSportEvent(sportEvent); // Pass the sport event

        eventPreviewController.setAdminController(this);

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


    @FXML
    void AddSportEvent(ActionEvent event) {
        openAddSportWindow();

    }


    private void openAddSportWindow(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/addSportEvent.fxml"));
            Parent root = loader.load();

            // Get the LoginController from the loader
            AddSportController addSportController = loader.getController();
            addSportController.setAdminController(this);

            // Create a new scene with the root node
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("New event");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/admin.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void updateTabs() {
        tabPane.getTabs().clear();
        initialize();
    }


}
