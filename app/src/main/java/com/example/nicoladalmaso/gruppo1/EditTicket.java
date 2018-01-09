package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import database.DataManager;
import database.MissionEntity;
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
        setTitle("Modifica ticket");
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
        inflater.inflate(R.menu.confirm_menu, menu);
        return true;
    }

    /** Dal Maso
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     *
     * Modify by Marco Olivieri: fixed amount error
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_confirm:
                //Salva i file nel DB
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                thisTicket.setTitle(txtTitle.getText().toString());
                try {
                    thisTicket.setDate(format.parse(txtDate.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                thisTicket.setShop(txtShop.getText().toString());
                //thisTicket.setAmount(BigDecimal.valueOf(Integer.parseInt(txtAmount.getText().toString())));

                try {
                    String newAmount = txtAmount.getText().toString();
                    newAmount = newAmount.replace(",", ".");
                    Double a = Double.parseDouble(newAmount);
                    thisTicket.setAmount(BigDecimal.valueOf(a));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }

                DB.updateTicket(thisTicket);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                finish();
                break;
        }
        return true;
    }

    private void setTicketValues(){
        Intent intent = getIntent();
        ticketId = (int) intent.getExtras().getLong("ticketID");
        Log.d("TicketID", "Edit ticket "+ticketId);
        thisTicket = DB.getTicket(ticketId);
        ticketPath = thisTicket.getFileUri().toString().substring(7);
        ticketTitle = thisTicket.getTitle();
        ticketDate = thisTicket.getDate()==null?"":thisTicket.getDate().toString();
        if(thisTicket.getAmount() !=null) {
            Double amount = thisTicket.getAmount().doubleValue();
            ticketAmount = amount.toString();
        }
        else
            ticketAmount="";
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
