package database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

/**
 * Created by Taschin Federico on 08/11/2017.
 */

@android.arch.persistence.room.Database(entities = {Ticket.class, Mission.class, Person.class}, version = 3)
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
                            .allowMainThreadQueries().addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Ticket ADD COLUMN Title STRING");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Ticket ADD COLUMN Description STRING");
        }
    };

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
