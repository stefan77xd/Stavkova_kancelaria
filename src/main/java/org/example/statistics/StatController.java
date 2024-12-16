package org.example.statistics;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.example.Factory;
import org.example.security.Auth;

import java.util.List;
public class StatController {
    @FXML
    private VBox statBox;
    private final StatisticsDAO statisticsDAO = Factory.INSTANCE.getStatisticsDAO();
    @FXML
    private Label mainLabel;
    @FXML
    public void initialize() {
        if (Auth.INSTANCE.getPrincipal() != null) {
            Integer userId = Auth.INSTANCE.getPrincipal().getId().intValue();

            List<Statistics> stats = statisticsDAO.getUsersStats(userId);

            for (Statistics stat : stats) {
                Label totalBetsLabel = new Label("Celkový počet stávok: " + stat.getTotalBets());
                totalBetsLabel.setStyle("-fx-font-size: 16px");
                Label totalStakesLabel = new Label("Celkové vklady: " + stat.getTotalStakes());
                totalStakesLabel.setStyle("-fx-font-size: 16px");
                Label totalWinningsLabel = new Label("Celkové výhry: " + stat.getTotalWinnings());
                totalWinningsLabel.setStyle("-fx-font-size: 16px");
                Label winRateLabel = new Label("Percentuálna úspešnosť: " + stat.getWinRate());
                winRateLabel.setStyle("-fx-font-size: 16px");
                Label averageBetLabel = new Label("Priemerná stávka: " + stat.getAverageBet());
                averageBetLabel.setStyle("-fx-font-size: 16px");
                Label maxBetLabel = new Label("Maximálna stávka: " + stat.getMaxBet());
                maxBetLabel.setStyle("-fx-font-size: 16px");

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
