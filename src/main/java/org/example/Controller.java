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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Controller {

    @FXML
    private TabPane sportTabPane;
    private SportEventDAO sportEventDAO = new SportEventDAO(); // DAO na načítanie športových udalostí

    @FXML
    public void initialize() {
        // Načítame všetky športové udalosti
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();

        // Roztriedime udalosti podľa športového typu (napr. "Football", "Basketball")
        Map<String, List<SportEvent>> groupedEvents = sportEvents.stream()
                .collect(Collectors.groupingBy(SportEvent::getSportType));
        sportTabPane.setStyle("-fx-background-color: #212121;");

        // Pre každý športový typ vytvoríme záložku (Tab)
        for (Map.Entry<String, List<SportEvent>> entry : groupedEvents.entrySet()) {
            String sportType = entry.getKey(); // Typ športu
            List<SportEvent> events = entry.getValue(); // Udalosti pre daný šport

            // Vytvoríme novú záložku
            Tab sportTab = new Tab(sportType);
            sportTab.setStyle("-fx-background-color: #212121; -fx-text-fill: #FFFFFF;");

            // Vytvoríme ListView pre udalosti tohto športu
            ListView<SportEvent> listView = new ListView<>(FXCollections.observableArrayList(events));
            listView.setCellFactory(lv -> new ListCell<SportEvent>() {
                @Override
                protected void updateItem(SportEvent sportEvent, boolean empty) {
                    super.updateItem(sportEvent, empty);

                    if (empty || sportEvent == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(sportEvent.toString());
                        setStyle("-fx-background-color: #303030; -fx-text-fill: white;");

                        // Hover efekt
                        setOnMouseEntered(event -> setStyle("-fx-background-color: #212121; -fx-text-fill: white;"));
                        setOnMouseExited(event -> setStyle("-fx-background-color: #303030; -fx-text-fill: white;"));
                    }
                }
            });

            // Pridáme ListView do záložky
            sportTab.setContent(listView);

            // Pridáme záložku do TabPane
            sportTabPane.getTabs().add(sportTab);
        }
    }

    // Otvorenie novej scény pre Tikety
    @FXML
    public void openTicketView() {
        try {
            // Načítame FXML pre Ticket View
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ticketView.fxml"));
            Parent root = fxmlLoader.load();

            // Vytvoríme nové okno (Stage)
            Stage stage = new Stage();
            stage.setTitle("Tickets");
            stage.setScene(new Scene(root));

            // Nastavíme, že nové okno je modal (blokuje hlavné okno)
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);

            // Nastavíme hlavné okno ako vlastníka nového okna
            stage.initOwner(sportTabPane.getScene().getWindow());

            // Nastavíme ikonu okna
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/ticket.png")));

            // Zobrazíme nové okno a čakáme, kým sa zavrie
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}