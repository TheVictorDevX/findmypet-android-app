package com.yuth.findmypetapplication.api;

import com.yuth.findmypetapplication.model.Post;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PostApi {

    @Multipart
    @POST("posts/")
    Call<Post> createPost(
            @Part MultipartBody.Part file,
            @Part("postTitle") RequestBody postTitle,
            @Part("postDescription") RequestBody postDescription,
            @Part("phone") RequestBody phone,
            @Part("userId") RequestBody userId
    );

    @GET("posts/{id}")
    Call<Post> getPostById(@Path("id") Long id);

    @GET("posts/")
    Call<List<Post>> getAllPosts();

    // New method to get posts by a specific user ID
    @GET("posts/user/{userId}")
    Call<List<Post>> getPostsByUserId(@Path("userId") Long userId);

    @DELETE("posts/{id}")
    Call<Void> deletePost(@Path("id") Long id);

    @Multipart
    @PUT("posts/{id}")
    Call<Post> updatePost(
            @Path("id") Long id,
            @Part MultipartBody.Part file,
            @Part("postTitle") RequestBody postTitle,
            @Part("postDescription") RequestBody postDescription,
            @Part("phone") RequestBody phone);
}