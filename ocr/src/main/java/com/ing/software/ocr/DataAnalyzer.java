package com.ing.software.ocr;

import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

import com.ing.software.ocr.OcrObjects.RawGridResult;
import com.ing.software.ocr.OcrObjects.RawStringResult;
import com.ing.software.common.Ticket;


/**
 * Class used to extract informations from raw data
 */
public class DataAnalyzer {

    private final OcrAnalyzer analyzer = new OcrAnalyzer();

    /**
     * Initialize OcrAnalyzer
     * @param context Android context
     * @return 0 if everything ok, negative number if an error occurred
     */
    public int initialize(Context context) {
        return analyzer.initialize(context);
    }

    /**
     * Get a Ticket from a Bitmap. Some fields of the new ticket can be null.
     * @param photo Bitmap. Not null.
     * @param ticketCb callback to get the ticket. Not null.
     */
    public void getTicket(Bitmap photo, final OnTicketReadyListener ticketCb) {
        analyzer.getOcrResult(photo, new OnOcrResultReadyListener() {
            @Override
            public void onOcrResultReady(OcrResult result) {
                // for now, let's invoke the callback syncronously.
                ticketCb.onTicketReady(getTicketFromResult(result));
            }
        });
    }

    /**
     * Coverts an OcrResult into a Ticket analyzing its data
     * @param result OcrResult to analyze. Not null.
     * @return Ticket. Some fields can be null;
     */
    private Ticket getTicketFromResult(OcrResult result) {
        List<RawGridResult> dateMap = result.getDateList();
        List<RawStringResult> amountResults = result.getAmountResults();
        return new Ticket();
    }
}
