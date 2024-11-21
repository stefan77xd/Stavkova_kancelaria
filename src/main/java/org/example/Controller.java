package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;

import java.util.List;

public class Controller {

    @FXML
    private ListView<SportEvent> AllSportEvents;

    private SportEventDAO sportEventDAO = new SportEventDAO();

    @FXML
    public void initialize() {

        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();


        AllSportEvents.setItems(FXCollections.observableArrayList(sportEvents));
    }
}

