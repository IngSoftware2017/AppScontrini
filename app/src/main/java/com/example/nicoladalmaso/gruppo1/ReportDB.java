package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import java.io.File;
import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;
import export.ExportManager;
import export.ExportTypeNotSupportedException;
import export.ExportedFile;

/**
 * @author Marco Olivieri on 06/01/2018 modified by Federico Taschin
 */

public class ReportDB extends AppCompatActivity {

    File defaultOutputPath;

    public DataManager DB;
    Context context;

    TextView txtActiveMission;
    TextView txtCloseMission;
    TextView txtRefoundableTicket, txtNotRefoundableTicket;
    Spinner exportSpiner;
    Button exportButton;
    ExportManager manager;

    //@author Marco Olivieri and Federico Taschin
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setElevation(0);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_report_db);
        setTitle("Report DB");

        DB = new DataManager(this.getApplicationContext());
        context = this.getApplicationContext();

        txtActiveMission=(TextView)findViewById(R.id.msgNumActiveMission);
        txtCloseMission=(TextView)findViewById(R.id.msgNumCloseMission);
        txtRefoundableTicket = (TextView)findViewById(R.id.msgNumTicketRefoundable);
        txtNotRefoundableTicket = (TextView)findViewById(R.id.msgNumTicketNotRefoundable);

        exportSpiner = findViewById(R.id.export_spinner);
        exportButton = findViewById(R.id.export_button);

        //FEDERICO TASCHIN
        //Instantiating the export manager
        defaultOutputPath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        manager = new ExportManager(DB, defaultOutputPath.getPath());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, manager.exportTags());
        exportSpiner.setAdapter(spinnerAdapter);





        Log.d("EXPORTDEBUG","EXPORTING");
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedExportTag = (String) exportSpiner.getSelectedItem();
                Log.d("EXPORTDEBUG","EXPORTING");

                DialogProperties properties = new DialogProperties();
                properties.selection_mode = DialogConfigs.SINGLE_MODE;
                properties.selection_type = DialogConfigs.DIR_SELECT;
                properties.root = new File(DialogConfigs.DEFAULT_DIR);
                properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                properties.extensions = null;

                FilePickerDialog dialog = new FilePickerDialog(ReportDB.this,properties);
                dialog.setTitle(R.string.export_activity_name);

                dialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        Log.d("EXPORTDEBUG","SELECTED PATH: "+files[0]);
                        manager.setOutputPath(files[0]);
                        try {
                            List<ExportedFile> exportedFiles = manager.exportDatabase(selectedExportTag);
                            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which){
                                        case DialogInterface.BUTTON_POSITIVE:
                                            EmailBuilder.createEmail().attachFiles(exportedFiles).sendEmail(context);
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            //No button clicked
                                            break;
                                    }
                                }
                            };

                            AlertDialog.Builder builder = new AlertDialog.Builder(ReportDB.this);
                            builder.setMessage(R.string.export_share_text).setPositiveButton("Si", dialogClickListener)
                                    .setNegativeButton("No", dialogClickListener).show();
                        } catch (ExportTypeNotSupportedException e) {
                            e.printStackTrace();
                        }
                    }
                });

                dialog.show();


            }
        });

        initializeValues();
    }



    /**
     * @author Elardo Stefano
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //toolbar is empty, handle only the return arrow which close the activity
        finish();
        return true;
    }

    /**
     * @author Marco Olivieri
     * Inizializes all fields with db numbers
     *
     * Modify: Improve count of refoundable (and not) Tickets
     * @author Matteo Mascotto
     */

    private void initializeValues(){
        /*
        List<PersonEntity> person = DB.getAllPerson();
        txtNumPerson.setText(txtNumPerson.getText()+" "+ String.valueOf(person.size()));
        */

        List<MissionEntity> activeMission = DB.getMissionClosed(false);
        txtActiveMission.setText(txtActiveMission.getText()+" "+ String.valueOf(activeMission.size()));

        //List<MissionEntity> allMission = DB.getAllMission();
        //MissionEntity m = allMission.get(0);
        //boolean repaid = m.isRepay();
        //int closeM = allMission.size()-activeMission.size();
        List<MissionEntity> closeMission = DB.getMissionClosed(true);
        txtCloseMission.setText(txtCloseMission.getText()+" "+ String.valueOf(closeMission.size()));

        int refoundableTickets = DB.getNumberOfRefundableTickets();
        txtRefoundableTicket.setText(txtRefoundableTicket.getText()+" "+ String.valueOf(refoundableTickets));

        int notRefoundableTickets = DB.getNumberOfNotRefundableTickets();
        txtNotRefoundableTicket.setText(txtNotRefoundableTicket.getText()+" "+ String.valueOf(notRefoundableTickets));
    }

}
