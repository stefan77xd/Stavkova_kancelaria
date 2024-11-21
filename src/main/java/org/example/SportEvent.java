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


    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getSportType() {
        return sportType;
    }

    public void setSportType(String sportType) {
        this.sportType = sportType;
    }

    public StatusForEvent getStatus() {
        return status;
    }

    public void setStatus(StatusForEvent status) {
        this.status = status;
    }
}
