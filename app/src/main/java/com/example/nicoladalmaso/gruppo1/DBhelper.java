package com.example.nicoladalmaso.gruppo1;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by nicoladalmaso on 28/10/17.
 */

public class DBhelper extends SQLiteOpenHelper
{
    public static final String DBNAME="BILLBOOK";

    public DBhelper(Context context) {
        super(context, DBNAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String q="CREATE TABLE "+DatabaseStrings.TBL_NAME+
                "("+
                    DatabaseStrings.FIELD_NAME+" TEXT PRIMARY KEY," +
                    DatabaseStrings.FIELD_DIRECTORY+" TEXT," +
                    DatabaseStrings.FIELD_DESC+" TEXT," +
                    //DatabaseStrings.FIELD_IMAGE+" BLOB," +        PER SALVARE DIRETTAMENTE L'IMMAGINE
                    DatabaseStrings.FIELD_DATE+" TEXT" +
                ")";
        db.execSQL(q);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseStrings.TBL_NAME);

        // create new table
        onCreate(db);
    }

}
