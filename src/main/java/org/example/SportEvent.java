package org.example;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SportEvent {
    private long eventId;
    private String eventName;
    private LocalDateTime startTime;
    private String sportType;
    private StatusForEvent status;
}

