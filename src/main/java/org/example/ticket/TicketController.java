package org.example.ticket;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.Factory;
import org.example.security.Auth;

import java.util.ArrayList;
import java.util.List;


public class TicketController {

    private final TicketDAO UserTicketDAO = Factory.INSTANCE.getTicketDAO();

    @FXML
    private TabPane ticketPane;

    @FXML
    private Label testLabel;

    @FXML
    public void initialize() {
        StatusForTicket[] orderedStatuses = {
                StatusForTicket.pending,
                StatusForTicket.won,
                StatusForTicket.lost
        };

        // Create tabs in the defined order
        if (Auth.INSTANCE.getPrincipal() != null) {
            for (StatusForTicket status : orderedStatuses) {
                String capitalizedLabel = capitalizeFirstLetter(status.name());
                Tab tab = new Tab(capitalizedLabel);

                ListView<Ticket> listView = new ListView<>();

                VBox vbox = new VBox(listView);
                vbox.setFillWidth(true);
                tab.setContent(vbox);

                ticketPane.getTabs().add(tab);
            }
        }

        if (Auth.INSTANCE.getPrincipal() != null) {
            testLabel.setText("Tikety");
            List<Ticket> userTickets = UserTicketDAO.getUsersTickets(Auth.INSTANCE.getPrincipal().getId().intValue());
            for (Tab tab : ticketPane.getTabs()) {
                String lowercaseTabName = tab.getText().toLowerCase();
                StatusForTicket status = StatusForTicket.valueOf(lowercaseTabName);

                List<Ticket> filteredTickets = filterTicketsByStatus(userTickets, status);

                ListView<Ticket> listView = new ListView<>();
                setupListView(listView, filteredTickets);

                VBox vbox = new VBox(listView);
                vbox.setFillWidth(true);
                tab.setContent(vbox);
            }
        } else {
            testLabel.setText("Prosím prihláste sa pre zobrazenie tiketov.");
        }
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


    private List<Ticket> filterTicketsByStatus(List<Ticket> tickets, StatusForTicket status) {
        List<Ticket> filteredTickets = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket.getStatus() == status) {
                filteredTickets.add(ticket);
            }
        }
        return filteredTickets;
    }

    private void setupListView(ListView<Ticket> listView, List<Ticket> tickets) {
        listView.setItems(FXCollections.observableArrayList(tickets));
        listView.setFixedCellSize(24);

        Platform.runLater(() -> {
            ScrollBar horizontalScrollBar = (ScrollBar) listView.lookup(".scroll-bar:horizontal");
            double scrollbarHeight = horizontalScrollBar != null ? horizontalScrollBar.getHeight() + 4 : 0;
            listView.setPrefHeight(tickets.size() * listView.getFixedCellSize() + scrollbarHeight);
        });

        listView.setCellFactory(lv -> new ListCell<Ticket>() {
            @Override
            protected void updateItem(Ticket ticket, boolean empty) {
                super.updateItem(ticket, empty);
                if (empty || ticket == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(ticket.toString());
                }
            }
        });
    }

}
