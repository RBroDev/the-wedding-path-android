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

/**
 * Primary entry point for The Wedding Path application.
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

        // Configure edge-to-edge layout to account for system bars
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

        // Handle new user registration
        btnRegister.setOnClickListener(v -> {
            String user = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // Strip domain if the user enters an email address
            if (user.contains("@")) {
                user = user.split("@")[0];
            }

            // Input validation for security and data integrity
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both a username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.length() > 30 || pass.length() > 64) {
                Toast.makeText(MainActivity.this, "Security Error: Input exceeds maximum allowed length.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!user.matches("^[a-zA-Z0-9.@]+$")) {
                Toast.makeText(MainActivity.this, "Security Error: Usernames can only contain letters, numbers, and periods.", Toast.LENGTH_LONG).show();
                return;
            }

            // Attempt to persist the new user to the database
            if (dbHelper.addUser(user, pass)) {
                Toast.makeText(MainActivity.this, "Journey Begun! Account created.", Toast.LENGTH_SHORT).show();
                etUsername.setText("");
                etPassword.setText("");
            } else {
                Toast.makeText(MainActivity.this, "Username already exists. Please choose another.", Toast.LENGTH_LONG).show();
            }
        });

        // Handle existing user authentication
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
            String pass = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // Strip domain if the user enters an email address
            if (user.contains("@")) {
                user = user.split("@")[0];
            }

            // Input validation
            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user.length() > 30 || pass.length() > 64) {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            if (!user.matches("^[a-zA-Z0-9.@]+$")) {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
                return;
            }

            // Authenticate against local database and retrieve the unique user ID
            int userId = dbHelper.checkUser(user, pass);

            if (userId != -1) {
                Toast.makeText(MainActivity.this, "Welcome to The Wedding Path!", Toast.LENGTH_SHORT).show();

                // Route the authenticated session to the Dashboard, passing the unique ID payload
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);

                // Finish this activity to remove it from the back stack so the user cannot "back" into the login screen
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}