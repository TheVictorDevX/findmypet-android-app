package com.yuth.findmypetapplication.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.api.PostApi;
import com.yuth.findmypetapplication.api.RetrofitClient;
import com.yuth.findmypetapplication.model.Post;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    private ImageView postImageView;
    private EditText postTitleEditText;
    private EditText postDescriptionEditText;
    private EditText postPhoneEditText;
    private Post currentPost;
    private PostApi postApi;
    private Uri selectedImageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views and Retrofit service
        postImageView = findViewById(R.id.iv_postImage);
        postTitleEditText = findViewById(R.id.et_postTitle);
        postDescriptionEditText = findViewById(R.id.et_postDescription);
        postPhoneEditText = findViewById(R.id.et_phoneNumber);
        postApi = RetrofitClient.getClient().create(PostApi.class);

        // Get the Post object from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("post_object") != null) {
            currentPost = (Post) intent.getSerializableExtra("post_object");
            if (currentPost != null) {
                displayPostDetails(currentPost);
            }
        } else {
            Toast.makeText(this, "Failed to load post data for editing.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Set click listeners for buttons
        Button pickImageButton = findViewById(R.id.btn_pickImage);
        pickImageButton.setOnClickListener(v -> pickImage());

        Button updateButton = findViewById(R.id.btn_upload);
        updateButton.setOnClickListener(v -> onUpdateClicked());
    }

    private void displayPostDetails(Post post) {
        // Pre-fill the EditText fields
        postTitleEditText.setText(post.getPostTitle());
        postDescriptionEditText.setText(post.getPostDescription());
        postPhoneEditText.setText(post.getPhone());

        // Load the existing image using Glide
        String imageUrl = post.getPostImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullImageUrl = RetrofitClient.BASE_URL + "images/" + imageUrl;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.shield_cat_solid_full)
                    .error(R.drawable.ic_launcher_background)
                    .into(postImageView);
        } else {
            postImageView.setImageResource(R.drawable.image_solid_full);
        }
    }

    // Method to handle the "Update Post" button click
    private void onUpdateClicked() {
        String title = postTitleEditText.getText().toString().trim();
        String description = postDescriptionEditText.getText().toString().trim();
        String phone = postPhoneEditText.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        MultipartBody.Part imagePart = null;
        if (selectedImageUri != null) {
            File imageFile = getFileFromUri(selectedImageUri);
            RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), imageFile);
            imagePart = MultipartBody.Part.createFormData("postImage", imageFile.getName(), requestFile);
        }

        // Convert text fields to RequestBody
        RequestBody titlePart = RequestBody.create(MediaType.parse("text/plain"), title);
        RequestBody descriptionPart = RequestBody.create(MediaType.parse("text/plain"), description);
        RequestBody phonePart = RequestBody.create(MediaType.parse("text/plain"), phone);

        Call<Post> call = postApi.updatePost(currentPost.getId(), imagePart, titlePart, descriptionPart, phonePart);
        call.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditPostActivity.this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the previous screen
                } else {
                    Toast.makeText(EditPostActivity.this, "Failed to update post.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Toast.makeText(EditPostActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            postImageView.setImageURI(selectedImageUri);
        }
    }

    private File getFileFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            File tempFile = new File(getCacheDir(), "temp_image_file");
            OutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            inputStream.close();
            outputStream.close();
            return tempFile;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void onCancelClicked(View view) {
        finish();
    }
}