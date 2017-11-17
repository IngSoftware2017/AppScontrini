package com.ing.software.ticketapp.OCR;

import com.ing.software.ticketapp.common.Ticket;

import java.util.List;

/**
 * Class used to extract informations from raw data
 */
public class DataAnalyzer {

    /**
     * Get a Ticket from an OcrResult. Some fields of the new ticket can be null.
     * @param resultInput OcrResult. Not null.
     * @param ticketCb callback to get the ticket. Not null.
     */
    public void getTicket(OcrResult resultInput, OnTicketReadyListener ticketCb) {
        Ticket newTicket = new Ticket();
        List<RawGridResult> dateMap = resultInput.getDateList();
        List<RawStringResult> amountResults = resultInput.getAmountResults();

        // todo: leggere resultInput e inserire le informazioni in newTicket


        // for now, let's invoke the callback syncronously.
        ticketCb.onTicketReady(newTicket);
    }
}
