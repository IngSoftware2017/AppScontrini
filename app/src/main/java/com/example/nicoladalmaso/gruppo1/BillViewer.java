package com.example.nicoladalmaso.gruppo1;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import database.DataManager;
import database.TicketEntity;

public class BillViewer extends AppCompatActivity {
    public FloatingActionButton fabEdit, fabDelete, fabCrop, fabConfirmEdit;
    public DataManager DB;

    //Dal Maso
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_bill_viewer);
        Intent intent = getIntent();
        //Get data from parent view
        long id = intent.getExtras().getLong("ID");
        String imgPath = intent.getExtras().getString("imagePath");
        String imgName = intent.getExtras().getString("imageName");
        String imgLastMod = intent.getExtras().getString("imgLastMod");
        String imgPrice = intent.getExtras().getString("imgPrice");
        setTitle(imgName);
        //Title
        TextView billLastMod = (TextView)findViewById(R.id.billDate);
        billLastMod.setText(imgLastMod);
        //ImageName
        TextView billName = (TextView)findViewById(R.id.billName);
        billName.setText(imgName);
        //Total price
        TextView billPrice = (TextView)findViewById(R.id.billAmount);
        billPrice.setText(imgPrice);
        //Full image view
        ImageView imgView = (ImageView)findViewById(R.id.billImage);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath,bmOptions);
        imgView.setImageBitmap(bitmap);
        fabEdit=(FloatingActionButton)findViewById(R.id.fabEdit);
        fabCrop=(FloatingActionButton)findViewById(R.id.fabCrop);
        fabDelete=(FloatingActionButton)findViewById(R.id.fabDelete);
        fabConfirmEdit= (FloatingActionButton)findViewById(R.id.fabConfirmEdit);
        //Edit Ticket Info button
        fabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTicketInfo(id);
            }//onClick
        });
        fabCrop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cropPhoto(id);
           }//onClick
        });
        fabDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteTicket(id);
            }//onClick
        });
    }

    /**PICCOLO
     * Method that deletes a ticket from the db
     * @param id the id of the TicketEntity in the db
     */
    private void deleteTicket(long id) {
        DB = new DataManager(this.getApplicationContext());
        TicketEntity ticketToDelete=DB.getTicket((int)id);
        Log.d("FileUri",ticketToDelete.getFileUri().getPath());
        //Delete the photo from the phone
        File photoToDelete=new File(ticketToDelete.getFileUri().getPath());
        photoToDelete.delete();
        DB.deleteTicket((int) id);
        //ClearAllImages
        //BillActivity.clearAllImages();
        //BillActivity.printAllImages();
        //TODO: Rebuild the listview
    }//deleteTicket

    /**PICCOLO
     * Method that lets the user crop and/or rotate the photo
     * @param id the id of the TicketEntity in the db
     */
    private void cropPhoto(long id) {
        DB=new DataManager(getApplicationContext());
        TicketEntity ticket = DB.getTicket((int) id);
        Uri toCropUri = ticket.getFileUri();
        CropImage.activity(toCropUri)
                .setOutputUri(toCropUri).start(this);
        ticket.setFileUri(toCropUri);
        DB.deleteTicket((int)id);
        DB.addTicket(ticket);
       //TODO: implement the method using the origial file instead
    }//cropPhoto

    /**PICCOLO
     * Method that lets the user edit the infos of the ticket, useful when the ocr fails
     * @param id the id of the TicketEntity in the db
     */
    private void editTicketInfo(long id) {
        EditText name = (EditText) findViewById(R.id.billName);
        EditText shop = (EditText) findViewById(R.id.billShop);
        EditText date = (EditText) findViewById(R.id.billDate);
        EditText amount = (EditText) findViewById(R.id.billAmount);
        fabEdit.setVisibility(View.INVISIBLE);
        fabConfirmEdit.setVisibility(View.VISIBLE);
        name.setEnabled(true);
        name.setClickable(true);
        shop.setEnabled(true);
        date.setEnabled(true);
        amount.setEnabled(true);
        fabConfirmEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO: SAVE THE NEW VALUES
                fabEdit.setVisibility(View.VISIBLE);
                fabConfirmEdit.setVisibility(View.INVISIBLE);
            }//onClick
        });
    }//editTicketInfo

    //Dal Maso, manage back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

