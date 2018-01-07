package com.ing.software.ticketapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import database.DataManager;
import database.TicketEntity;

import static com.ing.software.ticketapp.BillActivity.TICKET_MOD;


//Classe utilizzata per dupplicare la view cardview all'interno della ListView
//Dal Maso
public class CustomAdapter extends ArrayAdapter<TicketEntity> {

    Context context;
    String path = "";
    private int ticketID = 0;
    private int missionID;
    DataManager DB;
    private List<TicketEntity> t = new ArrayList<TicketEntity>();

    //Dal Maso, adapter declare
    CustomAdapter(Context context, int textViewResourceId,
                  List<TicketEntity> objects, int missionID, DataManager DB) {
        super(context, textViewResourceId, objects);
        this.context = context;
        this.missionID = missionID;
        this.DB = new DataManager(context);
        this.t = objects;
        Log.d("MISSION", ""+missionID);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {


        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.cardview, parent, false);
        }
        ImageView img = (ImageView)convertView.findViewById(R.id.image);
        TextView ticketTitle = (TextView)convertView.findViewById(R.id.title);
        TextView tot = (TextView)convertView.findViewById(R.id.description);

        TicketEntity c = getItem(position);
        File photo = new File(c.getFileUri().toString().substring(7));
        ticketTitle.setText(c.getTitle());

        //Amount text fixes
        String amount = "";
        if(c.getAmount() == null){
            amount = context.getString(R.string.no_amount);
            tot.setText(amount);
        }
        else {
            amount = c.getAmount().setScale(2, RoundingMode.HALF_UP).toString();
            tot.setText(context.getString(R.string.total_with_numb, amount));
        }

        //Ticket image bitmap set
        //BitmapFactory.Options options = new BitmapFactory.Options();
        //options.inPreferredConfig = ARGB_8888;
        //Bitmap bitmap = BitmapFactory.decodeFile(photo.getAbsolutePath(), options);
        //img.setImageBitmap(bitmap);

        convertView.setTag(c.getID());

        //Dal Maso


        Glide
                .with(context)
                .load(photo.getAbsolutePath())
                .thumbnail(0.1f)
                .into(img);

        if (!isTempPhoto(c)) {
            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    ticketID = Integer.parseInt(v.getTag().toString());
                    for (int i = 0; i < t.size(); i++) {
                        if (t.get(i).getID() == ticketID) {
                            TicketEntity thisTicket = t.get(i);
                            Intent startImageView = new Intent(context, BillViewer.class);
                            File photo = new File(thisTicket.getFileUri().toString().substring(7));

                            //Put data to next activity
                            startImageView.putExtra("ID", thisTicket.getID());

                            //Start new activity
                            ((BillActivity) context).startActivityForResult(startImageView, TICKET_MOD);
                            return;
                        }
                    }
                }//onClick
            });
        }

        return convertView;
    }

    private boolean isTempPhoto(TicketEntity e) {
        return e.getID() == 0; //ticketEntity was initialized, but never added to the db
    }
}