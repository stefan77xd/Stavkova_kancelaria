package org.example;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.admin.ResultMatchController;
import org.example.security.Auth;
import org.example.security.LoginController;
import org.example.sportevent.MatchController;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;
import org.example.user.AddBalanceControler;
import org.example.user.ProfileController;
import org.example.user.UserDAO;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    @FXML
    private TabPane sportTabPane;
    private final SportEventDAO sportEventDAO = Factory.INSTANCE.getSportEventDAO();
    private final UserDAO userDAO = Factory.INSTANCE.getUserDAO();
    @FXML
    public Button loginoruser;
    Stage stage;
    public void onLoginSuccess() {
        loginoruser.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\n Zostatok: " + Auth.INSTANCE.getPrincipal().getBalance());

        ContextMenu dropdownMenu = new ContextMenu();
        dropdownMenu.getStyleClass().add("dropdown-menu");
        MenuItem profileMenuItem = new MenuItem("Profil");
        profileMenuItem.getStyleClass().add("dropdown-item");
        MenuItem logoutMenuItem = new MenuItem("Odhlásiť sa");
        logoutMenuItem.getStyleClass().add("dropdown-item");
        MenuItem addBalance = new MenuItem("Vklad");
        addBalance.getStyleClass().add("dropdown-item");
        logoutMenuItem.setOnAction(event -> handleLogout());
        addBalance.setOnAction(event -> openBallanceWindow());
        profileMenuItem.setOnAction(event -> openProfileView());
        dropdownMenu.getItems().addAll(profileMenuItem, addBalance, logoutMenuItem);

        loginoruser.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                double buttonWidth = loginoruser.getWidth();
                dropdownMenu.setStyle("-fx-pref-width: " + buttonWidth + "px;");
                double buttonStartX = loginoruser.localToScreen(0, 0).getX();
                double buttonBottomY = loginoruser.localToScreen(0, loginoruser.getHeight()).getY() + 6;
                dropdownMenu.show(loginoruser, buttonStartX, buttonBottomY);
            }
        });
    }

    private void openProfileView() {
        if (Auth.INSTANCE.getPrincipal() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/profileView.fxml"));
                Parent root = loader.load();
                Scene ticketScene = new Scene(root);
                ticketScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
                Stage stage = new Stage();
                stage.setTitle("Profil");
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/admin.png"))));
                stage.setScene(ticketScene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
                ProfileController profileController = loader.getController();
                profileController.controller = this;
                profileController.stage = stage;
            } catch (IOException e) {
                System.err.println("Nepodarilo sa otvoriť profile view: " + e.getMessage());
            }
        }
    }

    private void openBallanceWindow() {
        if (Auth.INSTANCE.getPrincipal() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/addBalanceView.fxml"));
                Parent root = loader.load();
                AddBalanceControler addBalanceControler = loader.getController();
                addBalanceControler.UserID = Auth.INSTANCE.getPrincipal().getId();
                addBalanceControler.setMainController(this);
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
                stage = new Stage();
                stage.setTitle("Pridajte prostriedky");
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/card.png"))));
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public void updateBalance() {
        if (Auth.INSTANCE.getPrincipal() == null) {
            loginoruser.setText("Prosím, prihláste sa.");
            return;
        }

        var userId = Auth.INSTANCE.getPrincipal().getId();
                Double currentBalance = userDAO.getBalance(userId);
                if (currentBalance != null) {
                    Auth.INSTANCE.getPrincipal().setBalance(currentBalance); // Aktualizácia balansu v Auth
                    loginoruser.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\nZostatok: " + currentBalance);
                } else {
                    loginoruser.setText("Nepodarilo sa načítať zostatok.");
                }
    }


    private void handleLogout() {
        Auth.INSTANCE.setPrincipal(null);
        loginoruser.setText("Login/Register");
    }


    @FXML
    public void openTicketView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ticketView.fxml"));
            Parent root = loader.load();
            Scene ticketScene = new Scene(root);
            ticketScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Tikety");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/ticket.png"))));
            stage.setScene(ticketScene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }


    @FXML
    public void openLoginView() {
        if (Auth.INSTANCE.getPrincipal() == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/loginView.fxml"));
                Parent root = loader.load();
                LoginController loginController = loader.getController();
                loginController.setMainController(this);
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
                stage = new Stage();
                stage.setTitle("Login");
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
                stage.setScene(scene);
                stage.initModality(Modality.APPLICATION_MODAL); // Modal window
                stage.show();
            } catch (IOException e) {
                System.err.println(e.getMessage());
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
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/match.png"))));
            matchController.setMainController(this);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        sportTabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
            }
        });
        showOdds();
    }

    private void setupListView(ListView<SportEvent> listView, List<SportEvent> sportEvents) {
        listView.setItems(FXCollections.observableArrayList(sportEvents));
        listView.setFixedCellSize(24);
        listView.setPrefHeight(sportEvents.size() * listView.getFixedCellSize() + 2);
        listView.setCellFactory(lv -> new ListCell<>() {
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
                if (selectedEvent.getStatus() == StatusForEvent.upcoming) {
                    openMatchScene(selectedEvent);
                } else {
                    try {
                        openFinishedMatchScene(selectedEvent);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void openFinishedMatchScene(SportEvent selectedEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ResultView.fxml"));
        Parent root = loader.load();
        ResultMatchController resultMatchController = loader.getController();
        resultMatchController.setSportEvent(selectedEvent); // Pass the event to the controller
        Stage stage = new Stage();
        stage.setTitle("Ukončený zápas");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/match.png"))));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    public void showResults() {
        updateEvents(StatusForEvent.finished);
    }

    public void showOdds() {
        updateEvents(StatusForEvent.upcoming);
    }

    public void updateEvents(Enum<StatusForEvent> status) {
        sportTabPane.getTabs().clear();
        List<SportEvent> sportEvents = sportEventDAO.getAllSportEvents();
        List<SportEvent> Events = new ArrayList<>();
        for (SportEvent sportEvent : sportEvents) {
            if (sportEvent.getStatus() == status) {
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
        List<Tab> allTabs = new ArrayList<>();
        allTabs.add(allTab);

        for (Map.Entry<String, List<SportEvent>> entry : groupedEvents.entrySet()) {
            Tab sportTab = new Tab(entry.getKey());
            ListView<SportEvent> listView = new ListView<>();
            setupListView(listView, entry.getValue());
            VBox vbox = new VBox(listView);
            vbox.setFillWidth(true);
            sportTab.setContent(vbox);
            allTabs.add(sportTab);
        }
        allTabs.sort(Comparator.comparing(Tab::getText));
        sportTabPane.getTabs().addAll(allTabs);
    }

    @FXML
    public void openStatView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/statView.fxml"));
            Parent root = loader.load();
            Scene ticketScene = new Scene(root);
            ticketScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("Štatistika");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/statistics.png"))));
            stage.setScene(ticketScene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println("Nepodarilo sa otvoriť štatistiku: " + e.getMessage());
        }
    }
}


