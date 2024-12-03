package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;

import java.util.ArrayList;
import java.util.List;

public class ListViewController {
    private SportEventDAO sportEventDAO;
    @FXML
    private ListView<SportEvent> listOfSport;


    public void initialize() {
        sportEventDAO = new SportEventDAO();
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();
        List<SportEvent> upcomingEvents = new ArrayList<>();
        for (SportEvent sportEvent : sportEvents) {
            if (sportEvent.getStatus()== StatusForEvent.upcoming) {
                upcomingEvents.add(sportEvent);
            }

        }
        listOfSport.setItems(FXCollections.observableArrayList(upcomingEvents));

        }
    @FXML
    void selected(MouseEvent event) {
        System.out.println("klik"+listOfSport.getSelectionModel().getSelectedItem());
    }
    }


