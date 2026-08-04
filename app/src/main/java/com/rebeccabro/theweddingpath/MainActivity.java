package com.rebeccabro.theweddingpath;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Name: Rebecca Scranton
 * Date: August 3, 2026
 * Description: MainActivity acts as the initial entry point for The Wedding Path.
 * It manages user authentication and first-time account creation by securely
 * capturing user input and communicating with the local SQLite database.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure modern edge-to-edge window insets
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new DatabaseHelper(this);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        Button btnRegister = findViewById(R.id.button2);

        btnRegister.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // Input validation
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both a username and password.", Toast.LENGTH_SHORT).show();
            } else {

                // Attempt data persistence
                boolean isInserted = dbHelper.addUser(user, pass);

                if (isInserted) {
                    Toast.makeText(MainActivity.this, "Journey Begun! Account created.", Toast.LENGTH_SHORT).show();
                    // Reset UI state on success
                    etUsername.setText("");
                    etPassword.setText("");
                } else {
                    // Insertion failed (typically due to SQLite UNIQUE constraint violation)
                    Toast.makeText(MainActivity.this, "Username already exists. Please choose another.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}