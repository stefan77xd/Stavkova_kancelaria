package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Controller {

    @FXML
    private TabPane sportTabPane;
    private SportEventDAO sportEventDAO = new SportEventDAO(); // DAO for loading sports events

    @FXML
    public void openTicketView() {
        try {
            // Load the Ticket View from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ticketView.fxml"));
            Parent root = loader.load();

            // Create a new stage (window)
            Stage stage = new Stage();
            stage.setTitle("Ticket View");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/ticket.png")));

            // Set the scene for the stage
            stage.setScene(new Scene(root));

            // Set modality to block input events to other windows
            stage.initModality(Modality.APPLICATION_MODAL);

            // Show the stage
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Handle exception if FXML can't be loaded
        }
    }

    @FXML
    public void initialize() {
        // Add a listener to check when the TabPane is added to a Scene
        sportTabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                // Apply the stylesheet when the scene is available
                newScene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            }
        });

        // Set background for the TabPane directly in the controller
        sportTabPane.setStyle("-fx-background-color: #212121;");

        // Load all sport events
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();

        // Group the events by sport type (e.g., "Football", "Basketball")
        Map<String, List<SportEvent>> groupedEvents = sportEvents.stream()
                .collect(Collectors.groupingBy(SportEvent::getSportType));

        // Create an "All" tab to display all events
        Tab allTab = new Tab("All");

        // Combine all events into a single ListView
        ListView<SportEvent> allListView = new ListView<>();
        setupListView(allListView, sportEvents);

        // Set the ListView in a VBox and add to the "All" tab
        VBox allVBox = new VBox(allListView);
        allVBox.setFillWidth(true);
        allVBox.setStyle("-fx-background-color: #303030;");
        allTab.setContent(allVBox);

        // Add the "All" tab to the TabPane
        sportTabPane.getTabs().add(allTab);

        // For each sport type, create a new Tab
        for (Map.Entry<String, List<SportEvent>> entry : groupedEvents.entrySet()) {
            String sportType = entry.getKey(); // The type of sport
            List<SportEvent> events = entry.getValue(); // Events for that sport

            // Create a new Tab for the sport
            Tab sportTab = new Tab(sportType);

            // Create a ListView for the sport events
            ListView<SportEvent> listView = new ListView<>();

            // Use the helper method to configure the ListView
            setupListView(listView, events);

            // Create a VBox to hold the ListView (allows it to grow and fill space)
            VBox vbox = new VBox(listView);
            vbox.setFillWidth(true); // Ensures ListView fills available width
            vbox.setStyle("-fx-background-color: #303030;");

            // Set the VBox as the content of the Tab
            sportTab.setContent(vbox);

            // Add the Tab to the TabPane
            sportTabPane.getTabs().add(sportTab);
        }
    }


    private void setupListView(ListView<SportEvent> listView, List<SportEvent> sportEvents) {
        // Populate the ListView
        listView.setItems(FXCollections.observableArrayList(sportEvents));
        listView.setFixedCellSize(24);

        // Adjust the preferred height based on the number of items
        listView.setPrefHeight(sportEvents.size() * listView.getFixedCellSize() + 2);

        // Configure how each ListView cell looks
        listView.setCellFactory(lv -> new ListCell<SportEvent>() {
            @Override
            protected void updateItem(SportEvent sportEvent, boolean empty) {
                super.updateItem(sportEvent, empty);

                if (empty || sportEvent == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Set text and styles for the cell
                    setText(sportEvent.toString());
                    setStyle("-fx-background-color: #303030; -fx-text-fill: white;");

                    // Change style on hover
                    setOnMouseEntered(event -> setStyle("-fx-background-color: #212121; -fx-text-fill: white;"));
                    setOnMouseExited(event -> setStyle("-fx-background-color: #303030; -fx-text-fill: white;"));
                }
            }
        });
    }
}
