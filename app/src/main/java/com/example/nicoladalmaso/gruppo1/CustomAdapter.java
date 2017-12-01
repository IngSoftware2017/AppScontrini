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
import java.util.List;

/**
 * Created by nicoladalmaso on 28/10/17.
 */

//Classe utilizzata per dupplicare la view cardview all'interno della ListView
//Dal Maso
public class CustomAdapter extends ArrayAdapter<Scontrino> {

    Context context;
    String path = "";
    int pos = 0;

    public CustomAdapter(Context context, int textViewResourceId,
                         List<Scontrino> objects) {
        super(context, textViewResourceId, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.cardview, null);
        ImageView img = (ImageView)convertView.findViewById(R.id.image);
        TextView titolo = (TextView)convertView.findViewById(R.id.title);
        TextView descrizione = (TextView)convertView.findViewById(R.id.description);
        FloatingActionButton fabDelete = (FloatingActionButton)convertView.findViewById(R.id.btnDelete);
        FloatingActionButton fabCrop = (FloatingActionButton)convertView.findViewById(R.id.btnCrop);
        Scontrino c = getItem(position);
        titolo.setText(c.getTitolo());
        descrizione.setText(c.getDescrizione());
        img.setImageBitmap(c.getImg());
        fabDelete.setTag(position);
        convertView.setTag(position);
        //Dal Maso
        fabDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pos =  Integer.parseInt(v.getTag().toString());
                Log.d("TAG", v.getTag().toString());
                //context.deletePhoto(v);
                path = Variables.getInstance().getCurrentMissionDir();
                Log.d("Dir", path);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Sei sicuro di voler eliminare lo scontrino?")
                        .setTitle("Cancellazione");
                // Add the buttons
                builder.setPositiveButton("Cancella", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        File directory = new File(path);
                        File[] files = directory.listFiles();
                        if(files[pos].delete()){
                            ((BillActivity)context).clearAllImages();
                            ((BillActivity)context).printAllImages();
                        }
                    }
                });
                builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
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
                path = Variables.getInstance().getCurrentMissionDir();
                cropFile(pos,path);
                /*File directory =new File(path);
                File[] files =directory.listFiles();
                CropImage.activity(Uri.fromFile(files[pos])).start(MainActivity.this);*/
            }//onClick
        });
        //Dal Maso
        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                pos = Integer.parseInt(v.getTag().toString());
                path = Variables.getInstance().getCurrentMissionDir();
                File directory = new File(path);
                File[] files = directory.listFiles();
                Intent startImageView = new Intent(context, com.example.nicoladalmaso.gruppo1.BillViewer.class);
                startImageView.putExtra("imagePath", files[pos].getPath());
                context.startActivity(startImageView);
            }//onClick
        });
        return convertView;
    }
    /**PICCOLO_Edit by Dal Maso
     * Metodo che cancella permette all'utente di ridimensionare la foto
     * @param toCrop l'indice della foto di cui fire il resize
     * @param path percorso della foto
     */
    public void cropFile(int toCrop, String path){
        Log.d("Crop", "Start crop activity");
        boolean result = false;
        File directory = new File(path);
        File[] files = directory.listFiles();
        CropImage.activity(Uri.fromFile(files[toCrop]))
                .setOutputUri(Uri.fromFile(files[toCrop]))
                .start(((BillActivity)context));
        //files[toCrop].delete();
    }//cropFile

}