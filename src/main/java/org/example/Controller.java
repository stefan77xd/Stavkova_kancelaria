package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class Controller {

    @FXML
    private ListView<SportEvent> AllSportEvents;

    private SportEventDAO sportEventDAO = new SportEventDAO();

    @FXML
    public void initialize() {
        // Fetch the list of SportEvent objects
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();

        // Populate the ListView with SportEvent objects
        AllSportEvents.setItems(FXCollections.observableArrayList(sportEvents));

        // Set a fixed cell size for each item in the ListView
        AllSportEvents.setFixedCellSize(24);  // Example fixed height for each cell (adjust as needed)

        // Dynamically set the height of the ListView to fit all items
        AllSportEvents.setPrefHeight(sportEvents.size() * AllSportEvents.getFixedCellSize() + 2);  // +2 for padding

        // Set a custom cell factory for the ListView
        AllSportEvents.setCellFactory(lv -> new ListCell<SportEvent>() {
            @Override
            protected void updateItem(SportEvent sportEvent, boolean empty) {
                super.updateItem(sportEvent, empty);

                if (empty || sportEvent == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Set the text directly on the ListCell using SportEvent's toString() method
                    setText(sportEvent.toString());

                    // Set background and text color
                    setStyle("-fx-background-color: #303030; -fx-text-fill: white;");

                    // Add hover effect
                    setOnMouseEntered(event -> setStyle("-fx-background-color: #212121; -fx-text-fill: white;"));
                    setOnMouseExited(event -> setStyle("-fx-background-color: #303030; -fx-text-fill: white;"));
                }
            }
        });
    }

    // Method to open the new Ticket View
    @FXML
    public void openTicketView() {
        try {
            // Load the ticketView.fxml layout
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ticketView.fxml"));
            Parent root = fxmlLoader.load();

            // Create a new Stage (window)
            Stage stage = new Stage();
            stage.setTitle("Tickets");
            stage.setScene(new Scene(root));

            // Set the modality to WINDOW_MODAL to block input to the main window
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            // Set the owner of the new window to the main window
            stage.initOwner(AllSportEvents.getScene().getWindow());

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/ticket.png")));

            // Show the stage and wait for it to close (modal behavior)
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
