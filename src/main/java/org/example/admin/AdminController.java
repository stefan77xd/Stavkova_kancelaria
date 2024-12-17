package org.example.admin;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.example.AlertFactory;
import org.example.AppThemeConfig;
import org.example.ConfigReader;
import org.example.Factory;
import org.example.security.Auth;
import org.example.sportevent.SportEvent;
import org.example.sportevent.SportEventDAO;
import org.example.sportevent.StatusForEvent;

public class AdminController {
    @FXML
    private Label welcomeSign;
    @FXML
    private TabPane tabPane;
    private final SportEventDAO sportEventDAO = Factory.INSTANCE.getSportEventDAO();
    private final AlertFactory A = new AlertFactory();

    @FXML
    private Button themeButton;

    String theme = AppThemeConfig.getTheme();

    @FXML
    private void logout() {
        try {
            Auth.INSTANCE.setPrincipal(null);
            Stage currentStage = (Stage) welcomeSign.getScene().getWindow();
            currentStage.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/view.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
            stage.setTitle("Stávková kancelária");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/icon.png"))));
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            System.err.println("Chyba pri odhlasovani: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        welcomeSign.setText("Vitaj " + Auth.INSTANCE.getPrincipal().getUsername());
        loadSportsIntoTabs("upcoming");
        if (theme.equals("/css/dark-theme.css")) {
            Image icon = new Image("icons/sun.png");
            ImageView imageView = new ImageView(icon);

            imageView.setFitWidth(16);
            imageView.setFitHeight(16);

            themeButton.setGraphic(imageView);
        } else {
            Image icon = new Image("icons/theme.png");
            ImageView imageView = new ImageView(icon);

            imageView.setFitWidth(16);
            imageView.setFitHeight(16);

            themeButton.setGraphic(imageView);
        }
    }

    //zdroj https://stackoverflow.com/questions/17429508/how-do-you-get-javafx-listview-to-be-the-height-of-its-items
    private void loadSportsIntoTabs(String eventStatus) {
        List<SportEvent> sportEvents;
        if ("upcoming".equals(eventStatus)) {
            sportEvents = sportEventDAO.getAllSportEvents().stream()
                    .filter(event -> event.getStatus() == StatusForEvent.upcoming)
                    .collect(Collectors.toList());
        } else {
            sportEvents = sportEventDAO.getAllSportEvents().stream()
                    .filter(event -> event.getStatus() == StatusForEvent.finished)
                    .collect(Collectors.toList());
        }
        tabPane.getTabs().clear();

        Tab allTab = new Tab("All");
        ListView<SportEvent> allEventsListView = new ListView<>();
        allEventsListView.setItems(FXCollections.observableArrayList(sportEvents));
        int allItemCount = sportEvents.size();
        allEventsListView.setPrefHeight(allItemCount * 25);

        allEventsListView.setOnMouseClicked(event -> {
            SportEvent selectedEvent = allEventsListView.getSelectionModel().getSelectedItem();
            if (selectedEvent != null) {
                if (selectedEvent.getStatus() == StatusForEvent.upcoming) {
                    try {
                        openEventPreviewWindow(selectedEvent);
                    } catch (IOException e) {
                        System.err.println("Error pri otvarani event preview: " + e.getMessage());
                    }
                } else if (selectedEvent.getStatus() == StatusForEvent.finished) {
                    hideEvent(selectedEvent);
                    showFinishedEvents();
                }
            }
        });

        VBox allVBox = new VBox(allEventsListView);
        allTab.setContent(allVBox);
        tabPane.getTabs().add(allTab);

        Map<String, List<SportEvent>> eventsBySportType = sportEvents.stream()
                .collect(Collectors.groupingBy(SportEvent::getSportType));

        for (Map.Entry<String, List<SportEvent>> entry : eventsBySportType.entrySet()) {
            String sportType = entry.getKey();
            List<SportEvent> events = entry.getValue();

            Tab tab = new Tab(sportType);
            ListView<SportEvent> listView = new ListView<>();
            listView.setItems(FXCollections.observableArrayList(events));

            int itemCount = events.size();
            listView.setPrefHeight(itemCount * 25);

            listView.setOnMouseClicked(event -> {
                SportEvent selectedEvent = listView.getSelectionModel().getSelectedItem();
                if (selectedEvent != null) {
                    try {
                        openEventPreviewWindow(selectedEvent);
                    } catch (IOException e) {
                        System.err.println("Error pri otvarani event preview: " + e.getMessage());
                    }
                }
            });
            VBox vBox = new VBox(listView);
            tab.setContent(vBox);
            tabPane.getTabs().add(tab);
        }
    }



    private void openEventPreviewWindow(SportEvent sportEvent) throws IOException {
        if (sportEvent == null) {
            System.err.println("SportEvent je null.");
            return;
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eventPreview.fxml"));
        Parent root = loader.load();
        EventPreviewController eventPreviewController = loader.getController();
        eventPreviewController.setSportEvent(sportEvent);
        eventPreviewController.setAdminController(this);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
        Stage stage = new Stage();
        stage.setTitle("Event Preview");
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/login.png"))));
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML
    void AddSportEvent() {
        openAddSportWindow();
    }

    private void openAddSportWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/addSportEvent.fxml"));
            Parent root = loader.load();
            AddSportController addSportController = loader.getController();
            addSportController.setAdminController(this);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
            Stage stage = new Stage();
            stage.setTitle("New event");
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/admin.png"))));
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void updateTabs() {
        tabPane.getTabs().clear();
        initialize();
    }

    @FXML
    private void showUpcomingEvents() {
        loadSportsIntoTabs("upcoming");
    }

    @FXML
    private void showFinishedEvents() {
        loadSportsIntoTabs("finished");
    }

    public void hideEvent(SportEvent selectedEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Skryť Event");
        alert.setHeaderText("Ste si istý, že chcete skryť tento event?");
        alert.setContentText("Tento krok skryje event.");

        if (theme.equals("/css/dark-theme.css")) {
            alert.getDialogPane().setStyle("-fx-background-color: #303030;");
        } else {
            alert.getDialogPane().setStyle("-fx-background-color: #f9f9f9;");
        }



        alert.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (theme.equals("/css/dark-theme.css")) {
                    alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #303030;");
                    alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: white;");
                } else {
                    alert.getDialogPane().lookup(".header-panel").setStyle("-fx-background-color: #f9f9f9;");
                    alert.getDialogPane().lookup(".header-panel .label").setStyle("-fx-text-fill: black;");
                }
            }
        });

        ButtonType yesButton = new ButtonType("Áno");
        ButtonType noButton = new ButtonType("Nie");
        alert.getButtonTypes().setAll(yesButton, noButton);

        alert.showingProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Button yes = (Button) alert.getDialogPane().lookupButton(yesButton);
                if (theme.equals("/css/dark-theme.css")) {
                    yes.setStyle("-fx-background-color: #212121; -fx-text-fill: white; -fx-cursor: hand");

                    yes.setOnMouseEntered(event -> yes.setStyle("-fx-background-color: #121212; -fx-text-fill: white; -fx-cursor: hand"));
                    yes.setOnMouseExited(event -> yes.setStyle("-fx-background-color: #212121; -fx-text-fill: white; -fx-cursor: hand"));
                } else {
                    yes.setStyle("-fx-background-color: #d3d3d3; -fx-text-fill: black; -fx-cursor: hand");

                    yes.setOnMouseEntered(event -> yes.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-cursor: hand"));
                    yes.setOnMouseExited(event -> yes.setStyle("-fx-background-color: #d3d3d3; -fx-text-fill: black; -fx-cursor: hand"));
                }


                Button no = (Button) alert.getDialogPane().lookupButton(noButton);
                if (theme.equals("/css/dark-theme.css")) {
                    no.setStyle("-fx-background-color: #212121; -fx-text-fill: white; -fx-cursor: hand");

                    no.setOnMouseEntered(event -> no.setStyle("-fx-background-color: #121212; -fx-text-fill: white; -fx-cursor: hand"));
                    no.setOnMouseExited(event -> no.setStyle("-fx-background-color: #212121; -fx-text-fill: white; -fx-cursor: hand"));
                } else {
                    no.setStyle("-fx-background-color: #d3d3d3; -fx-text-fill: black; -fx-cursor: hand");

                    no.setOnMouseEntered(event -> no.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: black; -fx-cursor: hand"));
                    no.setOnMouseExited(event -> no.setStyle("-fx-background-color: #d3d3d3; -fx-text-fill: black; -fx-cursor: hand"));
                }
            }
        });

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/warning.png"))));

        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                sportEventDAO.hideEvent(selectedEvent.getEventId());
            }
        });
    }

    @FXML
    public void changeTheme() {
        String newTheme;
        String themeIconPath;

        if ("/css/dark-theme.css".equals(theme)) {
            newTheme = "/css/light-theme.css";
            themeIconPath = "icons/theme.png";
        } else {
            newTheme = "/css/dark-theme.css";
            themeIconPath = "icons/sun.png";
        }

        AppThemeConfig.setTheme(newTheme);
        theme = AppThemeConfig.getTheme();

        Image icon = new Image(themeIconPath);
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);
        themeButton.setGraphic(imageView);

        applyThemeToScene(newTheme);
    }

    private void applyThemeToScene(String themePath) {
        Scene scene = themeButton.getScene();
        if (scene != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(themePath);
        }
    }
}






