package org.example;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.security.Auth;
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
    private Button loginoruser;

    public void onLoginSuccess() {
        // Update the button text
        loginoruser.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\n Zostatok: " + Auth.INSTANCE.getPrincipal().getBalance());

        // Create a context menu for the dropdown
        ContextMenu dropdownMenu = new ContextMenu();
        dropdownMenu.getStyleClass().add("dropdown-menu");

        // Create "Profile" menu item
        MenuItem profileMenuItem = new MenuItem("Profil");
        profileMenuItem.getStyleClass().add("dropdown-item");
        MenuItem logoutMenuItem = new MenuItem("Odhlásiť sa");
        logoutMenuItem.getStyleClass().add("dropdown-item");

        // Set event handlers for menu items
        //profileMenuItem.setOnAction(event -> openProfileView());
        //logoutMenuItem.setOnAction(event -> handleLogout());

        // Add menu items to the dropdown
        dropdownMenu.getItems().addAll(profileMenuItem, logoutMenuItem);

        // Attach the context menu to the button
        loginoruser.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                // Get button width
                double buttonWidth = loginoruser.getWidth();

                // Apply a fixed width to the context menu using CSS
                dropdownMenu.setStyle("-fx-pref-width: " + buttonWidth + "px;");

                // Show the dropdown aligned with the button
                double buttonStartX = loginoruser.localToScreen(0, 0).getX();
                double buttonBottomY = loginoruser.localToScreen(0, loginoruser.getHeight()).getY()+6;
                dropdownMenu.show(loginoruser, buttonStartX, buttonBottomY);
            }
        });
    }







    @FXML
    public void openTicketView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ticketView.fxml"));
            Parent root = loader.load();

            // Add the stylesheet to the ticket view explicitly
            Scene ticketScene = new Scene(root);
            ticketScene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Tikety");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/ticket.png")));
            stage.setScene(ticketScene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    public void openLoginView() {
        if (Auth.INSTANCE.getPrincipal() == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/loginView.fxml"));
                Parent root = loader.load();

                // Get the LoginController from the loader
                LoginController loginController = loader.getController();

                // Pass the main controller to the login controller
                loginController.setMainController(this);

                // Create a new scene with the root node
                Scene scene = new Scene(root);

                // Add the dark theme CSS stylesheet to the scene
                scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

                // Create the stage and set the scene
                Stage stage = new Stage();
                stage.setTitle("Login");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/login.png")));
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL); // Modal window
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

            // Create the scene
            Scene scene = new Scene(root);

            // Add the dark theme CSS stylesheet to the scene
            scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

            // Set the scene to the stage
            stage.setScene(scene);

            // Make the stage modal
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

    @FXML
    public void openStatView(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/statView.fxml"));
            Parent root = loader.load();

            // Add the stylesheet to the ticket view explicitly
            Scene ticketScene = new Scene(root);
            ticketScene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());

            Stage stage = new Stage();
            stage.setTitle("Štatistika");
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/statistics.png")));
            stage.setScene(ticketScene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


