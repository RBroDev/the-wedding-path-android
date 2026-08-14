package com.rebeccabro.theweddingpath;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Primary data hub for The Wedding Path application.
 * Orchestrates the custom RecyclerView that displays the user's saved vendors
 * in an organic, winding path UI layout by reading from the local SQLite database.
 */
public class DashboardActivity extends AppCompatActivity {

    private List<String> vendorList;
    private DatabaseHelper dbHelper;
    private int realUserId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        vendorList = new ArrayList<>();

        realUserId = getIntent().getIntExtra("USER_ID", -1);

        // Reserve the first index for the action item to create a new vendor
        vendorList.add("+ Add New Vendor");

        loadVendorsFromDatabase();

        RecyclerView recyclerView = findViewById(R.id.rv_wedding_path);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        StoneAdapter adapter = new StoneAdapter(vendorList, vendorName -> {
            Intent intent = new Intent(DashboardActivity.this, VendorDetailActivity.class);
            intent.putExtra("USER_ID", realUserId);

            // Pass the selected vendor name to the detail view, or omit for a new vendor entry
            if (!vendorName.equals("+ Add New Vendor")) {
                intent.putExtra("VENDOR_NAME", vendorName);
            }

            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the vendor list when returning to the dashboard to reflect any newly added or deleted data
        vendorList.clear();
        vendorList.add("+ Add New Vendor");
        loadVendorsFromDatabase();

        RecyclerView recyclerView = findViewById(R.id.rv_wedding_path);
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    /**
     * Queries the database for the user's saved events and extracts
     * distinct vendor names to populate the dynamic path interface.
     */
    private void loadVendorsFromDatabase() {
        int userIdToUse = realUserId != -1 ? realUserId : 1;
        Cursor cursor = dbHelper.getUserEvents(userIdToUse);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_NAME);

                if (nameColumnIndex != -1) {
                    String vendorName = cursor.getString(nameColumnIndex) != null ? cursor.getString(nameColumnIndex) : "";

                    // Ensure only distinct, non-empty vendor names are added to the UI list
                    if (!vendorName.isEmpty() && !vendorList.contains(vendorName)) {
                        vendorList.add(vendorName);
                    }
                }
            } while (cursor.moveToNext());

            cursor.close();
        }
    }
}