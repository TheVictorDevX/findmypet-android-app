package com.yuth.findmypetapplication.activity;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.provider.OpenableColumns;

import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.api.RetrofitClient;
import com.yuth.findmypetapplication.api.UserApi;
import com.yuth.findmypetapplication.model.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ImageView ivProfileImage;
    private Button btnPickImage;
    private Button btn_register;

    EditText et_displayName;
    EditText et_username;
    EditText et_email;
    EditText et_password;
    EditText et_confirmPassword; // Added for confirm password

    private UserApi userApi;
    private Uri profileImageUri;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageUri = uri;
                    ivProfileImage.setImageURI(profileImageUri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivProfileImage = findViewById(R.id.iv_logo);
        btnPickImage = findViewById(R.id.btn_pickProfileImage);
        btn_register=findViewById(R.id.btn_register);

        btnPickImage.setOnClickListener(v -> mGetContent.launch("image/*"));

        et_displayName = findViewById(R.id.et_displayName);
        et_username = findViewById(R.id.et_username);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_confirmPassword = findViewById(R.id.et_confirmPassword); // Initialize the new EditText

        userApi = RetrofitClient.getClient().create(UserApi.class);

        btn_register.setOnClickListener(v -> {
            // Validate all inputs before proceeding
            if (!validateInputs()) {
                return;
            }

            try {
                // Create RequestBody for each text field
                RequestBody displayName = createPartFromString(et_displayName.getText().toString());
                RequestBody username = createPartFromString(et_username.getText().toString());
                RequestBody email = createPartFromString(et_email.getText().toString());
                RequestBody password = createPartFromString(et_password.getText().toString());

                // Create MultipartBody.Part for the image
                MultipartBody.Part profileImage = prepareFilePart("profileImage", profileImageUri);

                createUser(profileImage, displayName, username, email, password);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(RegisterActivity.this, "Error preparing image file.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Validates user inputs before making the API call.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean validateInputs() {
        String displayName = et_displayName.getText().toString().trim();
        String username = et_username.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String password = et_password.getText().toString().trim();
        String confirmPassword = et_confirmPassword.getText().toString().trim(); // Get the confirm password

        if (displayName.isEmpty()) {
            Toast.makeText(this, "Display name cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.isEmpty()) {
            Toast.makeText(this, "Username cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Simple email validation (can be more complex with a regex)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Password length check
        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if an image has been selected
        if (profileImageUri == null) {
            Toast.makeText(this, "Please select a profile image.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createUser(MultipartBody.Part profileImage, RequestBody displayName, RequestBody username, RequestBody email, RequestBody password) {
        Call<User> call = userApi.createUser(profileImage, displayName, username, email, password);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User createdUser = response.body();
                    String message = "User created successfully! ID: " + createdUser.getId();
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String message = "Error creating user: " + response.code();
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    Log.e("RegisterActivity", "Response Error: " + response.errorBody().toString());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                String message = "Network error: " + t.getMessage();
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                Log.e("RegisterActivity", "Network Error: ", t);
            }
        });
    }

    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) throws IOException {
        ContentResolver contentResolver = getContentResolver();
        String mimeType = contentResolver.getType(fileUri);
        String originalFilename = getFileNameFromUri(fileUri);

        // Create a temporary file to store the image data
        File tempFile = File.createTempFile("upload", ".tmp", getCacheDir());
        try (InputStream inputStream = contentResolver.openInputStream(fileUri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream != null) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        }

        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), tempFile);
        return MultipartBody.Part.createFormData(partName, originalFilename, requestFile); // Use the original filename
    }

    /**
     * Helper method to get the original filename from a content URI.
     */
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
}