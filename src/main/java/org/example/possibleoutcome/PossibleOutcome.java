package org.example.possibleoutcome;

import lombok.Data;

@Data
public class PossibleOutcome {
    private long outcomeId;
    private long eventId;
    private String resultName;
    private Double odds;
    private StatusForOutcomes statusForOutcomes;
}
