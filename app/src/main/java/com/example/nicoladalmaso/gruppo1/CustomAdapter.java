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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import database.DataManager;
import database.Ticket;

/**
 * Created by nicoladalmaso on 28/10/17.
 */

//Classe utilizzata per dupplicare la view cardview all'interno della ListView
//Dal Maso
public class CustomAdapter extends ArrayAdapter<Ticket> {

    Context context;
    String path = "";
    int pos = 0;
    int missionID;
    DataManager DB;
    List<Ticket> t = new ArrayList<Ticket>();

    public CustomAdapter(Context context, int textViewResourceId,
                         List<Ticket> objects, int missionID, DataManager DB) {
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
        TextView title = (TextView)convertView.findViewById(R.id.title);
        TextView tot = (TextView)convertView.findViewById(R.id.description);
        FloatingActionButton fabDelete = (FloatingActionButton)convertView.findViewById(R.id.btnDelete);
        FloatingActionButton fabCrop = (FloatingActionButton)convertView.findViewById(R.id.btnCrop);
        Ticket c = getItem(position);
        File photo = new File(c.getFileUri().toString().substring(7));
        title.setText(photo.getName());
        String amount = "";
        if(c.getAmount() == null){
            amount = "Prezzo non rilevato";
        }
        else {
            amount = c.getAmount().toString();
        }
        tot.setText("Totale: "+amount+"€");

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath(), options);
        img.setImageBitmap(bitmap);
        Log.d("LocalID", ""+c.toString());
        fabDelete.setTag(c.getID());
        convertView.setTag(c.getID());

        //Dal Maso
        fabDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pos =  Integer.parseInt(v.getTag().toString());
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(text.deleteTicketToast)
                        .setTitle("Cancellazione");
                String toDelete = "";
                for(int i = 0; i < t.size(); i++){
                    if(t.get(i).getID() == pos){
                        toDelete = t.get(i).getFileUri().toString().substring(7);
                    }
                }
                final File ticketDelete = new File(toDelete);
                // Add the buttons
                builder.setPositiveButton(text.buttonDelete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d("TicketID", ""+pos);
                        if(DB.deleteTicket(pos) && ticketDelete.delete()){
                            ticketDelete.delete();
                            Log.d("ELIMINATO", "OK");
                            ((BillActivity)context).clearAllImages();
                            ((BillActivity)context).printAllImages();
                        }
                    }
                });
                builder.setNegativeButton(text.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Nothing
                    }
                });
                AlertDialog alert = builder.show();
                Button nbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                nbutton.setTextColor(Color.parseColor("#2196F3"));
            }
        });

        //PICCOLO
        fabCrop.setTag(position);
        fabCrop.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                pos = Integer.parseInt(v.getTag().toString());
                cropFile(pos);
            }
        });

        //Dal Maso
        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                pos = Integer.parseInt(v.getTag().toString());
                for(int i = 0; i < t.size(); i++){
                    if(t.get(i).getID() == pos){
                        Ticket thisPhoto = t.get(i);
                        Intent startImageView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillViewer.class);
                        File photo = new File(thisPhoto.getFileUri().toString().substring(7));
                        startImageView.putExtra("imagePath", thisPhoto.getFileUri().toString().substring(7));
                        startImageView.putExtra("imageName", photo.getName());
                        SimpleDateFormat simpleDateFormat =
                                new SimpleDateFormat("HH:mm'   'dd/MM/yyyy");
                        String date = simpleDateFormat.format(photo.lastModified());
                        startImageView.putExtra("imgLastMod", date);
                        startImageView.putExtra("imgPrice", thisPhoto.getAmount().toString()+"€");
                        context.startActivity(startImageView);
                        return;
                    }
                }
            }//onClick
        });
        return convertView;
    }

    /**PICCOLO_Edit by Dal Maso
     * Method that lets the user crop the photo
     * @param toCrop the index of the photo to resize
     */
    public void cropFile(int toCrop){
        Log.d("Crop", "Start crop activity");
        boolean result = false;
        for(int i = 0; i < t.size(); i++){
            if(t.get(i).getID() == pos){
                Uri toCropUri = t.get(i).getFileUri();
                CropImage.activity(toCropUri)
                        .setOutputUri(toCropUri)
                        .start(((BillActivity)context));
                return;
            }
        }
    }

}