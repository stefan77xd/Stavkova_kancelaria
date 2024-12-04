package org.example.ticket;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.example.ConfigReader;
import org.example.Controller;
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

    private final TicketDAO UserTicketDAO = new TicketDAO();

    @FXML
    private TabPane ticketPane;

    @FXML
    private Label testLabel;



    @FXML
    public void initialize() {
        // Add a key event handler to the root node of the scene
        ticketPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().toString().equalsIgnoreCase("R")) {
                try {
                    refreshTickets();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // Create a custom order for the StatusForTicket enums
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
            System.out.println(userTickets);
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

    private void refreshTickets() throws SQLException {
        if (Auth.INSTANCE.getPrincipal() == null) {
            testLabel.setText("Nie ste prihlásený.");
            return;
        }

        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);

            // Načítanie tiketov používateľa
            List<Ticket> userTickets = UserTicketDAO.getUsersTickets(Auth.INSTANCE.getPrincipal().getId().intValue());
            for (Ticket userTicket : userTickets) {
                System.out.println("Spracováva sa tiket ID: " + userTicket.getTicketId());

                // Načítanie výsledku tiketu
                PossibleOutcome finalOutcome = create.selectFrom(POSSIBLE_OUTCOMES)
                        .where(POSSIBLE_OUTCOMES.OUTCOME_ID.eq(userTicket.getOutcomeId()))
                        .fetchOneInto(PossibleOutcome.class);
                String status = create.select(POSSIBLE_OUTCOMES.STATUS)
                        .from(POSSIBLE_OUTCOMES)
                        .where(POSSIBLE_OUTCOMES.OUTCOME_ID.eq(userTicket.getOutcomeId()))
                        .fetchOneInto(String.class);

                System.out.println("sdafasdfasdf " + status);
                if (finalOutcome != null) {
                    System.out.println(finalOutcome.getStatusForOutcomes() + "----" + userTicket.getResultName());
                    if (StatusForOutcomes.valueOf(status) == StatusForOutcomes.winning) {
                        System.out.println("toooooooooo");
                        create.update(TICKETS)
                                .set(TICKETS.STATUS, StatusForTicket.won.name())
                                .where(TICKETS.TICKET_ID.eq(userTicket.getTicketId()))
                                .execute();
                        System.out.println("ticket sa nacital");

                        addBalanceToUser(userTicket);
                    } else if (StatusForOutcomes.valueOf(status) == StatusForOutcomes.loosing) {
                        System.out.println("acacaca");
                        create.update(TICKETS)
                                .set(TICKETS.STATUS, StatusForTicket.lost.name())
                                .where(TICKETS.TICKET_ID.eq(userTicket.getTicketId()))
                                .execute();
                    }
                } else {
                    System.out.println("Výsledok pre tiket ID " + userTicket.getTicketId() + " nebol nájdený.");
                }
            }

            // Obnovenie zobrazenia
            List<Ticket> updatedTickets = UserTicketDAO.getUsersTickets(Auth.INSTANCE.getPrincipal().getId().intValue());
            populateTabs(updatedTickets);
            testLabel.setText("Tikety boli obnovené.");
        } catch (SQLException e) {
            System.err.println("Chyba pri pripájaní k databáze: " + e.getMessage());
            throw e;
        }
    }

    // Pomocná metóda na pridanie balansu
    private void addBalanceToUser(Ticket ticket) throws SQLException {
        System.out.println("zavolala som sa ");
        Properties config = ConfigReader.loadProperties("config.properties");
        String dbUrl = config.getProperty("db.url");

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext create = DSL.using(connection);

            // Získanie používateľa
            var userId = Auth.INSTANCE.getPrincipal().getId().intValue();

            // Skontrolovať, či balans už bol aktualizovaný
            Boolean balanceUpdated = create.select(TICKETS.BALANCE_UPDATED)
                    .from(TICKETS)
                    .where(TICKETS.TICKET_ID.eq(ticket.getTicketId()))
                    .fetchOneInto(Boolean.class);

            if (Boolean.TRUE.equals(balanceUpdated)) {
                System.out.println("Balans už bol aktualizovaný pre tiket ID: " + ticket.getTicketId());
                return; // Ak balans už bol aktualizovaný, ukonči metódu
            }

            // Získať stávku a kurz
            BigDecimal stake = create.select(TICKETS.STAKE)
                    .from(TICKETS)
                    .where(TICKETS.TICKET_ID.eq(ticket.getTicketId()))
                    .fetchOneInto(BigDecimal.class);

            BigDecimal odds = create.select(POSSIBLE_OUTCOMES.ODDS)
                    .from(POSSIBLE_OUTCOMES)
                    .where(POSSIBLE_OUTCOMES.OUTCOME_ID.eq(ticket.getOutcomeId()))
                    .fetchOneInto(BigDecimal.class);

            // Vypočítať výhru
            BigDecimal winnings = stake.multiply(odds);

            // Aktualizovať balans používateľa
            create.update(USERS)
                    .set(USERS.BALANCE, USERS.BALANCE.plus(winnings))
                    .where(USERS.USER_ID.eq(userId))
                    .execute();

            // Nastaviť BALANCE_UPDATED na true
            create.update(TICKETS)
                    .set(TICKETS.BALANCE_UPDATED, true)
                    .where(TICKETS.TICKET_ID.eq(ticket.getTicketId()))
                    .execute();

            System.out.println("Balance bol pridaný pre tiket ID: " + ticket.getTicketId());
        }
    }



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
