package com.unipd.ingsw.gruppo3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ing.software.common.Ticket;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.IOException;
import java.util.List;

import database.TicketEntity;

/**
 * Created by nicoladalmaso on 28/10/17.
 */

//Classe utilizzata per dupplicare la view cardview all'interno della ListView
//Dal Maso
public class CustomAdapterGruppo1 extends ArrayAdapter<TicketEntity> implements AdapterView.OnItemSelectedListener {

    Context context;
    String path = "";
    int pos = 0;

    public CustomAdapterGruppo1(Context context, int textViewResourceId,
                                List<TicketEntity> objects) {
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
        TicketEntity c = getItem(position);
        titolo.setText(c.getTitle());
        if(c.getAmount()!=null) {
            descrizione.setText(c.getAmount().toString());
        }
        try {
            img.setImageBitmap(bitmapFromUri(c.getFileUri()));
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AAAAAAAAAA","CANT LOAD BITMAP: "+c.getFileUri().getPath());
        }
        fabDelete.setTag(position);
        convertView.setTag(position);
        //Dal Maso
        fabDelete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pos =  Integer.parseInt(v.getTag().toString());
                Log.d("TAG", v.getTag().toString());
                //context.deletePhoto(v);
                path = VariablesGruppo1.getInstance().getCurrentMissionDir();
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
                            ((BillActivityGruppo1)context).clearAllImages();
                            ((BillActivityGruppo1)context).printAllImages();
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
                path = VariablesGruppo1.getInstance().getCurrentMissionDir();
                cropFile(pos,path);
                /*File directory =new File(path);
                File[] files =directory.listFiles();
                CropImage.activity(Uri.fromFile(files[pos])).start(MainActivity.this);*/
            }//onClick
        });
        //Dal Maso
        convertView.setOnClickListener(new View.OnClickListener(){
            public void onClick (View v){
                TicketEntity ticketEntity = (TicketEntity) v.getTag();
                Intent startImageView = new Intent(context, BillViewerGruppo1.class);
                startImageView.putExtra("imagePath", ticketEntity.getFileUri());
                startImageView.putExtra("imageName", ticketEntity.getAmount());
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
                .start(((BillActivityGruppo1)context));
        //files[toCrop].delete();
    }//cropFile

    public Bitmap bitmapFromUri(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(),uri);
        return bitmap;
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}