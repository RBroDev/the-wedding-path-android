package com.rebeccabro.theweddingpath;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/*
 * Name: Rebecca Scranton
 * Date: August 4, 2026
 * Description: Acts as the primary entry point for The Wedding Path application.
 * Manages user authentication and account registration via local SQLite persistence,
 * and routes authenticated sessions to the main dashboard.
 */
public class MainActivity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        Button btnRegister = findViewById(R.id.btn_register);
        Button btnLogin = findViewById(R.id.btn_login);

        // Process new user account creation
        btnRegister.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // Extract the local-part of an email address to use as a clean display name
            if (user.contains("@")) {
                user = user.split("@")[0];
            }

            // Ensure required credential fields are populated
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both a username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Enforce hard length constraints to prevent buffer/memory exploitation
            if (user.length() > 30 || pass.length() > 64) {
                Toast.makeText(MainActivity.this, "Security Error: Input exceeds maximum allowed length.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Sanitize input to allow only alphanumeric characters, periods, and standard email symbols
            if (!user.matches("^[a-zA-Z0-9.@]+$")) {
                Toast.makeText(MainActivity.this, "Security Error: Usernames can only contain letters, numbers, and periods.", Toast.LENGTH_LONG).show();
                return;
            }

            if (dbHelper.addUser(user, pass)) {
                Toast.makeText(MainActivity.this, "Journey Begun! Account created.", Toast.LENGTH_SHORT).show();
                etUsername.setText("");
                etPassword.setText("");
            } else {
                Toast.makeText(MainActivity.this, "Username already exists. Please choose another.", Toast.LENGTH_LONG).show();
            }
        });

        // Authenticate existing users
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // Format input to match the database username structure
            if (user.contains("@")) {
                user = user.split("@")[0];
            }

            // Verify inputs are present before querying the database
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reject invalid lengths immediately to bypass unnecessary database overhead
            if (user.length() > 30 || pass.length() > 64) {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            // Block malformed strings or illegal characters
            if (!user.matches("^[a-zA-Z0-9.@]+$")) {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            if (dbHelper.checkUser(user, pass)) {
                Toast.makeText(MainActivity.this, "Welcome to The Wedding Path!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);

                // Terminate the authentication activity to prevent reverse navigation
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}