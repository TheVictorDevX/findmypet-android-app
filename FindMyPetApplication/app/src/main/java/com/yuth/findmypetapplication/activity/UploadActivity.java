package com.yuth.findmypetapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns; // Add this import
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.api.PostApi;
import com.yuth.findmypetapplication.api.RetrofitClient;
import com.yuth.findmypetapplication.model.Post;

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

public class UploadActivity extends AppCompatActivity {

    private ImageView postImageView;
    private Button pickImageButton;
    private Button uploadButton;
    private EditText postTitleEditText;
    private EditText postDescriptionEditText;
    private EditText phoneNumberEditText;

    private Uri selectedImageUri;

    // User ID variable
    private Long currentUserId;

    // Use the new Activity Result API for picking an image
    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    postImageView.setImageURI(selectedImageUri);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sv_upload), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        postImageView = findViewById(R.id.iv_postImage);
        pickImageButton = findViewById(R.id.btn_pickImage);
        uploadButton = findViewById(R.id.btn_upload);
        postTitleEditText = findViewById(R.id.et_postTitle);
        postDescriptionEditText = findViewById(R.id.et_postDescription);
        phoneNumberEditText = findViewById(R.id.et_phoneNumber);

        // Retrieve user ID from SharedPreferences
        currentUserId = getUserIdFromSharedPreferences();
        if (currentUserId == -1L) {
            Toast.makeText(this, "User not logged in. Please log in first.", Toast.LENGTH_LONG).show();
            // Optional: Redirect to LoginActivity if no user ID is found
            // Intent intent = new Intent(this, LoginActivity.class);
            // startActivity(intent);
            // finish();
        }

        // Set click listeners
        pickImageButton.setOnClickListener(v -> openImagePicker());
        uploadButton.setOnClickListener(v -> uploadPost());
    }

    public void onCancelClicked(View view) {
        finish();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    // Inside your `UploadActivity.java` file
    public void uploadPost() {
        // Check if an image has been selected
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please select an image.", Toast.LENGTH_SHORT).show();
            return;
        }

        // You must have a way to get the user ID, for example from SharedPreferences.
        Long currentUserId = getUserIdFromSharedPreferences();
        if (currentUserId == null || currentUserId == -1L) {
            Toast.makeText(this, "Error: User ID not found. Please log in again.", Toast.LENGTH_LONG).show();
            return;
        }

        String postTitle = postTitleEditText.getText().toString().trim();
        String postDescription = postDescriptionEditText.getText().toString().trim();
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        if (postTitle.isEmpty() || postDescription.isEmpty() || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Create a temporary file from the selected URI
        File imageFile = null;
        try {
            imageFile = getFileFromUri(selectedImageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare image for upload.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 2: Get the original filename with its correct extension
        String originalFilename = getFileNameFromUri(selectedImageUri);

        // Step 3: Prepare multipart form data
        // Use the correct MIME type for the file
        RequestBody postImagePart = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), imageFile);
        // Use `createFormData` to include the file with its original filename
        MultipartBody.Part filePart = MultipartBody.Part.createFormData("postImage", originalFilename, postImagePart);

        // Create a Retrofit service instance
        PostApi postApi = RetrofitClient.getClient().create(PostApi.class);

        // Step 4: Create RequestBody for other form data
        RequestBody postTitlePart = RequestBody.create(MultipartBody.FORM, postTitle);
        RequestBody postDescriptionPart = RequestBody.create(MultipartBody.FORM, postDescription);
        RequestBody phonePart = RequestBody.create(MultipartBody.FORM, phoneNumber);
        RequestBody userIdPart = RequestBody.create(MultipartBody.FORM, String.valueOf(currentUserId));

        // Step 5: Make the API call
        Call<Post> call = postApi.createPost(filePart, postTitlePart, postDescriptionPart, phonePart, userIdPart);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UploadActivity.this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity on success
                } else {
                    Toast.makeText(UploadActivity.this, "Failed to upload post.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(UploadActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to create a temporary file from a URI
    private File getFileFromUri(Uri uri) throws IOException {
        File tempFile = File.createTempFile("upload_image", ".tmp", getCacheDir());
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            if (inputStream == null) {
                throw new IOException("Unable to open InputStream for URI: " + uri);
            }
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        return tempFile;
    }

    /**
     * Retrieves the user ID from SharedPreferences.
     *
     * @return The logged-in user's ID, or -1L if not found.
     */
    private Long getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        // The default value -1L is used to indicate that the key was not found.
        return sharedPreferences.getLong(LoginActivity.USER_ID_KEY, -1L);
    }

    // **ADD THIS METHOD TO YOUR UploadActivity CLASS**
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