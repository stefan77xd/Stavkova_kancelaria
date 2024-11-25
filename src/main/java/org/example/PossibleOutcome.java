package org.example;
import lombok.Data;

@Data
public class PossibleOutcome {
    private long outcomeId;
    private long eventId;
    private String resultName;
    private double odds;
    private StatusForOutcomes statusForOutcomes;
}
