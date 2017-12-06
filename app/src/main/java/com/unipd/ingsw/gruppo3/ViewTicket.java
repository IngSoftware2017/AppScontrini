package com.unipd.ingsw.gruppo3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import database.TicketEntity;

/**
 * Created by Marco Olivieri
 */
public class ViewTicket extends AppCompatActivity {

    TicketEntity ticket;
    ImageView image;
    TextView name;
    TextView category;
    TextView amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_ticket);

        setTitle("Ticket");

        image = findViewById(R.id.ticketView);
        name = findViewById(R.id.nameText);
        category = findViewById(R.id.categoryText);
        amount = findViewById(R.id.amountText);

        ticket = getObject();
        setFieldInizialization();
    }

    /**
     * @author Marco Olivieri on 05/12/2017 (Team 3)
     * Sets all field in the layout from the ticket object
     */
    private void setFieldInizialization(){
        image.setImageURI(ticket.getFileUri());
        name.setText(ticket.getTitle());
        category.setText(ticket.getCategory());
        amount.setText((CharSequence) ticket.getAmount());
    }

    /**
     * Return the object received
     */
    private TicketEntity getObject(){
        Intent intent = getIntent();
        return (TicketEntity) intent.getSerializableExtra("codice");
    }
}
