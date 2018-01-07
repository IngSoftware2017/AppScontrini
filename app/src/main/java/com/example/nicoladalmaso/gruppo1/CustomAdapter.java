package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ing.software.common.Ticket;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import database.DataManager;
import database.TicketEntity;

/**
 * Created by nicoladalmaso on 28/10/17.
 */

//Classe utilizzata per dupplicare la view cardview all'interno della ListView
//Dal Maso
public class CustomAdapter extends ArrayAdapter<TicketEntity> {

    Context context;
    String path = "";
    int ticketID = 0;
    int missionID;
    DataManager DB;
    List<TicketEntity> t = new ArrayList<TicketEntity>();
    HashMap<Integer, Bitmap> bitmaps = new HashMap<>();

    //Dal Maso, adapter declare
    public CustomAdapter(Context context, int textViewResourceId,
                         List<TicketEntity> objects, int missionID, DataManager DB) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.missionID = missionID;
        this.DB = new DataManager(context);
        this.t = objects;
        Log.d("MISSION", ""+missionID);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.cardview, null);
        ImageView img = (ImageView)convertView.findViewById(R.id.image);
        TextView ticketTitle = (TextView)convertView.findViewById(R.id.title);
        TextView tot = (TextView)convertView.findViewById(R.id.description);

        TicketEntity c = getItem(position);
        File photo = new File(c.getFileUri().toString().substring(7));
        ticketTitle.setText(c.getTitle());

        //Amount text fixes
        String amount = "";
        if(c.getAmount() == null){
            amount = "Prezzo non rilevato";
            tot.setText(amount);
        }
        else {
            amount = c.getAmount().toString();
            tot.setText("Totale: "+amount+"€");
        }


        Bitmap image = bitmaps.get(new Integer((int)c.getID()));
        img.setImageBitmap(image);
        //For next ticket manages
        convertView.setTag(c.getID());

        //Dal Maso
        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                ticketID = Integer.parseInt(v.getTag().toString());
                for(int i = 0; i < t.size(); i++){
                    if(t.get(i).getID() == ticketID){
                        TicketEntity thisTicket = t.get(i);
                        Intent startImageView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillViewer.class);
                        File photo = new File(thisTicket.getFileUri().toString().substring(7));

                        //Put data to next activity
                        startImageView.putExtra("ID",thisTicket.getID());

                        //Start new activity
                        ((BillActivity)context).startActivityForResult(startImageView, 4);
                        return;
                    }
                }
            }//onClick
        });
        return convertView;
    }


    /** Taschin Federico
     * Sets the HashMap with the bitmaps to be displayed
     * @param bitmaps not null, contains the Bitmap objects that have to be displayed in the list
     */
    public void setBitmaps(HashMap<Integer,Bitmap> bitmaps){
        this.bitmaps = bitmaps;
    }


}