package org.example.statistics;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.security.Auth;

import java.util.List;

public class StatController {

    @FXML
    private VBox statBox;

    private final StatisticsDAO statisticsDAO = new StatisticsDAO();

    @FXML
    private Label mainLabel;

    @FXML
    public void initialize() {
        // Check if the user is authenticated
        if (Auth.INSTANCE.getPrincipal() != null) {
            // Assume Auth.INSTANCE.getPrincipal() returns a User object or userId
            Integer userId = Auth.INSTANCE.getPrincipal().getId().intValue();

            // Fetch statistics for the user
            List<Statistics> stats = statisticsDAO.getUsersStats(userId);

            // Display the statistics in the VBox
            for (Statistics stat : stats) {
                Label totalBetsLabel = new Label("Total Bets: " + stat.getTotalBets());
                totalBetsLabel.setStyle("-fx-font-size: 16px");
                Label totalStakesLabel = new Label("Total Stakes: " + stat.getTotalStakes());
                totalStakesLabel.setStyle("-fx-font-size: 16px");
                Label totalWinningsLabel = new Label("Total Winnings: " + stat.getTotalWinnings());
                totalWinningsLabel.setStyle("-fx-font-size: 16px");
                Label winRateLabel = new Label("Win Rate: " + stat.getWinRate());
                winRateLabel.setStyle("-fx-font-size: 16px");
                Label averageBetLabel = new Label("Average Bet: " + stat.getAverageBet());
                averageBetLabel.setStyle("-fx-font-size: 16px");
                Label maxBetLabel = new Label("Max Bet: " + stat.getMaxBet());
                maxBetLabel.setStyle("-fx-font-size: 16px");

                // Add labels to the VBox
                statBox.getChildren().addAll(
                        totalBetsLabel,
                        totalStakesLabel,
                        totalWinningsLabel,
                        winRateLabel,
                        averageBetLabel,
                        maxBetLabel
                );
            }
        } else {
            mainLabel.setText("Prosím prihláste sa, pre zobrazenie štatistiky.");
        }
    }
}
