package com.yuth.findmypetapplication.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    private ImageView postImageView;
    private TextView postTitleTextView;
    private TextView postUserTextView;
    private TextView postDateTextView;
    private TextView postDescriptionTextView;
    private TextView postPhoneTextView;
    private Post currentPost;
    private long currentUserId;
    private Button editButton;
    private Button deleteButton; // Declare delete button

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cl_register), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        postImageView = findViewById(R.id.imageView);
        postTitleTextView = findViewById(R.id.textView5);
        postUserTextView = findViewById(R.id.textView6);
        postDateTextView = findViewById(R.id.textView7);
        postDescriptionTextView = findViewById(R.id.textView8);
        postPhoneTextView = findViewById(R.id.textView9);
        editButton = findViewById(R.id.button3);
        deleteButton = findViewById(R.id.button_delete); // Initialize the delete button

        // Get the Post object from the Intent
        Intent intent = getIntent();
        if (intent != null && intent.getSerializableExtra("post_object") != null) {
            currentPost = (Post) intent.getSerializableExtra("post_object");
            if (currentPost != null) {
                // Get the current user's ID
                currentUserId = getUserIdFromSharedPreferences();
                // Display the post details
                displayPostDetails(currentPost);
            }
        } else {
            Toast.makeText(this, "Failed to load post details.", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity if no post data is found
        }
    }

    private void displayPostDetails(Post post) {
        // Set the post title
        postTitleTextView.setText(post.getPostTitle());

        // Set the user's display name
        if (post.getUserDisplayName() != null) {
            postUserTextView.setText("by " + post.getUserDisplayName());
        } else {
            postUserTextView.setText("by Unknown User");
        }

        // Format and set the post date
        String formattedDate = formatDate(post.getCreateDate());
        postDateTextView.setText(formattedDate);

        // Set the description
        postDescriptionTextView.setText(post.getPostDescription());

        // Set the phone number
        postPhoneTextView.setText("Phone: " + post.getPhone());

        // Load image using Glide
        String imageUrl = post.getPostImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullImageUrl = RetrofitClient.BASE_URL + "images/" + imageUrl;
            Glide.with(this)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.shield_cat_solid_full)
                    .error(R.drawable.ic_launcher_background)
                    .into(postImageView);
        } else {
            // Set a default image if no URL is available
            postImageView.setImageResource(R.drawable.image_solid_full);
        }

        // Conditionally show/hide the Edit and Delete buttons
        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
            editButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            editButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
    }

    private long getUserIdFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        // The default value -1 indicates no user is logged in
        return sharedPreferences.getLong(LoginActivity.USER_ID_KEY, -1);
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Date not available";
        }

        // Use the new java.time API on API level 26 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Use ISO_LOCAL_DATE_TIME for a more general match
                DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);
                return outputFormatter.format(dateTime);
            } catch (Exception e) {
                // Log the exception for debugging
                Log.e("PostDetailActivity", "Error formatting date with java.time: " + dateString, e);
                return dateString;
            }
        } else {
            // Fallback to SimpleDateFormat for older Android versions
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException e) {
                // Log the exception for debugging
                Log.e("PostDetailActivity", "Error formatting date with SimpleDateFormat: " + dateString, e);
            }
            return dateString;
        }
    }

    public void onBackClicked(View view) {
        finish();
    }

    public void onEditClicked(View view) {
        if (currentPost != null) {
            Intent intent = new Intent(this, EditPostActivity.class);
            intent.putExtra("post_object", currentPost);
            startActivity(intent);
        } else {
            // Handle the case where the post object is null
            Toast.makeText(this, "Post data not available for editing.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCallClicked(View view) {
        if (currentPost != null && currentPost.getPhone() != null && !currentPost.getPhone().isEmpty()) {
            String phoneNumber = currentPost.getPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Phone number not available.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onDeleteClicked(View view) {
        if (currentPost != null) {
            PostApi postApi = RetrofitClient.getClient().create(PostApi.class);
            postApi.deletePost(currentPost.getId()).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(PostDetailActivity.this, "Post deleted successfully.", Toast.LENGTH_SHORT).show();
                        // Close the activity and return to the previous screen
                        finish();
                    } else {
                        Toast.makeText(PostDetailActivity.this, "Failed to delete post. Please try again.", Toast.LENGTH_SHORT).show();
                        Log.e("PostDetailActivity", "Error deleting post: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(PostDetailActivity.this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                    Log.e("PostDetailActivity", "Network error deleting post", t);
                }
            });
        }
    }
}