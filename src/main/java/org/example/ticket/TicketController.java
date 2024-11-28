package org.example.ticket;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.example.security.Auth;

import java.util.ArrayList;
import java.util.List;


public class TicketController {

    private TicketDAO UserTicketDAO = new TicketDAO();

    @FXML
    private TabPane ticketPane;

    @FXML
    public void initialize() {
        // Create tabs for each status
        for (StatusForTicket status : StatusForTicket.values()) {
            Tab tab = new Tab(status.name());
            ticketPane.getTabs().add(tab);

            // Create a ListView for each tab, initially empty
            ListView<Ticket> listView = new ListView<>();

            // Add the ListView to the tab inside a VBox
            VBox vbox = new VBox(listView);
            vbox.setFillWidth(true);
            tab.setContent(vbox);
        }

        // Check if the user is logged in and populate the ListViews accordingly
        if (Auth.INSTANCE.getPrincipal() != null) {
            // If user is logged in, fetch the user's tickets and populate the ListViews
            List<Ticket> userTickets = UserTicketDAO.getUsersTickets(Auth.INSTANCE.getPrincipal().getId());

            // Now, for each tab, add the user's tickets to the ListView corresponding to the status
            for (Tab tab : ticketPane.getTabs()) {
                // Create the ListView again (it was initialized earlier as empty)
                ListView<Ticket> listView = new ListView<>();

                // You can further categorize the tickets per tab, for example:
                StatusForTicket status = StatusForTicket.valueOf(tab.getText());
                List<Ticket> filteredTickets = filterTicketsByStatus(userTickets, status);

                // Setup the ListView with the filtered tickets
                setupListView(listView, filteredTickets);

                // Replace the VBox content with the populated ListView
                VBox vbox = new VBox(listView);
                vbox.setFillWidth(true);
                tab.setContent(vbox);
            }
        } else {
            // If the user is not logged in, show a message in each ListView (optional)
            System.out.println("Please log in to view your tickets.");
        }
    }

    // Filter tickets by the current status (you can modify this method based on your needs)
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
        // Set the items for the ListView
        listView.setItems(FXCollections.observableArrayList(tickets));
        listView.setFixedCellSize(24);
        listView.setPrefHeight(tickets.size() * listView.getFixedCellSize() + 2);

        // Customize how each item (Ticket) is displayed in the ListView
        listView.setCellFactory(lv -> new ListCell<Ticket>() {
            @Override
            protected void updateItem(Ticket ticket, boolean empty) {
                super.updateItem(ticket, empty);
                if (empty || ticket == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(ticket.toString()); // You can customize what to display for each ticket here
                }
            }
        });

        // Optional: Handle click events on the tickets (if needed)
        listView.setOnMouseClicked(event -> {
            Ticket selectedTicket = listView.getSelectionModel().getSelectedItem();
            if (selectedTicket != null) {
                // You can implement what should happen when a ticket is selected
                openTicketDetails(selectedTicket);
            }
        });
    }

    // Method to handle ticket details when clicked (you can implement the details view here)
    private void openTicketDetails(Ticket selectedTicket) {
        // Logic for opening the ticket details (for example, showing a new scene or a dialog)
        System.out.println("Ticket selected: " + selectedTicket);
    }
}
