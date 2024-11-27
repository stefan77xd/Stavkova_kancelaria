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
import org.example.sportevent.MatchController;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Controller {

    @FXML
    private TabPane sportTabPane;

    private SportEventDAO sportEventDAO = new SportEventDAO();

    @FXML
    public void openTicketView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ticketView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ticket View");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/ticket.png")));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openMatchScene(SportEvent sportEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/matchView.fxml"));
            Parent root = loader.load();

            MatchController matchController = loader.getController();
            matchController.setSportEvent(sportEvent);

            Stage stage = new Stage();
            stage.setTitle("Detail zápasu");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/match.png")));
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        sportTabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            }
        });

        // Zavolanie metódy showOdds na inicializáciu tabov
        showOdds(null);
    }


    private void setupListView(ListView<SportEvent> listView, List<SportEvent> sportEvents) {
        listView.setItems(FXCollections.observableArrayList(sportEvents));
        listView.setFixedCellSize(24);
        listView.setPrefHeight(sportEvents.size() * listView.getFixedCellSize() + 2);
        listView.setCellFactory(lv -> new ListCell<SportEvent>() {
            @Override
            protected void updateItem(SportEvent sportEvent, boolean empty) {
                super.updateItem(sportEvent, empty);
                if (empty || sportEvent == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(sportEvent.toString());
                }
            }
        });

        listView.setOnMouseClicked(event -> {
            SportEvent selectedEvent = listView.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                openMatchScene(selectedEvent);
            }
        });
    }

    public void showResults(javafx.event.ActionEvent actionEvent) {
        updateEvents(StatusForEvent.finished);
    }
    public void showOdds(javafx.event.ActionEvent actionEvent) {
        updateEvents(StatusForEvent.upcoming);

    }


    public void updateEvents(Enum<StatusForEvent> status) {
        sportTabPane.getTabs().clear();
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();
        List<SportEvent> Events= new ArrayList<>();
        for (SportEvent sportEvent : sportEvents) {
            if (sportEvent.getStatus()==status) {
                Events.add(sportEvent);
            }
        }
        Map<String, List<SportEvent>> groupedEvents = Events.stream()
                .collect(Collectors.groupingBy(SportEvent::getSportType));
        Tab allTab = new Tab("All");
        ListView<SportEvent> allListView = new ListView<>();
        setupListView(allListView, Events);
        VBox allVBox = new VBox(allListView);
        allVBox.setFillWidth(true);
        allTab.setContent(allVBox);
        sportTabPane.getTabs().add(allTab);

        for (Map.Entry<String, List<SportEvent>> entry : groupedEvents.entrySet()) {
            Tab sportTab = new Tab(entry.getKey());
            ListView<SportEvent> listView = new ListView<>();
            setupListView(listView, entry.getValue());
            VBox vbox = new VBox(listView);
            vbox.setFillWidth(true);
            sportTab.setContent(vbox);
            sportTabPane.getTabs().add(sportTab);
        }
    }

    }


