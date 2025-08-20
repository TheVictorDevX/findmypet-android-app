package com.yuth.findmypetapplication.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yuth.findmypetapplication.R;
import com.yuth.findmypetapplication.activity.PostDetailActivity;
import com.yuth.findmypetapplication.activity.ViewAllPostsActivity;
import com.yuth.findmypetapplication.adapter.PostAdapter;
import com.yuth.findmypetapplication.api.PostApi;
import com.yuth.findmypetapplication.api.RetrofitClient;
import com.yuth.findmypetapplication.api.UserApi;
import com.yuth.findmypetapplication.model.Post;
import com.yuth.findmypetapplication.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CardListFragment extends Fragment {
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private List<Post> postList;
    private PostApi postApi;
    private UserApi userApi;
    private TextView emptyTextView; // Add this line

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public CardListFragment() {
        // Required empty public constructor
    }

    public static CardListFragment newInstance(String param1, String param2) {
        CardListFragment fragment = new CardListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        postApi = RetrofitClient.getClient().create(PostApi.class);
        userApi = RetrofitClient.getClient().create(UserApi.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_list, container, false);

        recyclerView = view.findViewById(R.id.cardListrecyclerView);
        emptyTextView = view.findViewById(R.id.emptyTextView); // Add this line

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        // Now passing the context to the adapter
        adapter = new PostAdapter(postList, getContext());

        adapter.setOnItemClickListener(position -> {
            Post selectedPost = postList.get(position);
            Intent intent = new Intent(requireContext(), PostDetailActivity.class);
            intent.putExtra("post_object", selectedPost);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get the user ID from the hosting activity's intent
        Intent intent = requireActivity().getIntent();
        long userId = intent.getLongExtra("user_id", -1L);

        fetchPostsFromBackend(userId); // Call the new method with the user ID
    }

    public PostAdapter getPostAdapter() {
        return adapter;
    }

    // The method now takes an optional userId
    private void fetchPostsFromBackend(long userId) {
        Call<List<Post>> call;
        if (userId != -1L) {
            // Fetch posts by a specific user
            call = postApi.getPostsByUserId(userId);
            Log.d("CardListFragment", "Fetching posts for user ID: " + userId);
        } else {
            // Fetch all posts
            call = postApi.getAllPosts();
            Log.d("CardListFragment", "Fetching all posts");
        }

        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(@NonNull Call<List<Post>> call, @NonNull Response<List<Post>> response) {
                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> fetchedPosts = response.body();
                    if (fetchedPosts.isEmpty()) {
                        showEmptyView(userId);
                    } else {
                        postList.clear();
                        postList.addAll(fetchedPosts);
                        fetchUserDisplayNames(postList);
                        showRecyclerView();
                    }
                } else {
                    Log.e("CardListFragment", "Failed to fetch posts: " + response.message());
                    Toast.makeText(getContext(), "Failed to fetch posts.", Toast.LENGTH_SHORT).show();
                    showEmptyView(userId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Post>> call, @NonNull Throwable t) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                Log.e("CardListFragment", "Error fetching posts", t);
                Toast.makeText(getContext(), "Network error.", Toast.LENGTH_SHORT).show();
                showEmptyView(userId);
            }
        });
    }

    private void fetchUserDisplayNames(List<Post> posts) {
        if (posts.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        final AtomicInteger postsFetchedCount = new AtomicInteger(0);

        for (final Post post : posts) {
            if (post.getUserId() != null) {
                userApi.getUserById(post.getUserId()).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            post.setUserDisplayName(response.body().getDisplayName());
                        } else {
                            post.setUserDisplayName("Unknown User");
                        }
                        checkIfAllUsersFetched(postsFetchedCount, posts.size());
                    }

                    @Override
                    public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                        post.setUserDisplayName("Unknown User");
                        Log.e("CardListFragment", "Error fetching user for post ID: " + post.getUserId(), t);
                        checkIfAllUsersFetched(postsFetchedCount, posts.size());
                    }
                });
            } else {
                post.setUserDisplayName("Unknown User");
                checkIfAllUsersFetched(postsFetchedCount, posts.size());
            }
        }
    }

    private void checkIfAllUsersFetched(AtomicInteger count, int total) {
        if (count.incrementAndGet() == total) {
            if (isAdded() && getContext() != null) {
                requireActivity().runOnUiThread(() -> adapter.setPosts(postList));
            }
        }
    }

    // Helper methods to manage view visibility
    private void showRecyclerView() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    private void showEmptyView(long userId) {
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        if (userId != -1L) {
            emptyTextView.setText("You have no posts yet.");
        } else {
            emptyTextView.setText("No posts found.");
        }
    }
}