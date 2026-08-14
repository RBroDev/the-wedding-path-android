package com.rebeccabro.theweddingpath;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

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

    // State variables
    private int realUserId = -1;
    private String selectedVendorName = null;

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

        Button btnSaveVendor = findViewById(R.id.btn_save_vendor);
        Button btnAddEvent = findViewById(R.id.btn_add_event);
        Button btnDeleteVendor = findViewById(R.id.btn_delete_vendor);

        btnSaveVendor.setOnClickListener(v -> saveVendorToDatabase(true));
        btnAddEvent.setOnClickListener(v -> saveVendorToDatabase(false));
        btnDeleteVendor.setOnClickListener(v -> deleteEntireVendor());
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
     * Processes an update to an existing sub-event.
     * Validates user input before committing changes to the database.
     *
     * @param event The SubEvent object being updated.
     */
    private void handleEventUpdate(SubEvent event) {
        String updatedTitle = event.getTitle() + " (Updated)";
        String name = etVendorName.getText() != null ? etVendorName.getText().toString().trim() : "";
        String address = etAddress.getText() != null ? etAddress.getText().toString().trim() : "";
        String phoneStr = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String contact = etContact.getText() != null ? etContact.getText().toString().trim() : "";
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        long phone = 0;
        if (!phoneStr.isEmpty()) {
            try {
                phone = Long.parseLong(phoneStr);
            } catch (NumberFormatException e) {
                Toast.makeText(VendorDetailActivity.this, "Please enter a valid phone number (numbers only).", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        boolean isUpdated = dbHelper.updateEvent(
                event.getId(), name, address, phone, contact, notes, updatedTitle, event.getTimestamp()
        );

        if (isUpdated) {
            Toast.makeText(VendorDetailActivity.this, "Event Updated!", Toast.LENGTH_SHORT).show();
            loadSubEvents();
        }
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
     *                          false if triggered by "Add Event".
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
        // The "VENDOR_ROOT" title acts as a flag to hide this record from the sub-event UI grid.
        if (selectedVendorName == null || selectedVendorName.isEmpty()) {
            dbHelper.addEvent(userIdToUse, name, address, phone, contact, notes, "VENDOR_ROOT", System.currentTimeMillis());
            selectedVendorName = name;
        }

        // Handle specific button execution paths
        if (isSaveVendorClick) {
            Toast.makeText(this, "Vendor saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            dbHelper.addEvent(userIdToUse, name, address, phone, contact, notes, "New Sub-Event", System.currentTimeMillis());
            loadSubEvents();
            Toast.makeText(this, "Event added!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Retrieves all sub-events associated with the selected vendor from the database
     * and refreshes the RecyclerView.
     */
    private void loadSubEvents() {
        int oldSize = subEventList.size();
        subEventList.clear();
        if (oldSize > 0) {
            adapter.notifyItemRangeRemoved(0, oldSize);
        }

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

                        // Exclude administrative roots and legacy data configurations from the UI
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

        if (!subEventList.isEmpty()) {
            adapter.notifyItemRangeInserted(0, subEventList.size());
        }
    }

    /**
     * Cascades deletion across all database records (both visible sub-events and hidden root records)
     * associated with the currently selected vendor.
     */
    private void deleteEntireVendor() {
        // Exit if attempting to delete an unsaved vendor
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
}