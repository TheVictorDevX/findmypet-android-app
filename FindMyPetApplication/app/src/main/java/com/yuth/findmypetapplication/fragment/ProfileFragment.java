package com.yuth.findmypetapplication.fragment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.activity.LoginActivity;
import com.yuth.findmypetapplication.activity.ViewAllPostsActivity;
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

public class ProfileFragment extends Fragment {

    private EditText displayNameField;
    private EditText usernameField;
    private EditText emailField;
    private EditText passwordField;
    private ImageView profileImageView;
    private Button viewAllPostsButton;
    private Button saveButton;
    private Button logoutButton;
    private Button pickImageButton;
    private UserApi userApi;
    private Uri profileImageUri;

    // Launcher for picking an image from the device
    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    profileImageUri = uri; // Save the URI
                    Glide.with(this)
                            .load(profileImageUri)
                            .circleCrop()
                            .into(profileImageView);
                }
            });

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userApi = RetrofitClient.getClient().create(UserApi.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.iv_logo);
        displayNameField = view.findViewById(R.id.et_displayName);
        usernameField = view.findViewById(R.id.editTextText4);
        emailField = view.findViewById(R.id.emailField);
        passwordField = view.findViewById(R.id.passwordField);
        viewAllPostsButton = view.findViewById(R.id.profileViewAllPostsButton);
        saveButton = view.findViewById(R.id.profileSaveButton);
        logoutButton = view.findViewById(R.id.profileLogoutButton);
        pickImageButton = view.findViewById(R.id.button);

        pickImageButton.setOnClickListener(v -> mGetContent.launch("image/*"));

        viewAllPostsButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
            Long currentUserId = sharedPreferences.getLong(LoginActivity.USER_ID_KEY, -1L);

            Intent intent = new Intent(getContext(), ViewAllPostsActivity.class);
            // Pass the user ID as an extra
            intent.putExtra("user_id", currentUserId);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            clearUserInfo();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        saveButton.setOnClickListener(v -> updateUserProfile());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchUserProfile();
    }

    private void fetchUserProfile() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        Long currentUserId = sharedPreferences.getLong(LoginActivity.USER_ID_KEY, -1L);

        if (currentUserId != -1L) {
            Call<User> call = userApi.getUserById(currentUserId);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        User user = response.body();
                        displayUserInfo(user);
                    } else {
                        Toast.makeText(getContext(), "Failed to fetch user data. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileFragment", "Failed to fetch user profile: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }
                    Log.e("ProfileFragment", "Network error: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Network error. Please check your connection.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    private void displayUserInfo(User user) {
        displayNameField.setText(user.getDisplayName());
        usernameField.setText(user.getUsername());
        emailField.setText(user.getEmail());
        passwordField.setText(user.getPassword());

        String imageUrl = user.getProfileImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullImageUrl = RetrofitClient.BASE_URL + "images/" + imageUrl;
            Log.d("ProfileFragment", "Loading image from URL: " + fullImageUrl);

            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.shield_cat_solid_full)
                    .error(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.shield_cat_solid_full);
            Toast.makeText(getContext(), "No profile picture found.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfile() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        Long currentUserId = sharedPreferences.getLong(LoginActivity.USER_ID_KEY, -1L);

        if (currentUserId == -1L) {
            Toast.makeText(getContext(), "User ID not found. Please log in again.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            RequestBody displayNamePart = RequestBody.create(MultipartBody.FORM, displayNameField.getText().toString());
            RequestBody usernamePart = RequestBody.create(MultipartBody.FORM, usernameField.getText().toString());
            RequestBody emailPart = RequestBody.create(MultipartBody.FORM, emailField.getText().toString());

            // Handle password update
            RequestBody passwordPart = null;
            String newPassword = passwordField.getText().toString();
            if (!newPassword.isEmpty()) {
                passwordPart = RequestBody.create(MultipartBody.FORM, newPassword);
            }

            MultipartBody.Part imagePart = null;
            if (profileImageUri != null) {
                imagePart = prepareFilePart("profileImage", profileImageUri);
            }

            Call<User> call = userApi.updateUser(currentUserId, imagePart, displayNamePart, usernamePart, emailPart, passwordPart);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        // Clear the password field after a successful update for security
//                        passwordField.setText("");
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e("ProfileFragment", "Update failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("ProfileFragment", "Network error during profile update", t);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error preparing image for upload.", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearUserInfo() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LoginActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LoginActivity.USER_ID_KEY);
        editor.apply();
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) throws IOException {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        String mimeType = contentResolver.getType(fileUri);
        String originalFilename = getFileNameFromUri(fileUri);
        File tempFile = File.createTempFile("upload", ".tmp", requireActivity().getCacheDir());
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
        return MultipartBody.Part.createFormData(partName, originalFilename, requestFile);
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null)) {
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