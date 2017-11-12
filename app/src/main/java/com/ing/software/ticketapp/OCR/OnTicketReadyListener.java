package com.ing.software.ticketapp.OCR;

import com.ing.software.ticketapp.common.Ticket;

/**
 * Callback interface used to get a Ticket from OcrResult.
 */
public interface OnTicketReadyListener {

    /**
     * Get a Ticket. In the argument "ticket", fields corresponding to unextracted information are null.
     * ID and fileURI fields are uninitialized.
     * @param ticket new Ticket. Never null.
     */
    void onTicketReady(Ticket ticket);
}
