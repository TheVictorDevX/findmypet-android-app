package com.yuth.findmypetapplication.activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.api.RetrofitClient;
import com.yuth.findmypetapplication.api.UserApi;
import com.yuth.findmypetapplication.model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText emailField, passwordField;
    private UserApi userApi;
    public static final String SHARED_PREFS_NAME = "MyPrefs";
    public static final String USER_ID_KEY = "user_id_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize EditText fields and Retrofit service
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        userApi = RetrofitClient.getClient().create(UserApi.class);
    }

    // handle login button when clicked
    public void onLoginClicked(View view) {
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a User object for the login request body
        User loginUser = new User();
        loginUser.setEmail(email);
        loginUser.setPassword(password);

        Call<User> call = userApi.loginUser(loginUser);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    // Login successful, save user info
                    saveUserInfo(user);
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to DashboardActivity
                    Intent intent = new Intent(LoginActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish(); // close LoginActivity
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                    Log.e("LoginActivity", "Login failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Network error during login", t);
            }
        });
    }

    /**
     * Saves the logged-in user's information to SharedPreferences.
     *
     * @param user The User object containing the user's data.
     */
    private void saveUserInfo(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(USER_ID_KEY, user.getId());
        editor.apply(); // Use apply() for async save
    }
}