package org.example.possibleoutcome;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PossibleOutcome {
    private long outcomeId;
    private long eventId;
    private String resultName;
    private double odds;
    private StatusForOutcomes statusForOutcomes;
}
