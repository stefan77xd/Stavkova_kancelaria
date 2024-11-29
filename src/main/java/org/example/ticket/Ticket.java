package org.example.ticket;
import lombok.Data;

@Data
public class Ticket {
    private long ticketId;
    private long userId;
    private long outcomeId;
    private StatusForTicket status;
    private double stake;
    private String eventName;
    private String resultName;

    @Override
    public String toString() {
        return eventName + ", "
                + resultName + ", " + stake + ", " + status;
    }
}
