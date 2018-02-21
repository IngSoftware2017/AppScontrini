package com.example.nicoladalmaso.gruppo1;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import database.DataManager;
import database.MissionEntity;
import database.TicketEntity;

import static com.example.nicoladalmaso.gruppo1.EditMission.hideSoftKeyboard;

/**
 * Class for Edit the Ticket's data
 *
 * Modified: Improve refoundable Ticket
 * @author Matteo Mascotto
 */
public class EditTicket extends AppCompatActivity {

    public DataManager DB;
    long ticketID;
    Context context;
    TicketEntity thisTicket;
    String ticketTitle = "", ticketDate = "", ticketAmount = "", ticketShop = "", ticketPath = "";
    TextView txtTitle;
    TextView txtAmount;
    TextView txtShop;
    TextView txtDate;
    CheckBox chkIsRefoundable;
    MissionEntity thisMission;
    TextView txtPeople;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        context = this.getApplicationContext();
        setTitle(context.getString(R.string.title_EditTicket));
        setContentView(R.layout.activity_edit_ticket);

        DB = new DataManager(this.getApplicationContext());
        ticketID = getIntent().getExtras().getLong(IntentCodes.INTENT_TICKET_ID);
        thisTicket = DB.getTicket(ticketID);
        thisMission = DB.getMission(thisTicket.getMissionID());
        TextView editDate = findViewById(R.id.input_ticketDateMod);
        LinearLayout bntMissionStart = findViewById(R.id.buttonEditTicketDate);
        bntMissionStart.setOnClickListener(v -> {
            Singleton.getInstance().setStartFlag(2);
            DialogFragment newFragment = new DatePickerFragment().newInstance(editDate, null,null);
            newFragment.show(getFragmentManager(), "startDatePicker");
        });

        //Get data from parent view
        setTicketValues();
    }

    /** Dal Maso
     * Setting toolbar buttons and style from /res/menu
     * @param menu menu to create
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
     *
     * Modified: Improve refoundable Ticket
     * @author Matteo Mascotto on 15-02-2018
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_confirm:
                //Salva i file nel DB
                thisTicket.setTitle(txtTitle.getText().toString());
                SimpleDateFormat DateFormat = new SimpleDateFormat("dd/MM/yyyy");

                try {

                    String  start = DateFormat.format(thisMission.getStartDate());

                    String finish = DateFormat.format(thisMission.getEndDate());

                    if( AppUtilities.checkIntervalDate(start,finish,txtDate.getText().toString()))
                        thisTicket.setDate(DateFormat.parse(txtDate.getText().toString()));
                    else {
                        Toast.makeText(context, getResources().getString(R.string.toast_errorTicketDate), Toast.LENGTH_SHORT).show();
                        break;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                thisTicket.setShop(txtShop.getText().toString());
                if(!txtPeople.getText().toString().equals("")){
                    thisTicket.setTagPlaces(Short.parseShort(txtPeople.getText().toString()));
                }

                try {
                    String newAmount = txtAmount.getText().toString();
                    newAmount = newAmount.replace(",", ".");
                    Double a = Double.parseDouble(newAmount);
                    thisTicket.setAmount(BigDecimal.valueOf(a));
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }

                thisTicket.setRefundable(chkIsRefoundable.isChecked());

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

    /** Dal Maso
     * set the current values on the variables
     */
    private void setTicketValues() {

        ticketPath = thisTicket.getFileUri().toString().substring(7);
        ticketTitle = thisTicket.getTitle();
        ticketDate = thisTicket.getDate()==null?"":thisTicket.getDate().toString();

        if(thisTicket.getAmount() !=null)
            ticketAmount = thisTicket.getAmount().setScale(2, RoundingMode.HALF_EVEN).toString();
        else
            ticketAmount = "";

        if (thisTicket.getShop() != null)
            ticketShop = thisTicket.getShop();
        else
            ticketShop = "";

        //set those values to the edittext
        setTicketValuesOnScreen();
    }

    /** Dal Maso
     * It set the value on Activity
     *
     * Modified: Improved Refoundable Ticket
     * @author Matteo Mascotto on 15-02-2018
     * Modified: Remove bug interval date for DatePicker
     */
    private void setTicketValuesOnScreen(){
        txtTitle = findViewById(R.id.input_ticketTitleMod);
        txtAmount = findViewById(R.id.input_ticketAmountMod);
        txtShop = findViewById(R.id.input_ticketShopMod);
        txtDate = findViewById(R.id.input_ticketDateMod);
        chkIsRefoundable = findViewById(R.id.check_isRefoundable);
        txtPeople = findViewById(R.id.input_ticketPersonNumber);
        LinearLayout btnModifyDate = findViewById(R.id.buttonEditTicketDate);

        btnModifyDate.setOnClickListener(v -> {
            DialogFragment newFragment = new DatePickerFragment().newInstance(txtDate, null,null);
            newFragment.show(getFragmentManager(), "startDatePicker");
        });

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");

        txtTitle.setText(ticketTitle);
        txtDate.setText(formatter.format(thisTicket.getDate()));
        txtShop.setText(ticketShop);
        txtAmount.setText(ticketAmount);
        chkIsRefoundable.setChecked(thisTicket.isRefundable());
    }
}
