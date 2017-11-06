package gruppo2.ocrcomponent;

/**
 * Classe responsabile dell'estrazione di informazioni dai dati.
 */
public class DataAnalyzer {

    /**
     * Ottieni un Ticket a partire dai dati contenuti in resultInput.
     * alcuni campi del ticket possono essere null.
     * @param resultInput input. Non puo' essere null.
     * @param ticketCb callback per ottenere il ticket. Non nullo.
     */
    public void getTicket(OcrResult resultInput, OnTicketReadyListener ticketCb) {
        Ticket newTicket = new Ticket();

        // todo: leggere resultInput e inserire le informazioni in newTicket

        //per ora chiamiamo il callback in modo sincrono.
        ticketCb.onTicketReady(newTicket);
    }
}
