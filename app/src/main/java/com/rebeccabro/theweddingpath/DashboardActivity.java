package com.rebeccabro.theweddingpath;

import android.database.Cursor;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Name: Rebecca Scranton
 * Date: August 13, 2026
 * Description: DashboardActivity acts as the primary data hub for "The Wedding Path"
 * application. It orchestrates the custom RecyclerView that displays the user's
 * saved vendors in an organic, winding path UI layout by reading from the SQLite database.
 */
public class DashboardActivity extends AppCompatActivity {

    private List<String> vendorList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        vendorList = new ArrayList<>();

        /*
         * Pin the "+ Add New Vendor" action to the start of the list.
         * Due to setReverseLayout(true) on the layout manager, index 0
         * visually renders at the physical bottom of the user's screen.
         */
        vendorList.add("+ Add New Vendor");

        loadVendorsFromDatabase();

        // UI elements scoped locally for memory efficiency
        RecyclerView recyclerView = findViewById(R.id.rv_wedding_path);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        StoneAdapter adapter = new StoneAdapter(vendorList);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Queries the database for the user's saved events and extracts
     * distinct vendor names to populate the dynamic Winding Path.
     */
    private void loadVendorsFromDatabase() {
        /*
         * TODO: Replace mock data once authentication intent routing is implemented.
         * Mocking UserId (1) to satisfy DB schema constraints.
         */
        int mockUserId = 1;
        Cursor cursor = dbHelper.getUserEvents(mockUserId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int nameColumnIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_VENDOR_NAME);

                if (nameColumnIndex != -1) {
                    String vendorName = cursor.getString(nameColumnIndex) != null ? cursor.getString(nameColumnIndex) : "";

                    if (!vendorName.isEmpty() && !vendorList.contains(vendorName)) {
                        vendorList.add(vendorName);
                    }
                }
            } while (cursor.moveToNext());

            // Cursors must be explicitly closed to prevent critical memory leaks
            cursor.close();
        }
    }
}