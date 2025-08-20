package com.yuth.findmypetapplication.adapter;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.api.RetrofitClient;
import com.yuth.findmypetapplication.model.Post;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private OnItemClickListener listener;
    private List<Post> postList;
    private List<Post> allPosts; // New list to hold all posts

    private Context context;


    public PostAdapter(List<Post> postList, android.content.Context context) { // Add context here
        this.postList = postList;
        this.allPosts = new ArrayList<>(postList);
        this.context = context; // Initialize the context
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        public TextView postTitle;
        public TextView postDescription;
        public TextView postUser;
        public TextView postDate;
        public ImageView postImage;

        public PostViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.postTitleTextView);
            postDescription = itemView.findViewById(R.id.postDescriptionTextView);
            postUser = itemView.findViewById(R.id.postUserTextView);
            postDate = itemView.findViewById(R.id.postDateTextView);
            postImage = itemView.findViewById(R.id.postImageView);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new PostViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.postTitle.setText(post.getPostTitle());
        holder.postDescription.setText(post.getPostDescription());

        // Set user's display name from the post model
        if (post.getUserDisplayName() != null) {
            holder.postUser.setText("Posted by " + post.getUserDisplayName());
        } else {
            holder.postUser.setText("Posted by Unknown");
        }

        // Format and display the creation date
        String formattedDate = formatDate(post.getCreateDate());
        holder.postDate.setText(formattedDate);

        // Load image using Glide
        String imageUrl = post.getPostImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullImageUrl = RetrofitClient.BASE_URL + "images/" + imageUrl;

            Glide.with(holder.itemView.getContext())
                    .load(fullImageUrl)
                    .placeholder(R.drawable.shield_cat_solid_full)
                    .error(R.drawable.ic_launcher_background)
                    .into(holder.postImage);
        } else {
            // Set a default image if no URL is available
            holder.postImage.setImageResource(R.drawable.image_solid_full);
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Filters the list of posts based on a search query.
     * @param query The search string.
     */
    public void filter(String query) {
        postList.clear();
        if (query.isEmpty()) {
            postList.addAll(allPosts);
        } else {
            query = query.toLowerCase(Locale.getDefault());
            for (Post post : allPosts) {
                if (post.getPostTitle().toLowerCase(Locale.getDefault()).contains(query) ||
                        post.getPostDescription().toLowerCase(Locale.getDefault()).contains(query)) {
                    postList.add(post);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Updates the adapter's data set and resets the filter.
     * @param newPosts The new list of posts to display.
     */
    public void setPosts(List<Post> newPosts) {
        allPosts.clear();
        allPosts.addAll(newPosts);
        filter(""); // Apply the filter to reset the displayed list
    }

    // Helper method to format the date string
    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Date not available";
        }

        // Use the new java.time API on API level 26 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDateTime dateTime = LocalDateTime.parse(dateString, inputFormatter);
                return outputFormatter.format(dateTime);
            } catch (Exception e) {
                Log.e("PostAdapter", "Error formatting date with java.time: " + dateString, e);
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
                Log.e("PostAdapter", "Error formatting date with SimpleDateFormat: " + dateString, e);
            }
            return dateString;
        }
    }
}