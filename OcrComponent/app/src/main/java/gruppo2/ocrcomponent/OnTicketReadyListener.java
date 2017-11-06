package gruppo2.ocrcomponent;

/**
 * Interfaccia callback utilizzata per ottenere un Ticket a partire da OcrResult.
 */
public interface OnTicketReadyListener {

    /**
     * Ottieni un Ticket. Nell'argomento "ticket", i campi corrispondenti alle informazioni non estratte sono null.
     * I campi ID e fileURI vanno ignorati.
     * @param ticket il nuovo Ticket. Mai null.
     */
    void onTicketReady(Ticket ticket);
}
