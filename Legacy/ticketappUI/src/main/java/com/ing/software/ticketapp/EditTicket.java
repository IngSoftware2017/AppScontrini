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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        setTitle(getString(R.string.edit_ticket));
        setContentView(R.layout.activity_edit_ticket);
        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(EditTicket.this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(EditTicket.this);
        }


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
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_confirm:
                //Salva i file nel DB
                thisTicket.setTitle(txtTitle.getText().toString());
                Date date = parseDate(txtDate.getText().toString());
                if (date == null) {
                    if (!txtDate.getText().toString().replaceAll(" ", "").equals("")) {
                        builder.setTitle(getString(R.string.alert_invalid_date_title))
                                .setMessage(R.string.alert_invalid_date_mess)
                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                    // do nothing
                                })
                                .show();
                        break;
                    }
                } else {
                    thisTicket.setDate(date);
                }
                thisTicket.setShop(txtShop.getText().toString());
                try {
                    thisTicket.setAmount(new BigDecimal(txtAmount.getText().toString().replaceAll(",", ".").replaceAll(" ", "")));
                } catch (NumberFormatException e) {
                    if (!txtAmount.getText().toString().replaceAll(" ", "").equals("")) {
                        builder.setTitle(getString(R.string.alert_invalid_amount_title))
                                .setMessage(R.string.alert_invalid_amount_mess)
                                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                                    // do nothing
                                })
                                .show();
                        break;
                    }
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
        if (thisTicket.getDate() != null) {
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            ticketDate = df.format(thisTicket.getDate());
        }
        if (thisTicket.getAmount() != null) {
            ticketAmount = thisTicket.getAmount().setScale(2, RoundingMode.HALF_UP).toString();
        }
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



    /**
     * @author Salvagno
     *
     * @param dateString An input date string.
     * @return A Date (java.util.Date) reference. The reference will be null if
     *         we could not match any of the known formats.
     */
    public static Date parseDate(String dateString)
    {
        Date date = null;
        Locale locale = new Locale("US");

        //prendo i tre pezzi della stringa data
        char[] string = dateString.toCharArray();
        String token1 = "";
        String token2 = "";
        String token3 = "";
        Integer numberOfSymbols = 0;
        String finalDate = "";
        String[] formats = null;



        for (char c : string) {
            if (c == '-' || c == '.' || c == '/')
            {
                numberOfSymbols ++;
                finalDate=finalDate+'-';
            }
            else
            {
                finalDate=finalDate+c;

                if(numberOfSymbols == 0)
                    token1 = token1+c;
                else if(numberOfSymbols == 1)
                    token2 = token2+c;
                else if(numberOfSymbols == 2)
                    token3 = token3+c;
                else
                    return null;
            }

        }

        //convert string to integer and get last two digit
        int token1Number = -1;
        int token2Number = -1;
        int token3Number = -1;
        try {
            token1Number = ((Integer.parseInt(token1))%100);    //probably is day
            token2Number = ((Integer.parseInt(token2))%100);    //probably is month
            if(token3.length()==4) //if this token have 4 character
            {
                token3Number = Integer.parseInt(token3);
                formats = new String[] {"dd-MM-yyyy","MM-dd-yyyy"};
            }
            else {
                token3Number = ((Integer.parseInt(token3)) % 100);
                formats = new String[] {"dd-MM-yy", "MM-dd-yy"};
            }
        } catch (NumberFormatException e) {
            return null;
        }

        if((token1Number >= 1 && token1Number <= 12) && (token2Number >= 1 && token2Number <= 12))
            dateString = String.valueOf(token1Number)+'-'+String.valueOf(token2Number)+'-'+String.valueOf(token3Number);
        else if(token2Number > 12)
            dateString = String.valueOf(token2Number)+'-'+String.valueOf(token1Number)+'-'+String.valueOf(token3Number);
        else
            dateString = String.valueOf(token1Number)+'-'+String.valueOf(token2Number)+'-'+String.valueOf(token3Number);

        for (int i = 0; i < formats.length; i++)
        {
            String format = formats[i];
            SimpleDateFormat dateFormat = new SimpleDateFormat(format,locale);
            try
            {
                // parse() will throw an exception if the given dateString doesn't match
                // the current format
                date = dateFormat.parse(dateString);
                break;
            }
            catch(ParseException e)
            {
                // don't do anything. just let the loop continue.
            }
        }

        return date;
    }
}
