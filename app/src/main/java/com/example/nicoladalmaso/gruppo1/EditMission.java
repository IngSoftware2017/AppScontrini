package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;
import export.ExportManager;
import export.ExportTypeNotSupportedException;
import export.ExportedFile;

/**
 * Created by Francesco on 03/01/2018.
 */

public class EditMission extends AppCompatActivity {
    public DataManager DB;
    long missionID;
    MissionEntity thisMission;
    PersonEntity person;
    Context context;
    TextView txtMissionName;
    TextView txtMissionStart;
    TextView txtMissionEnd;
    TextView txtMissionLocation;
    CheckBox chkIsClosed;
    TextView txtCount;
    TextView txtMissionTotal;
    Button btnExport;
    Spinner spnrExport;
    File defaultOutputPath;
    ExportManager manager;

    //TODO: poter cambiare persona?
    @Override
    /**Piccolo (using Dal Maso's code)
     * Method that is run every time the activity is started
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setElevation(0);
        setContentView(R.layout.activity_edit_mission);
        context = this.getApplicationContext();
        setTitle(context.getString(R.string.title_EditMission));

        DB = new DataManager(this.getApplicationContext());
        txtMissionName=(TextView)findViewById(R.id.input_missionEditName);
        txtMissionLocation=(TextView)findViewById(R.id.input_missionEditLocation);
        txtMissionStart=(TextView)findViewById(R.id.input_missionEditStart);
        txtMissionEnd=(TextView)findViewById(R.id.input_missionEditFinish);
        chkIsClosed=(CheckBox)findViewById(R.id.check_isRepaid);
        txtCount=(TextView)findViewById(R.id.ticket_count);
        txtMissionTotal=(TextView)findViewById(R.id.mission_total);
        btnExport=(Button)findViewById(R.id.export_button);
        spnrExport=(Spinner)findViewById(R.id.export_spinner);

        missionID = Singleton.getInstance().getMissionID();
        thisMission = DB.getMission(missionID);

        LinearLayout bntMissionStart = (LinearLayout)findViewById(R.id.button_missionEditStart);
        LinearLayout bntMissionFinish = (LinearLayout)findViewById(R.id.button_missionEditFinish);
        //lazzarin clean startDate on Singleton
        Singleton.getInstance().setStartDate(thisMission.getStartDate());
        bntMissionStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // edit by Lazzarin: use flag to tell Datepicker what date we're setting
                hideSoftKeyboard(EditMission.this);
                Singleton.getInstance().setStartFlag(0);
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtMissionStart);
                newFragment.show(getFragmentManager(), "startDatePicker");
            }
        });

        bntMissionFinish.setOnClickListener(new View.OnClickListener() {
            // edit by Lazzarin
            public void onClick(View v) {
                Singleton.getInstance().setStartFlag(1);
                hideSoftKeyboard(EditMission.this);
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtMissionEnd);
                newFragment.show(getFragmentManager(), "finishDatePicker");
            }
        });

        defaultOutputPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        manager = new ExportManager(DB, defaultOutputPath.getPath());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, manager.exportTags());
        spnrExport.setAdapter(spinnerAdapter);

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ExportedFile exported = manager.exportMission(missionID,(String) spnrExport.getSelectedItem());
                    EmailBuilder.createEmail().to(person.getEmail()).attachFile(exported.file).sendEmail(EditMission.this);
                } catch (ExportTypeNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        });

        setMissionValues();
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

    /** Dal Maso, adapted by Piccolo
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

                thisMission.setName(txtMissionName.getText().toString());
                thisMission.setLocation(txtMissionLocation.getText().toString());

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    String start = txtMissionStart.getText().toString();
                    String finish =  txtMissionEnd.getText().toString();

                    if(!AppUtilities.checkDate(start, finish)) {
                        Toast.makeText(context, getResources().getString(R.string.toast_errorDate), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    thisMission.setStartDate(format.parse(start));
                    thisMission.setEndDate(format.parse(finish));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                thisMission.setClosed(chkIsClosed.isChecked());
                DB.updateMission(thisMission);
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

    /** Dal Maso, adapted by Piccolo
     * set the values of the mission
     */
    private void setMissionValues(){
        //Intent intent = getIntent();
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        /*
        missionID = (int) intent.getExtras().getLong(IntentCodes.INTENT_MISSION_ID);
        thisMission = DB.getMission(missionID);
        Log.d("EDITMISSION","MISSION_ID= "+missionID);
        for(MissionEntity missionEntity : DB.getAllMission()){
            Log.d("EDITMISSION",""+missionEntity.getID());
        }
        person = DB.getPerson(thisMission.getPersonID());
        */
        txtMissionName.setText(thisMission.getName());
        txtMissionLocation.setText(thisMission.getLocation());
        txtMissionStart.setText(formatter.format(thisMission.getStartDate()));
        txtMissionEnd.setText(formatter.format(thisMission.getEndDate()));
        chkIsClosed.setChecked(thisMission.isClosed());

        //Elardo mission summary
        List<TicketEntity> ticketList=DB.getTicketsForMission(missionID);
        txtCount.setText(Integer.toString(ticketList.size()));
        Double total= 0.0;
        boolean unreadableTicket=false;
        for(int i=0; i<ticketList.size(); i++){
            if (ticketList.get(i).getAmount()== null){
                unreadableTicket=true;
            }
            else{
                Double amount = ticketList.get(i).getAmount().doubleValue();
                total+=amount;
            }

        }
        if (ticketList.size()==0)
            txtMissionTotal.setText(getResources().getString(R.string.noBills));
        else if (unreadableTicket)
            txtMissionTotal.setText(getResources().getString(R.string.euro_string)+ " " + total.toString()+" "+getResources().getString(R.string.unreadable_ticket));
        else
            txtMissionTotal.setText(getResources().getString(R.string.euro_string)+ " " + total.toString());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}