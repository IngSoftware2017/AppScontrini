package com.unipd.ingsw.gruppo3;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * Created by Federico Taschin on 07/12/2017.
 */

public class IntentWrapperTicketEntity implements Serializable{

    int ID;
    BigDecimal amount;
    String shop;
    Date date;
    String title;
    String category;
    int missionID;
}
