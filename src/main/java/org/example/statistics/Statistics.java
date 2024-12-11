package org.example.statistics;
import lombok.Data;
@Data
public class Statistics {
    private Integer UserId;
    private int totalBets;
    private double totalStakes;
    private double totalWinnings;
    private double winRate;
    private double averageBet;
    private double maxBet;
}

