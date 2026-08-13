package com.rebeccabro.theweddingpath;

/*
 * Name: Rebecca Scranton
 * Date: August 13, 2026
 * Description: Manages the detailed profile view for specific vendors.
 * Captures user input to persist new event_details records to the SQLite database
 * and manages the sub-event RecyclerView grid (CRUD Operations).
 */

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

public class VendorDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SubEventAdapter adapter;
    private List<SubEvent> subEventList;

    // UI elements scoped globally in the class so they can be accessed by the Update logic
    private TextInputEditText etVendorName, etContact, etPhone, etAddress, etNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_detail);

        dbHelper = new DatabaseHelper(this);
        subEventList = new ArrayList<>();

        etVendorName = findViewById(R.id.et_vendor_name);
        etContact = findViewById(R.id.et_contact);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etNotes = findViewById(R.id.et_notes);
        Button btnAddEvent = findViewById(R.id.btn_add_event);

        // Initialize the RecyclerView for the sub-event grid locally to save memory
        RecyclerView rvSubEvents = findViewById(R.id.rv_sub_events);
        rvSubEvents.setLayoutManager(new LinearLayoutManager(this));

        /*
         * Wire up the Adapter and its click listener interface.
         * This handles the UPDATE and DELETE commands sent from the individual row items.
         */
        adapter = new SubEventAdapter(subEventList, new SubEventAdapter.SubEventClickListener() {
            @Override
            public void onUpdateClick(SubEvent event) {
                // UPDATE OPERATION: Modify existing item values in the database.
                // Appending a flag to prove the SQL update succeeds without needing a complex UI dialog.
                String updatedTitle = event.getTitle() + " (Updated)";
                String currentName = etVendorName.getText() != null ? etVendorName.getText().toString().trim() : "Vendor";

                boolean isUpdated = dbHelper.updateEvent(
                        event.getId(), currentName, "", 0, "", "", updatedTitle, event.getTimestamp()
                );

                if (isUpdated) {
                    Toast.makeText(VendorDetailActivity.this, "Event Updated!", Toast.LENGTH_SHORT).show();
                    loadSubEvents(); // Refresh Grid to show changes
                }
            }

            @Override
            public void onDeleteClick(SubEvent event, int position) {
                // DELETE OPERATION: Row-level removal of items from the database.
                boolean isDeleted = dbHelper.deleteEvent(event.getId());

                if (isDeleted) {
                    subEventList.remove(position);
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(VendorDetailActivity.this, "Event Deleted", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rvSubEvents.setAdapter(adapter);

        // READ OPERATION: Initial load of grid data
        loadSubEvents();

        // CREATE OPERATION: Save new vendor details and events
        btnAddEvent.setOnClickListener(v -> {

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

            /*
             * TODO: Replace mock data once authentication intent routing is implemented.
             * Mocking UserId (1) and default timestamp to satisfy DB schema constraints.
             */
            int mockUserId = 1;
            String defaultEventTitle = "Initial Consultation";
            long currentTimestamp = System.currentTimeMillis();

            boolean isInserted = dbHelper.addEvent(
                    mockUserId, name, address, phone, contact, notes, defaultEventTitle, currentTimestamp
            );

            if (isInserted) {
                Toast.makeText(this, "Vendor details saved successfully!", Toast.LENGTH_SHORT).show();

                etVendorName.setText("");
                etContact.setText("");
                etPhone.setText("");
                etAddress.setText("");
                etNotes.setText("");

                // READ OPERATION: Refresh the grid so the new event instantly appears
                loadSubEvents();
            } else {
                Toast.makeText(this, "Error saving vendor details. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * READ OPERATION: Queries the database for all events and updates the RecyclerView.
     */
    private void loadSubEvents() {
        int oldSize = subEventList.size();
        subEventList.clear();
        if (oldSize > 0) {
            adapter.notifyItemRangeRemoved(0, oldSize);
        }

        int mockUserId = 1; // TODO: Replace with authenticated user ID
        Cursor cursor = dbHelper.getUserEvents(mockUserId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_EVENT_ID);
                int titleIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SUB_EVENT_TITLE);
                int timeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIMESTAMP);

                // Prevent NullPointerExceptions if column names change
                if (idIndex != -1 && titleIndex != -1 && timeIndex != -1) {
                    int eventId = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    long timestamp = cursor.getLong(timeIndex);

                    subEventList.add(new SubEvent(eventId, title, timestamp));
                }
            } while (cursor.moveToNext());

            cursor.close(); // Prevent memory leaks
        }

        if (!subEventList.isEmpty()) {
            adapter.notifyItemRangeInserted(0, subEventList.size());
        }
    }
}