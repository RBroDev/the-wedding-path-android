package com.rebeccabro.theweddingpath;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Name: Rebecca Scranton
 * Date: August 4, 2026
 * Description: Manages database creation, version management, and local data persistence for The Wedding Path.
 * Initializes the user_credentials and event_details tables and establishes the core relational schema.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "wedding_path.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_USER_CREDENTIALS = "user_credentials";
    public static final String TABLE_EVENT_DETAILS = "event_details";

    public static final String COLUMN_USER_ID = "_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    public static final String COLUMN_EVENT_ID = "_id";
    public static final String COLUMN_FK_USER_ID = "user_id";
    public static final String COLUMN_VENDOR_NAME = "vendor_name";
    public static final String COLUMN_VENDOR_ADDRESS = "vendor_address";
    public static final String COLUMN_VENDOR_PHONE = "vendor_phone";
    public static final String COLUMN_VENDOR_CONTACT = "vendor_contact";
    public static final String COLUMN_VENDOR_NOTES = "vendor_notes";
    public static final String COLUMN_SUB_EVENT_TITLE = "sub_event_title";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_USER_CREDENTIALS =
            "CREATE TABLE " + TABLE_USER_CREDENTIALS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USERNAME + " TEXT UNIQUE, " +
                    COLUMN_PASSWORD + " TEXT)";

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER_CREDENTIALS);
        db.execSQL(CREATE_TABLE_EVENT_DETAILS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENT_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_CREDENTIALS);
        onCreate(db);
    }

    /**
     * Persists a new user credential record to the database.
     *
     * @param username The requested username
     * @param password The requested password
     * @return true if the insertion was successful, false on constraint violation (e.g., duplicate username)
     */
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USER_CREDENTIALS, null, values);
        db.close();

        return result != -1;
    }

    /**
     * Authenticates a user against stored database credentials.
     *
     * @param username The inputted username
     * @param password The inputted password
     * @return true if the credentials match an existing record, false otherwise
     */
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM user_credentials WHERE username = ? AND password = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username, password});

        boolean isValid = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return isValid;
    }

    /**
     * CREATE: Persists a new vendor and event record to the database.
     * Utilizes ContentValues to map inputs to the event_details schema safely.
     *
     * @param userId The ID of the authenticated user.
     * @param vendorName The business name of the vendor.
     * @param address The physical address of the vendor.
     * @param phone The contact phone number (passed as long to prevent integer overflow).
     * @param contact The primary point of contact.
     * @param notes Additional details or notes.
     * @param subEventTitle The title of the specific milestone/event.
     * @param timestamp The date/time of the event.
     * @return true if the insertion was successful, false otherwise.
     */

    public boolean addEvent(int userId, String vendorName, String address, long phone, String contact, String notes, String subEventTitle, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FK_USER_ID, userId);
        values.put(COLUMN_VENDOR_NAME, vendorName);
        values.put(COLUMN_VENDOR_ADDRESS, address);
        values.put(COLUMN_VENDOR_PHONE, phone);
        values.put(COLUMN_VENDOR_CONTACT, contact);
        values.put(COLUMN_VENDOR_NOTES, notes);
        values.put(COLUMN_SUB_EVENT_TITLE, subEventTitle);
        values.put(COLUMN_TIMESTAMP, timestamp);

        long result = db.insert(TABLE_EVENT_DETAILS, null, values);
        db.close();
        return result != -1;
    }

    /**
     * READ: Retrieves all vendor events associated with a specific user.
     *
     * @param userId The ID of the authenticated user.
     * @return A Cursor containing the user's populated event records.
     */
    public Cursor getUserEvents(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_EVENT_DETAILS + " WHERE " + COLUMN_FK_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    /**
     * UPDATE: Modifies an existing vendor/event record in the database.
     *
     * @param eventId The unique identifier of the specific event to update.
     * @param vendorName The updated business name of the vendor.
     * @param address The updated physical address.
     * @param phone The updated phone number.
     * @param contact The updated point of contact.
     * @param notes Updated notes.
     * @param subEventTitle The updated milestone/event title.
     * @param timestamp The updated date/time.
     * @return true if the update modified at least one row, false otherwise.
     */
    public boolean updateEvent(int eventId, String vendorName, String address, long phone, String contact, String notes, String subEventTitle, long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_VENDOR_NAME, vendorName);
        values.put(COLUMN_VENDOR_ADDRESS, address);
        values.put(COLUMN_VENDOR_PHONE, phone);
        values.put(COLUMN_VENDOR_CONTACT, contact);
        values.put(COLUMN_VENDOR_NOTES, notes);
        values.put(COLUMN_SUB_EVENT_TITLE, subEventTitle);
        values.put(COLUMN_TIMESTAMP, timestamp);

        int result = db.update(TABLE_EVENT_DETAILS, values, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});
        db.close();
        return result > 0;
    }

    /**
     * DELETE: Removes a specific event record from the database entirely.
     *
     * @param eventId The unique identifier of the event to delete.
     * @return true if the deletion successfully removed a row, false otherwise.
     */
    public boolean deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EVENT_DETAILS, COLUMN_EVENT_ID + " = ?", new String[]{String.valueOf(eventId)});
        db.close();
        return result > 0;
    }
}