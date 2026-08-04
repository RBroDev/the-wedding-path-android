package com.rebeccabro.theweddingpath;

/*
 * Name: Rebecca Scranton
 * Date: August 3, 2026
 * Description: Manages the local SQLite database creation
 * and versioning for The Wedding Path, defining the relational
 * schema for the user_credentials and event_details tables.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class that manages database creation and version management for The Wedding Path application.
 * This class handles the initialization of the user_credentials and event_details tables,
 * establishing the relational schema necessary for local data persistence.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database schema constants
    private static final String DATABASE_NAME = "wedding_path.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USER_CREDENTIALS = "user_credentials";
    public static final String TABLE_EVENT_DETAILS = "event_details";

    // User Credentials table columns
    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // Event Details table columns
    public static final String COLUMN_EVENT_ID = "_id";
    public static final String COLUMN_FK_USER_ID = "user_id"; // Foreign key linking to the user
    public static final String COLUMN_VENDOR_NAME = "vendor_name";
    public static final String COLUMN_VENDOR_ADDRESS = "vendor_address";
    public static final String COLUMN_VENDOR_PHONE = "vendor_phone";
    public static final String COLUMN_VENDOR_CONTACT = "vendor_contact";
    public static final String COLUMN_VENDOR_NOTES = "vendor_notes";
    public static final String COLUMN_SUB_EVENT_TITLE = "sub_event_title";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // SQL statement to create the User Credentials table
    private static final String CREATE_TABLE_USER_CREDENTIALS =
            "CREATE TABLE " + TABLE_USER_CREDENTIALS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT)";

    // SQL statement to create the Event Details table with a foreign key constraint
    private static final String CREATE_TABLE_EVENT_DETAILS =
            "CREATE TABLE " + TABLE_EVENT_DETAILS + " (" +
                    COLUMN_EVENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_FK_USER_ID + " INTEGER, " +
                    COLUMN_VENDOR_NAME + " TEXT, " +
                    COLUMN_VENDOR_ADDRESS + " TEXT, " +
                    COLUMN_VENDOR_PHONE + " INTEGER, " +
                    COLUMN_VENDOR_CONTACT + " TEXT, " +
                    COLUMN_VENDOR_NOTES + " TEXT, " +
                    COLUMN_SUB_EVENT_TITLE + " TEXT, " +
                    COLUMN_TIMESTAMP + " INTEGER, " +
                    "FOREIGN KEY(" + COLUMN_FK_USER_ID + ") REFERENCES " + TABLE_USER_CREDENTIALS + "(" + COLUMN_USER_ID + "))";

    /**
     * Constructor for DatabaseHelper.
     *
     * @param context The application context used to locate paths to the database.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time.
     * Executes the SQL statements to build the initial schema.
     *
     * @param db The SQLite database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_CREDENTIALS);
        db.execSQL(CREATE_TABLE_EVENT_DETAILS);
    }

    /**
     * Called when the database needs to be upgraded.
     * Drops existing tables and recreates them to apply the new schema configuration.
     *
     * @param db         The SQLite database instance.
     * @param oldVersion The old database version integer.
     * @param newVersion The new database version integer.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CREDENTIALS);
        onCreate(db);
    }
}
