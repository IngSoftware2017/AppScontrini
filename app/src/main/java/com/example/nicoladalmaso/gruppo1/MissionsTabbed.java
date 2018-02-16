package com.example.nicoladalmaso.gruppo1;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import database.DataManager;
import database.MissionEntity;
import database.PersonEntity;
import database.TicketEntity;

public class MissionsTabbed extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    DataManager DB;
    int personID;
    PersonEntity thisPerson;
    final int MISSION_MOD = 1;
    final int PERSON_MOD = 2;
    Toolbar toolbar;
    MissionsOpen missionsOpen = new MissionsOpen();
    MissionsClosed missionsClosed = new MissionsClosed();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_missions_tabbed);

        Intent intent = getIntent();
        personID = intent.getExtras().getInt(IntentCodes.INTENT_PERSON_ID);
        DB = new DataManager(this);
        thisPerson = DB.getPerson(personID);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(thisPerson.getLastName()+" "+thisPerson.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initialize();
    }

    private void initialize(){
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.person_menu, menu);
        return true;
    }

    /** Dal Maso
     * Catch events on toolbar
     * @param item object on the toolbar
     * @return flag of success
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        // Handle item selection
        switch (item.getItemId()) {

            case (R.id.action_deletePerson):
                deletePerson();
                break;

            case (R.id.action_editPerson):
                //Open Edit Person Activity
                Intent editPerson = new Intent(this, com.example.nicoladalmaso.gruppo1.EditPerson.class);
                editPerson.putExtra(IntentCodes.INTENT_PERSON_ID, personID);
                startActivityForResult(editPerson, PERSON_MOD);
                break;

            default:
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    /** Dal Maso
     * Catch intent results
     * @param requestCode action number
     * @param resultCode intent result code
     * @param data intent data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result", ""+requestCode);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case (PERSON_MOD):
                    thisPerson = DB.getPerson(personID);
                    toolbar.setTitle(thisPerson.getName());
                    break;

                default:
                    missionsOpen.clearAllMissions();
                    missionsOpen.printAllMissions();
                    missionsClosed.clearAllMissions();
                    missionsClosed.printAllMissions();
                    break;
            }
        }
    }

    /** Dal Maso
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Bundle bundle = new Bundle();
            bundle.putInt(IntentCodes.INTENT_PERSON_ID, personID);
            switch(position){
                case 0:
                    missionsOpen.setArguments(bundle);
                    missionsOpen.setParentActivity(MissionsTabbed.this);
                    return missionsOpen;
                case 1:
                    missionsClosed.setArguments(bundle);
                    missionsClosed.setParentActivity(MissionsTabbed.this);
                    return missionsClosed;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    /**
     * Mantovan Federico
     * Delete the person and the missions\tickets associated with it
     */
    public void deletePerson(){
        AlertDialog.Builder toast = new AlertDialog.Builder(this);
        //Dialog
        toast.setMessage(getString(R.string.delete_person))
                .setTitle(getString(R.string.delete_title_person));
        //Positive button
        toast.setPositiveButton(getString(R.string.buttonDelete), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                List<MissionEntity> listMission = DB.getMissionsForPerson(personID);
                for(int i = 0; i < listMission.size(); i++){
                    List<TicketEntity> listTicket = DB.getTicketsForMission((int) listMission.get(i).getID());
                    for(int j = 0; j < listTicket.size(); j++){
                        DB.deleteTicket((int) listTicket.get(j).getID());
                    }
                }
                DB.deleteMission(personID);
                DB.deletePerson(personID);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        //Negative button
        toast.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //Nothing to do
            }
        });
        //Show toast
        AlertDialog alert = toast.show();
        Button nbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        nbutton.setTextColor(Color.parseColor("#2196F3"));
    }

    public void reload() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
