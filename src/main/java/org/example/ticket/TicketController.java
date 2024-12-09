package org.example.ticket;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.example.ConfigReader;
import org.example.Controller;
import org.example.Factory;
import org.example.possibleoutcome.PossibleOutcome;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.security.Auth;
import org.jooq.DSLContext;
import org.jooq.Name;
import org.jooq.impl.DSL;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.jooq.codegen.maven.example.Tables.*;

public class TicketController {

    private final TicketDAO UserTicketDAO = Factory.INSTANCE.getTicketDAO();

    @FXML
    private TabPane ticketPane;

    @FXML
    private Label testLabel;



    @FXML
    public void initialize() {
        StatusForTicket[] orderedStatuses = {
                StatusForTicket.pending, // assuming 'pending' is the lowercase enum constant
                StatusForTicket.won,     // assuming 'won' is the lowercase enum constant
                StatusForTicket.lost     // assuming 'lost' is the lowercase enum constant
        };

        // Create tabs in the defined order
        if (Auth.INSTANCE.getPrincipal() != null) {
            for (StatusForTicket status : orderedStatuses) {
                // Capitalize the first letter of each tab's name for display
                String capitalizedLabel = capitalizeFirstLetter(status.name());
                Tab tab = new Tab(capitalizedLabel);

                // Create a ListView for each tab
                ListView<Ticket> listView = new ListView<>();

                // Add the ListView to the tab inside a VBox
                VBox vbox = new VBox(listView);
                vbox.setFillWidth(true);
                tab.setContent(vbox);

                // Add the tab to the TabPane
                ticketPane.getTabs().add(tab);
            }
        }

        // Check if the user is logged in and populate the ListViews accordingly
        if (Auth.INSTANCE.getPrincipal() != null) {
            testLabel.setText("Tikety");
            List<Ticket> userTickets = UserTicketDAO.getUsersTickets(Auth.INSTANCE.getPrincipal().getId().intValue());
            // Populate the ListViews with the user's tickets for each tab (by status)
            for (Tab tab : ticketPane.getTabs()) {
                // Convert the capitalized tab name back to lowercase to match the enum constant
                String lowercaseTabName = tab.getText().toLowerCase();
                StatusForTicket status = StatusForTicket.valueOf(lowercaseTabName); // Match with lowercase enum

                List<Ticket> filteredTickets = filterTicketsByStatus(userTickets, status);

                // Set up the ListView with the filtered tickets
                ListView<Ticket> listView = new ListView<>();
                setupListView(listView, filteredTickets);

                // Replace the VBox content with the populated ListView
                VBox vbox = new VBox(listView);
                vbox.setFillWidth(true);
                tab.setContent(vbox);
            }
        } else {
            testLabel.setText("Prosím prihláste sa pre zobrazenie tiketov.");
        }
    }

    // Helper method to capitalize the first letter
    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }



    // Pomocná metóda na pridanie balansu




    // Zoradenie tiketov do tabov
    private void populateTabs(List<Ticket> userTickets) {
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

        Platform.runLater(() -> {
            ScrollBar horizontalScrollBar = (ScrollBar) listView.lookup(".scroll-bar:horizontal");
            double scrollbarHeight = horizontalScrollBar != null ? horizontalScrollBar.getHeight() + 4 : 0;
            listView.setPrefHeight(tickets.size() * listView.getFixedCellSize() + scrollbarHeight);
        });


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
