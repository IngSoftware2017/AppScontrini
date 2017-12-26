package com.ing.software.ticketapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import database.DataManager;
import database.TicketEntity;

public class EditTicket extends AppCompatActivity {

    public DataManager DB;
    int ticketId;
    Context context;
    TicketEntity thisTicket;
    String ticketTitle = "", ticketDate = "", ticketAmount = "", ticketShop = "", ticketPath = "";
    TextView txtTitle;
    TextView txtAmount;
    TextView txtShop;
    TextView txtDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setTitle(getString(R.string.edit_ticket));
        setContentView(R.layout.activity_edit_ticket);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        //Get data from parent view
        setTicketValues();

    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu
     * @return success flag
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.addmission_menu, menu);
        return true;
    }

    /** Dal Maso
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     *
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_addMission:
                //Salva i file nel DB
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                thisTicket.setTitle(txtTitle.getText().toString());
                try {
                    thisTicket.setDate(format.parse(txtDate.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                thisTicket.setShop(txtShop.getText().toString());
                try {
                    thisTicket.setAmount(new BigDecimal(txtAmount.getText().toString().replaceAll(",", ".")));
                } catch (NumberFormatException e) {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        builder = new AlertDialog.Builder(EditTicket.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                    } else {
                        builder = new AlertDialog.Builder(EditTicket.this);
                    }
                    builder.setTitle(getString(R.string.alert_invalid_amount_title))
                            .setMessage(R.string.alert_invalid_amount_mess)
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                // do nothing
                            })
                            .show();
                }
                DB.deleteTicket((int)thisTicket.getID());
                DB.addTicket(thisTicket);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                //finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setTicketValues(){
        Intent intent = getIntent();
        ticketId = (int) intent.getExtras().getLong("ticketID");
        Log.d("TicketID", "Edit ticket "+ticketId);
        thisTicket = DB.getTicket(ticketId);
        ticketPath = thisTicket.getFileUri().toString().substring(7);
        ticketTitle = thisTicket.getTitle();
        ticketDate = thisTicket.getDate().toString();
        ticketAmount = thisTicket.getAmount().setScale(2, RoundingMode.HALF_UP).toString();
        ticketShop = thisTicket.getShop();

        //set those values to the edittext
        setTicketValuesOnScreen();
    }

    private void setTicketValuesOnScreen(){
        txtTitle = (TextView)findViewById(R.id.input_ticketTitleMod);
        txtAmount = (TextView)findViewById(R.id.input_ticketAmountMod);
        txtShop = (TextView)findViewById(R.id.input_ticketShopMod);
        txtDate = (TextView)findViewById(R.id.input_ticketDateMod);

        txtTitle.setText(ticketTitle);
        txtDate.setText(ticketDate);
        txtShop.setText(ticketShop);
        txtAmount.setText(ticketAmount);
    }
}
