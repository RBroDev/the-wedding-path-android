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
        Button btnRegister = findViewById(R.id.button2);
        Button btnLogin = findViewById(R.id.button);

        // Handle new user registration
        btnRegister.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both a username and password.", Toast.LENGTH_SHORT).show();
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

        // Handle existing user authentication
        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both username and password.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.checkUser(user, pass)) {
                Toast.makeText(MainActivity.this, "Welcome to The Wedding Path!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);

                // Terminate activity to prevent back-navigation to the authentication flow
                finish();
            } else {
                Toast.makeText(MainActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }
}