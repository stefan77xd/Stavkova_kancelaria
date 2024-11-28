package org.example.ticket;
import lombok.Data;

@Data
public class Ticket {
    private long ticketId;
    private long userId;
    private long outcomeId;
    private StatusForTicket status;
    private double stake;

    @Override
    public String toString() {
        return ticketId + ", " + userId + ", "
                + outcomeId + ", " + stake + ", " + status;
    }
}
