package database;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by Taschin Federico on 08/11/2017.
 */

@android.arch.persistence.room.Database(entities = {Ticket.class, Mission.class, Person.class}, version = 2)
public abstract class Database extends RoomDatabase {

    private static Database INSTANCE; //Unique instance of the Database object
    protected abstract DAO ticketDao(); //returns the DAO object that contains query methods. This method is automatically implemented by Room library

    /* Creates the unique instance of the Database object.
     * @param context not null, Context of the Activity that calls this method.
     * @return Database not null, single instance of the database
     */
    public static Database getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), Database.class, Constants.DATABASE_NAME)
                            .allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
    /**Destroys the Database instance (by setting it to null). getAppDatabase(context) must be called before using the saved instance of Database again
    **/
    public static void destroyInstance() {
        INSTANCE = null;
    }

    //Specifications for this methods will arrive, do not use for now
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }
    //Specifications for this methods will arrive, do not use for now
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }


}
