package org.example;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.example.possibleoutcome.PossibleOutcome;
import org.example.possibleoutcome.StatusForOutcomes;
import org.example.AddBalanceControler;
import org.example.security.Auth;
import org.example.security.LoginController;
import org.example.sportevent.MatchController;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.codegen.maven.example.Tables.POSSIBLE_OUTCOMES;
import static org.jooq.codegen.maven.example.Tables.USERS;

public class Controller {

    @FXML
    private TabPane sportTabPane;

    private final SportEventDAO sportEventDAO = new SportEventDAO();

    @FXML
    private Button loginoruser;
    Stage stage;

    public void onLoginSuccess() {
        // Update the button text
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


        var userId = Auth.INSTANCE.getPrincipal().getId().intValue();

        try {

            Properties config = ConfigReader.loadProperties("config.properties");
            String dbUrl = config.getProperty("db.url");

            // Pripojenie k databáze
            try (Connection connection = DriverManager.getConnection(dbUrl)) {
                DSLContext create = DSL.using(connection);

                // Získanie aktuálneho zostatku z databázy
                BigDecimal currentBalance = create.select(USERS.BALANCE)
                        .from(USERS)
                        .where(USERS.USER_ID.eq(userId))
                        .fetchOneInto(BigDecimal.class);

                if (currentBalance != null) {


                    Auth.INSTANCE.getPrincipal().setBalance(currentBalance.doubleValue()); // Aktualizácia balansu v Auth
                    loginoruser.setText(Auth.INSTANCE.getPrincipal().getUsername() + "\nZostatok: " + currentBalance);
                } else {
                    loginoruser.setText("Nepodarilo sa načítať zostatok.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loginoruser.setText("Chyba pri pripájaní k databáze.");
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/ResultView.fxml"));
            Parent root = loader.load();

            ResultMatchController resultMatchController = loader.getController();
            resultMatchController.eventName.setText(selectedEvent.getEventName());
            Properties config = ConfigReader.loadProperties("config.properties");
            String dbUrl = config.getProperty("db.url");

            try (Connection connection = DriverManager.getConnection(dbUrl)) {
                DSLContext create = DSL.using(connection);

                Set<String> resultNames = new HashSet<>(create.select(POSSIBLE_OUTCOMES.RESULT_NAME)
                        .from(POSSIBLE_OUTCOMES)
                        .where(POSSIBLE_OUTCOMES.EVENT_ID.eq((int) selectedEvent.getEventId()))
                        .and(POSSIBLE_OUTCOMES.STATUS.eq(StatusForOutcomes.winning.name()))
                        .fetch(POSSIBLE_OUTCOMES.RESULT_NAME));


                // Skontrolujeme, či nie sú prázdne hodnoty v 'resultNames'
                Iterator<String> iterator = resultNames.iterator();

// Nastavíme hodnoty pre outcomeResult1, outcomeResult2 a outcomeResult3
                if (iterator.hasNext()) {
                    resultMatchController.outcomeResult1.setText(iterator.next());
                } else {
                    resultMatchController.outcomeResult1.setText("");
                }

                if (iterator.hasNext()) {
                    resultMatchController.outcomeResult2.setText(iterator.next());
                } else {
                    resultMatchController.outcomeResult2.setText("");
                }

                if (iterator.hasNext()) {
                    resultMatchController.outcomeResult3.setText(iterator.next());
                } else {
                    resultMatchController.outcomeResult3.setText("");
                }

// Ak je text "Label", nastavíme prázdny text
                if (resultMatchController.outcomeResult1.getText().equals("Label")) {
                    resultMatchController.outcomeResult1.setText("");
                }
                if (resultMatchController.outcomeResult2.getText().equals("Label")) {
                    resultMatchController.outcomeResult2.setText("");
                }
                if (resultMatchController.outcomeResult3.getText().equals("Label")) {
                    resultMatchController.outcomeResult3.setText("");
                }


                Stage stage = new Stage();
                stage.setTitle("Ukončený zápas");
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/match.png"))));


                Scene scene = new Scene(root);


                scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/dark-theme.css")).toExternalForm());


                stage.setScene(scene);


                stage.initModality(Modality.APPLICATION_MODAL);
                stage.show();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        } finally {

        }
    }

    public void showResults() {
        closeMainView();
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
            System.err.println(e.getMessage());
        }
    }

    public void closeMainView() {

        if (stage != null) {
            stage.close();
        } else {
            System.err.println("Stage hlavného okna je null, nemôžem zavrieť okno.");
        }
    }


}


