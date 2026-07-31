package com.rebeccabro.theweddingpath;

/*
 * Name: Rebecca Scranton
 * Date: July 30, 2026
 * Description: VendorDetailActivity manages the detailed profile view for
 * specific vendors,incorporating top-level vendor metadata fields and a
 * RecyclerView grid for managing associated sub-events and deadlines.
 */

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class VendorDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Bind the activity lifecycle to the vendor detail XML layout shell
        setContentView(R.layout.activity_vendor_detail);
    }
}