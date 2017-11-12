package database;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.Database;
import android.content.Context;

/**
 * Created by Taschin Federico on 08/11/2017.
 */

@Database(entities = {TicketEntity.class}, version = 1)
public abstract class TicketDatabase extends RoomDatabase {

    private static TicketDatabase INSTANCE; //Unique instance of the Database object
    public abstract DAO ticketDao(); //returns the DAO object that contains query methods. This method is automatically implemented by Room library

    /* Creates the unique instance of the Database object.
     * @param context not null, Context of the Activity that calls this method.
     * @return TicketDatabase not null, single instance of the database
     */
    public static TicketDatabase getAppDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TicketDatabase.class, DatabaseConstants.DATABASE_NAME)
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
    //pecifications for this methods will arrive, do not use for now
    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }


}
