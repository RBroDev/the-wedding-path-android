package com.rebeccabro.theweddingpath;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.switchmaterial.SwitchMaterial; // Added import for the switch
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Activity responsible for managing and displaying detailed information for a specific vendor.
 * This includes updating vendor contact information and managing associated sub-events.
 */
public class VendorDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SubEventAdapter adapter;
    private List<SubEvent> subEventList;

    // UI Components
    private TextInputEditText etVendorName, etContact, etPhone, etAddress, etNotes;
    private SwitchMaterial switchSmsReminder; // Added switch state variable

    // State variables
    private int realUserId = -1;
    private String selectedVendorName = null;

    /**
     * Handles the system permission dialog response.
     * Ensures the app continues to function smoothly even if the user denies SMS access.
     */
    private final ActivityResultLauncher<String> requestSmsPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "SMS Notifications Enabled", Toast.LENGTH_SHORT).show();
                    // TODO: Activate background scheduling logic for this vendor (Issue #13)
                } else {
                    Toast.makeText(this, "SMS Access Denied. The app will continue without text alerts.", Toast.LENGTH_LONG).show();
                    // The app gracefully continues; UI reverts the SMS toggle to off
                    if (switchSmsReminder != null) {
                        switchSmsReminder.setChecked(false);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_detail);

        dbHelper = new DatabaseHelper(this);
        subEventList = new ArrayList<>();

        realUserId = getIntent().getIntExtra("USER_ID", -1);
        selectedVendorName = getIntent().getStringExtra("VENDOR_NAME");

        initializeUI();
        setupRecyclerView();

        if (selectedVendorName != null && !selectedVendorName.isEmpty()) {
            etVendorName.setText(selectedVendorName);
            loadVendorProfileDetails(selectedVendorName);
        }

        loadSubEvents();
    }

    /**
     * Binds UI components to their respective views and sets up click listeners.
     */
    private void initializeUI() {
        etVendorName = findViewById(R.id.et_vendor_name);
        etContact = findViewById(R.id.et_contact);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etNotes = findViewById(R.id.et_notes);

        // Bind the SMS Switch
        switchSmsReminder = findViewById(R.id.switch_sms_reminder);

        Button btnSaveVendor = findViewById(R.id.btn_save_vendor);
        Button btnAddEvent = findViewById(R.id.btn_add_event);
        Button btnDeleteVendor = findViewById(R.id.btn_delete_vendor);

        btnSaveVendor.setOnClickListener(v -> saveVendorToDatabase(true));
        btnAddEvent.setOnClickListener(v -> showAddEventDialog());
        btnDeleteVendor.setOnClickListener(v -> deleteEntireVendor());

        // Wire up the permission check to the switch toggle
        switchSmsReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkAndRequestSmsPermission();
            }
        });
    }

    /**
     * Initializes the RecyclerView and its adapter for displaying sub-events.
     */
    private void setupRecyclerView() {
        RecyclerView rvSubEvents = findViewById(R.id.rv_sub_events);
        rvSubEvents.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SubEventAdapter(subEventList, new SubEventAdapter.SubEventClickListener() {
            @Override
            public void onUpdateClick(SubEvent event) {
                handleEventUpdate(event);
            }

            @Override
            public void onDeleteClick(SubEvent event, int position) {
                handleEventDeletion(event, position);
            }
        });

        rvSubEvents.setAdapter(adapter);
    }

    /**
     * Displays a dialog to capture detailed sub-event information (Date, Time, Location)
     * and saves it to the database.
     */
    private void showAddEventDialog() {
        String currentVendorName = etVendorName.getText() != null ? etVendorName.getText().toString().trim() : "";
        if (currentVendorName.isEmpty()) {
            Toast.makeText(this, "Please enter and save a Vendor Name first.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedVendorName == null || selectedVendorName.isEmpty()) {
            saveVendorToDatabase(false);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.et_event_name);
        EditText etLocation = view.findViewById(R.id.et_event_location);
        EditText etDialogNotes = view.findViewById(R.id.et_event_notes);
        Button btnDate = view.findViewById(R.id.btn_pick_date);
        Button btnTime = view.findViewById(R.id.btn_pick_time);

        Calendar eventCalendar = Calendar.getInstance();

        btnDate.setOnClickListener(v -> new DatePickerDialog(this, (datePicker, year, month, day) -> {
            eventCalendar.set(Calendar.YEAR, year);
            eventCalendar.set(Calendar.MONTH, month);
            eventCalendar.set(Calendar.DAY_OF_MONTH, day);
            btnDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(eventCalendar.getTime()));
        }, eventCalendar.get(Calendar.YEAR), eventCalendar.get(Calendar.MONTH), eventCalendar.get(Calendar.DAY_OF_MONTH)).show());

        btnTime.setOnClickListener(v -> new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
            eventCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            eventCalendar.set(Calendar.MINUTE, minute);
            btnTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(eventCalendar.getTime()));
        }, eventCalendar.get(Calendar.HOUR_OF_DAY), eventCalendar.get(Calendar.MINUTE), false).show());

        builder.setPositiveButton("Save Event", (dialog, which) -> {
            String subName = etName.getText() != null ? etName.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
            String subNotes = etDialogNotes.getText() != null ? etDialogNotes.getText().toString().trim() : "";

            if (subName.isEmpty()) {
                Toast.makeText(this, "Event Name is required.", Toast.LENGTH_SHORT).show();
                return;
            }

            String packedTitle = subName;
            if (!location.isEmpty()) packedTitle += " @ " + location;
            if (!subNotes.isEmpty()) packedTitle += " | " + subNotes;

            String vName = etVendorName.getText() != null ? etVendorName.getText().toString().trim() : "";
            String vContact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
            String vAddress = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
            long vPhone = 0;
            try {
                String p = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                if (!p.isEmpty()) vPhone = Long.parseLong(p);
            } catch (Exception ignored) {}

            int userIdToUse = realUserId != -1 ? realUserId : 1;
            long timestamp = eventCalendar.getTimeInMillis();

            boolean isInserted = dbHelper.addEvent(
                    userIdToUse, vName, vAddress, vPhone, vContact, "", packedTitle, timestamp
            );

            if (isInserted) {
                Toast.makeText(this, "Event Scheduled!", Toast.LENGTH_SHORT).show();
                loadSubEvents();
            } else {
                Toast.makeText(this, "Error saving event.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Processes an update to an existing sub-event by launching an edit dialog.
     * Unpacks the packed title string to pre-fill the input fields.
     *
     * @param event The SubEvent object being updated.
     */
    private void handleEventUpdate(SubEvent event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_event, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.et_event_name);
        EditText etLocation = view.findViewById(R.id.et_event_location);
        EditText etDialogNotes = view.findViewById(R.id.et_event_notes);
        Button btnDate = view.findViewById(R.id.btn_pick_date);
        Button btnTime = view.findViewById(R.id.btn_pick_time);

        // Unpack the Title String to populate the edit form
        String packedTitle = event.getTitle();
        String parsedName = packedTitle;
        String parsedLocation = "";
        String parsedNotes = "";

        if (packedTitle.contains(" | ")) {
            int notesIndex = packedTitle.indexOf(" | ");
            parsedNotes = packedTitle.substring(notesIndex + 3);
            packedTitle = packedTitle.substring(0, notesIndex);
        }
        if (packedTitle.contains(" @ ")) {
            int locIndex = packedTitle.indexOf(" @ ");
            parsedLocation = packedTitle.substring(locIndex + 3);
            parsedName = packedTitle.substring(0, locIndex);
        }

        etName.setText(parsedName);
        etLocation.setText(parsedLocation);
        etDialogNotes.setText(parsedNotes);

        // Set up the Calendar and Buttons to reflect the event's currently saved time
        Calendar eventCalendar = Calendar.getInstance();
        eventCalendar.setTimeInMillis(event.getTimestamp());

        btnDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(eventCalendar.getTime()));
        btnTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(eventCalendar.getTime()));

        btnDate.setOnClickListener(v -> new DatePickerDialog(this, (datePicker, year, month, day) -> {
            eventCalendar.set(Calendar.YEAR, year);
            eventCalendar.set(Calendar.MONTH, month);
            eventCalendar.set(Calendar.DAY_OF_MONTH, day);
            btnDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(eventCalendar.getTime()));
        }, eventCalendar.get(Calendar.YEAR), eventCalendar.get(Calendar.MONTH), eventCalendar.get(Calendar.DAY_OF_MONTH)).show());

        btnTime.setOnClickListener(v -> new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
            eventCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            eventCalendar.set(Calendar.MINUTE, minute);
            btnTime.setText(new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(eventCalendar.getTime()));
        }, eventCalendar.get(Calendar.HOUR_OF_DAY), eventCalendar.get(Calendar.MINUTE), false).show());

        builder.setPositiveButton("Update Event", (dialog, which) -> {
            String subName = etName.getText() != null ? etName.getText().toString().trim() : "";
            String location = etLocation.getText() != null ? etLocation.getText().toString().trim() : "";
            String subNotes = etDialogNotes.getText() != null ? etDialogNotes.getText().toString().trim() : "";

            if (subName.isEmpty()) {
                Toast.makeText(this, "Event Name is required.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pack it back up
            String newPackedTitle = subName;
            if (!location.isEmpty()) newPackedTitle += " @ " + location;
            if (!subNotes.isEmpty()) newPackedTitle += " | " + subNotes;

            String vName = etVendorName.getText() != null ? etVendorName.getText().toString().trim() : "";
            String vContact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
            String vAddress = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
            long vPhone = 0;
            try {
                String p = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
                if (!p.isEmpty()) vPhone = Long.parseLong(p);
            } catch (Exception ignored) {}

            boolean isUpdated = dbHelper.updateEvent(
                    event.getId(), vName, vAddress, vPhone, vContact, "", newPackedTitle, eventCalendar.getTimeInMillis()
            );

            if (isUpdated) {
                Toast.makeText(this, "Event Updated!", Toast.LENGTH_SHORT).show();
                loadSubEvents();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    /**
     * Processes the deletion of a specific sub-event and updates the UI accordingly.
     *
     * @param event    The SubEvent object to delete.
     * @param position The position of the event in the adapter.
     */
    private void handleEventDeletion(SubEvent event, int position) {
        boolean isDeleted = dbHelper.deleteEvent(event.getId());

        if (isDeleted) {
            subEventList.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(VendorDetailActivity.this, "Event Deleted", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads the vendor's profile details from the database and populates the UI fields.
     *
     * @param vendorName The name of the vendor to retrieve data for.
     */
    private void loadVendorProfileDetails(String vendorName) {
        int userIdToUse = realUserId != -1 ? realUserId : 1;
        Cursor cursor = dbHelper.getUserEvents(userIdToUse);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_NAME);

                // Match the specific vendor to populate fields
                if (nameIndex != -1 && vendorName.equals(cursor.getString(nameIndex))) {
                    int addressIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_ADDRESS);
                    int phoneIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_PHONE);
                    int contactIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_CONTACT);
                    int notesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_NOTES);

                    if (addressIndex != -1) etAddress.setText(cursor.getString(addressIndex));
                    if (phoneIndex != -1) {
                        long phoneVal = cursor.getLong(phoneIndex);
                        if (phoneVal != 0) etPhone.setText(String.valueOf(phoneVal));
                    }
                    if (contactIndex != -1) etContact.setText(cursor.getString(contactIndex));
                    if (notesIndex != -1) etNotes.setText(cursor.getString(notesIndex));

                    break; // Stop iterating once the target vendor is found
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    /**
     * Saves or updates the vendor information in the database.
     * Handles both top-level vendor profile updates and the addition of new sub-events.
     *
     * @param isSaveVendorClick true if the action was triggered by the "Save Vendor" button,
     *                          false if triggered by "Add Event" (hidden root saving).
     */
    private void saveVendorToDatabase(boolean isSaveVendorClick) {
        String name = etVendorName.getText() != null ? etVendorName.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String phoneStr = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Vendor Name is required.", Toast.LENGTH_SHORT).show();
            return;
        }

        long phone = 0;
        if (!phoneStr.isEmpty()) {
            try {
                phone = Long.parseLong(phoneStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid phone number (numbers only).", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int userIdToUse = realUserId != -1 ? realUserId : 1;

        // Sync updated top-level form fields across all existing rows for this vendor
        if (selectedVendorName != null && !selectedVendorName.isEmpty()) {
            Cursor cursor = dbHelper.getUserEvents(userIdToUse);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_NAME);
                    if (nameIndex != -1 && selectedVendorName.equals(cursor.getString(nameIndex))) {

                        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_ID);
                        int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUB_EVENT_TITLE);
                        int timeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP);

                        // Ensure column indexes are valid before extracting data
                        if (idIndex != -1 && titleIndex != -1 && timeIndex != -1) {
                            int eventId = cursor.getInt(idIndex);
                            String rowTitle = cursor.getString(titleIndex);
                            long rowTime = cursor.getLong(timeIndex);

                            dbHelper.updateEvent(eventId, name, address, phone, contact, notes, rowTitle, rowTime);
                        }
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
        }

        // Create an anchor record for brand-new vendors.
        if (selectedVendorName == null || selectedVendorName.isEmpty()) {
            dbHelper.addEvent(userIdToUse, name, address, phone, contact, notes, "VENDOR_ROOT", System.currentTimeMillis());
            selectedVendorName = name;
        }

        if (isSaveVendorClick) {
            Toast.makeText(this, "Vendor saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * Retrieves all sub-events associated with the selected vendor from the database,
     * sorts them chronologically, and refreshes the RecyclerView.
     */
    @SuppressLint("NotifyDataSetChanged")
    private void loadSubEvents() {
        subEventList.clear();

        int userIdToUse = realUserId != -1 ? realUserId : 1;
        Cursor cursor = dbHelper.getUserEvents(userIdToUse);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_ID);
                int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUB_EVENT_TITLE);
                int timeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP);
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_NAME);

                if (idIndex != -1 && titleIndex != -1 && timeIndex != -1 && nameIndex != -1) {
                    String vName = cursor.getString(nameIndex);
                    String title = cursor.getString(titleIndex);

                    if (selectedVendorName == null || selectedVendorName.isEmpty() || (vName != null && vName.equals(selectedVendorName))) {
                        if (title != null && !title.equals("VENDOR_ROOT") && !title.equals("Initial Consultation")) {
                            int eventId = cursor.getInt(idIndex);
                            long timestamp = cursor.getLong(timeIndex);
                            subEventList.add(new SubEvent(eventId, title, timestamp));
                        }
                    }
                }
            } while (cursor.moveToNext());

            cursor.close();
        }

        // Sort and apply one clean UI update
        subEventList.sort(Comparator.comparingLong(SubEvent::getTimestamp));
        adapter.notifyDataSetChanged();
    }

    /**
     * Cascades deletion across all database records (both visible sub-events and hidden root records)
     * associated with the currently selected vendor.
     */
    private void deleteEntireVendor() {
        if (selectedVendorName == null || selectedVendorName.isEmpty()) {
            finish();
            return;
        }

        int userIdToUse = realUserId != -1 ? realUserId : 1;
        Cursor cursor = dbHelper.getUserEvents(userIdToUse);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int nameIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_NAME);

                if (nameIndex != -1 && selectedVendorName.equals(cursor.getString(nameIndex))) {
                    int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_ID);

                    if (idIndex != -1) {
                        int eventId = cursor.getInt(idIndex);
                        dbHelper.deleteEvent(eventId);
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        Toast.makeText(this, "Vendor completely deleted.", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Evaluates the current SMS permission state at runtime.
     * Triggers the system request dialog if access has not yet been granted.
     */
    private void checkAndRequestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted
            Toast.makeText(this, "SMS Notifications are already active.", Toast.LENGTH_SHORT).show();
            // TODO: Activate background scheduling logic for this vendor (Issue #13)
        } else {
            // Trigger the system permissions request dialog
            requestSmsPermissionLauncher.launch(Manifest.permission.SEND_SMS);
        }
    }
}