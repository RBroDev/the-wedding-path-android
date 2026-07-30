package com.rebeccabro.theweddingpath;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/*
 * Name: Rebecca Scranton
 * Date: July 29, 2026
 * Description: DashboardActivity acts as the primary data hub for "The Wedding Path"
 * application. It is responsible for orchestrating the custom RecyclerView that
 * displays the user's wedding milestones in an organic, winding path UI layout.
 */

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge display for modern Android window insets
        EdgeToEdge.enable(this);

        // Inflate the dashboard layout containing the RecyclerView
        setContentView(R.layout.activity_dashboard);

        // Bind the RecyclerView component from the XML layout
        RecyclerView recyclerView = findViewById(R.id.rv_wedding_path);

        // Apply a vertical layout manager and tell it to build from the bottom up
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);

        /* * Initialize mock dataset for UI validation.
         * These specific strings simulate various database states (populated,
         * pending, and actionable) to test the dynamic alpha-rendering logic
         * housed within the StoneAdapter.
         */
        List<String> testVendors = new ArrayList<>();
        testVendors.add("Venue (Booked)");
        testVendors.add("Transportation");
        testVendors.add("Baker");
        testVendors.add("DJ / Band");
        testVendors.add("Florist");
        testVendors.add("Caterer");
        testVendors.add("Photographer");
        testVendors.add("+ Add New Vendor");

        // Instantiate the custom adapter with the mock dataset and attach it to the view
        StoneAdapter adapter = new StoneAdapter(testVendors);
        recyclerView.setAdapter(adapter);
    }
}
