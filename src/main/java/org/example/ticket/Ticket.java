package org.example.ticket;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.example.ticket.StatusForTicket;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Ticket {
    private Integer ticketId;
    private Integer userId;
    private Integer outcomeId;
    private StatusForTicket status;
    private double stake;

    // Dynamické atribúty, ktoré sú pridané v aplikácii, ale nie sú v databáze
    private String resultName;
    private String eventName;
    private LocalDateTime eventStartTime;



}
