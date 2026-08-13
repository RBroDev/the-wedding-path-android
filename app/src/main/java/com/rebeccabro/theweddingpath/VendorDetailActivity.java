package com.rebeccabro.theweddingpath;

/*
 * Name: Rebecca Scranton
 * Date: August 13, 2026
 * Description: VendorDetailActivity manages the detailed profile view for
 * specific vendors. It captures user input to create new database records
 * and manages the sub-event RecyclerView grid.
 */

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Manages the detailed profile view for specific vendors.
 * Captures user input to persist new event_details records to the SQLite database
 * and manages the sub-event RecyclerView grid.
 */
public class VendorDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_detail);

        dbHelper = new DatabaseHelper(this);

        // UI elements scoped locally for memory efficiency
        TextInputEditText etVendorName = findViewById(R.id.et_vendor_name);
        TextInputEditText etContact = findViewById(R.id.et_contact);
        TextInputEditText etPhone = findViewById(R.id.et_phone);
        TextInputEditText etAddress = findViewById(R.id.et_address);
        TextInputEditText etNotes = findViewById(R.id.et_notes);
        Button btnAddEvent = findViewById(R.id.btn_add_event);

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

                // TODO: Trigger read operation to refresh RecyclerView grid
            } else {
                Toast.makeText(this, "Error saving vendor details. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}