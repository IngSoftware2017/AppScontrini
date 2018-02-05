package com.sw.ing.gestionescontrini.iterator;

import android.graphics.Bitmap;

import java.io.IOException;

/**
 * Created by Federico Taschin on 02/12/2017.
 */
public class Bundle {

    private TicketInfo ticketInfo;

    /**Created by Federico Taschin
     * Lazy instantiation loading of the Bitmap
     * @return The Bitmap object
     * @throws IOException
     */
    public Bitmap getBitmap() throws IOException {
        return ImageLoader.getBitmap(ticketInfo);
    }

    /** Created by Federico Taschin
     * @return the TicketInfo object
     */
    public TicketInfo getTicketInfo() {
        return ticketInfo;
    }

    /**Created by Federico Taschin
     * @param ticketInfo TicketInfo object to be set
     */
    public void setTicketInfo(TicketInfo ticketInfo) {
        this.ticketInfo = ticketInfo;
    }
}
