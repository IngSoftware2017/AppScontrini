package com.unipd.ingsw.gruppo3;

import com.ing.software.common.Ticket;

import database.TicketEntity;

/**
 * Created by Federico Taschin on 07/12/2017.
 */

public class Wrapper {


    public static TicketEntity toTicketEntity(Ticket t){
        TicketEntity ticketEntity = new TicketEntity();
        ticketEntity.setID(t.ID);
        ticketEntity.setMissionID(t.missionId);
        ticketEntity.setTitle(t.title);
        ticketEntity.setDate(t.date);
        ticketEntity.setFileUri(t.fileURI);
        return ticketEntity;
    }

    public static Ticket toTicket(TicketEntity ticketEntity){
        Ticket ticket = new Ticket();
        ticket.ID = ticketEntity.getID();
        ticket.fileURI = ticketEntity.getFileUri();
        ticket.amount = ticketEntity.getAmount();
        ticket.missionId = ticketEntity.getMissionID();
        ticket.title = ticketEntity.getTitle();
        ticket.date = ticketEntity.getDate();
        return ticket;
    }

    public static TicketEntity toTicketEntity(IntentWrapperTicketEntity i){
                TicketEntity ticketEntity = new TicketEntity();
                ticketEntity.setAmount(i.amount);
                ticketEntity.setDate(i.date);
                ticketEntity.setTitle(i.title);
                ticketEntity.setMissionID(i.missionID);
                ticketEntity.setID(i.ID);
                return ticketEntity;
    }

    public static IntentWrapperTicketEntity toIntentWrapper(TicketEntity ticketEntity){
        IntentWrapperTicketEntity i = new IntentWrapperTicketEntity();
        i.amount = ticketEntity.getAmount();
        i.ID = ticketEntity.getID();
        i.missionID = ticketEntity.getMissionID();
        i.title = ticketEntity.getTitle();
return i;
    }
}
