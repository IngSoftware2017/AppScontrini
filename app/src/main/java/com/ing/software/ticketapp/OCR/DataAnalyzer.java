package com.ing.software.ticketapp.OCR;

import android.content.Context;
import android.graphics.Bitmap;

import com.ing.software.ticketapp.common.Ticket;

import java.util.List;

/**
 * Class used to extract informations from raw data
 */
public class DataAnalyzer {

    private final OcrAnalyzer analyzer = new OcrAnalyzer();


    public int initialize(Context context) {
        return analyzer.initialize(context);
    }

    /**
     * Get a Ticket from a Bitmap. Some fields of the new ticket can be null.
     * @param photo Bitmap. Not null.
     * @param ticketCb callback to get the ticket. Not null.
     */
    public void getTicket(Bitmap photo, OnTicketReadyListener ticketCb) {
        analyzer.getOcrResult(photo, new OnOcrResultReadyListener() {
            @Override
            public void onOcrResultReady(OcrResult result) {
                // for now, let's invoke the callback syncronously.
                ticketCb.onTicketReady(getTicketFromResult(result));
            }
        });
    }

    private Ticket getTicketFromResult(OcrResult result) {
        List<RawGridResult> dateMap = result.getDateList();
        List<RawStringResult> amountResults = result.getAmountResults();
        return new Ticket();
    }
}
