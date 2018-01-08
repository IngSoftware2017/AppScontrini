package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.cardview, null);

        ImageView img = (ImageView)convertView.findViewById(R.id.image);
        TextView ticketTitle = (TextView)convertView.findViewById(R.id.title);
        TextView tot = (TextView)convertView.findViewById(R.id.description);

        TicketEntity c = getItem(position);
        File photo = new File(c.getFileUri().toString().substring(7));
        ticketTitle.setText(c.getTitle());

        //Amount text fixes
        String amount = "";
        if(c.getAmount() == null || c.getAmount().compareTo(new BigDecimal(0.00, MathContext.DECIMAL64)) <= 0){
            amount = context.getString(R.string.string_NoAmount);
            tot.setText(amount);
        }
        else {
            amount = new DecimalFormat("#.##").format(c.getAmount()).toString();
            tot.setText("Totale: "+amount+"€");
        }


        //Ticket image bitmap set
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath(), options);
        bitmap = Bitmap.createScaledBitmap(bitmap, parent.getWidth()/3, parent.getHeight()/3,true);
        img.setImageBitmap(bitmap);

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

    private byte[] bitmapToByte(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }
}