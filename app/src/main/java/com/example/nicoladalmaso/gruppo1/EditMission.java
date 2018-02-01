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

import database.DataManager;
import database.MissionEntity;
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
    Context context;
    TextView txtMissionName;
    TextView txtMissionStart;
    TextView txtMissionEnd;
    TextView txtMissionLocation;
    CheckBox chkIsClosed;
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
        missionID = Singleton.getInstance().getMissionID();
        thisMission = DB.getMission(missionID);

        LinearLayout bntMissionStart = (LinearLayout)findViewById(R.id.button_missionEditStart);
        LinearLayout bntMissionFinish = (LinearLayout)findViewById(R.id.button_missionEditFinish);

        bntMissionStart.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // edit by Lazzarin: use flag to tell Datepicker what date we're setting
                hideSoftKeyboard(EditMission.this);
                Log.d("stato del flag prima",Singleton.getInstance().getStartFlag()+"");
                Singleton.getInstance().setStartFlag(false);
                Log.d("flag appena prima",Singleton.getInstance().getStartFlag()+"");
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtMissionStart);
                newFragment.show(getFragmentManager(), "startDatePicker");
              /*  Log.d("stato del flag durante",Singleton.getInstance().getStartFlag()+"");
                Singleton.getInstance().setStartFlag(true);
                Log.d("stato del flag dopo",Singleton.getInstance().getStartFlag()+"");
            */}
        });

        bntMissionFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("stato del flag prima",Singleton.getInstance().getStartFlag()+"");
                hideSoftKeyboard(EditMission.this);
                DialogFragment newFragment = new DatePickerFragment().newInstance(txtMissionEnd);
                newFragment.show(getFragmentManager(), "finishDatePicker");
                Log.d("stato del flag durante",Singleton.getInstance().getStartFlag()+"");
                Log.d("stato del flag dopo",Singleton.getInstance().getStartFlag()+"");
            }
        });

        setMissionValues();
        defaultOutputPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        manager = new ExportManager(DB, defaultOutputPath.getPath());
        Spinner fileTypesSpinner= (Spinner) findViewById(R.id.spinner_formats);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, manager.exportTags());
        fileTypesSpinner.setAdapter(spinnerAdapter);

       /* ArrayList<String> fileTypes=manager.exportTags();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.fileTypes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fileTypesSpinner.setAdapter(adapter);*/
        Button btnExport =(Button) findViewById(R.id.button_export);
        btnExport.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){


                try {
                    Log.d("exportDebug","formato: "+fileTypesSpinner.getSelectedItem().toString());
                    ExportedFile exported = manager.exportMission(missionID,fileTypesSpinner.getSelectedItem().toString());
                    Uri toExport = Uri.fromFile(exported.file);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, toExport);
                    shareIntent.setType("file/"+fileTypesSpinner.getSelectedItem().toString());
                    startActivity(Intent.createChooser(shareIntent,getResources().getString(R.string.text_ExportMissionTo)));
                } catch (ExportTypeNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        });
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
                        Log.d("formato data inserita", "errato");
                        Toast.makeText(context, getResources().getString(R.string.toast_errorDate), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    else
                        Log.d("formato data inserita","corretto");

                    thisMission.setStartDate(format.parse(start));
                    thisMission.setEndDate(format.parse(finish));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                thisMission.setRepay(chkIsClosed.isChecked());
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
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        txtMissionName.setText(thisMission.getName());
        txtMissionLocation.setText(thisMission.getLocation());
        txtMissionStart.setText(formatter.format(thisMission.getStartDate()));
        txtMissionEnd.setText(formatter.format(thisMission.getEndDate()));
        chkIsClosed.setChecked(thisMission.isRepay());
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }
}