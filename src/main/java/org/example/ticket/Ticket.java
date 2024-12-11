package org.example.ticket;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.ticket.StatusForTicket;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Ticket {
    private Integer ticketId;
    private Integer userId;
    private Integer outcomeId;
    private StatusForTicket status;
    private BigDecimal stake;
    private String resultName;
    private String eventName;
    private LocalDateTime eventStartTime;


    @Override
    public String toString() {
        return eventName + ", " + resultName + ", "
                + eventStartTime.toLocalDate() + ", " + stake + ", " + status;
    }
}
