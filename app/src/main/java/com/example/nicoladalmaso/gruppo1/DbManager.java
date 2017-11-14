package com.example.nicoladalmaso.gruppo1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nicoladalmaso on 28/10/17.
 */

public class DbManager
{
    private DBhelper dbhelper;

    public DbManager(Context ctx)
    {
        dbhelper=new DBhelper(ctx);
    }

    public void addRecord(String id, String dir, String desc, String date)
    {
        SQLiteDatabase db=dbhelper.getWritableDatabase();

        ContentValues cv=new ContentValues();
        cv.put(DatabaseStrings.FIELD_NAME, id);
        cv.put(DatabaseStrings.FIELD_DIRECTORY, dir);
        cv.put(DatabaseStrings.FIELD_DESC, desc);
        //cv.put(DatabaseStrings.FIELD_IMAGE, img);    PER SALVARE DIRETTAMENTE L'IMMAGINE
        cv.put(DatabaseStrings.FIELD_DATE, date);
        try
        {
            db.insert(DatabaseStrings.TBL_NAME, null,cv);
        }
        catch (SQLiteException sqle)
        {
            // Gestione delle eccezioni
        }
    }

    public boolean delete(long id)
    {
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        try
        {
            if (db.delete(DatabaseStrings.TBL_NAME, DatabaseStrings.FIELD_ID+"=?", new String[]{Long.toString(id)})>0)
                return true;
            return false;
        }
        catch (SQLiteException sqle)
        {
            return false;
        }

    }

    /**
     * Metodo che invoca una query per la cancellazione del record
     * @param id la chiave della riga da cancellare
     * @return
     */
    public boolean delete(String id){
        SQLiteDatabase db=dbhelper.getWritableDatabase();
        try{
            if (db.delete(DatabaseStrings.TBL_NAME, DatabaseStrings.FIELD_ID+"=?", new String[]{(id)})>0)
                return true;
            return false;
        }//try
        catch (SQLiteException sqle){
            return false;
        }//catch
    }//delete


    public Cursor query()
    {
        Cursor crs=null;
        try
        {
            SQLiteDatabase db=dbhelper.getReadableDatabase();
            crs=db.query(DatabaseStrings.TBL_NAME, null, null, null, null, null, null, null);
        }
        catch(SQLiteException sqle)
        {
            return null;
        }
        return crs;
    }

    // convert from bitmap to byte array
    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }



}