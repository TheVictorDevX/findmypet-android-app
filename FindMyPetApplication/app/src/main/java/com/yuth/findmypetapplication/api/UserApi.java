package com.yuth.findmypetapplication.api;

import com.yuth.findmypetapplication.model.User;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserApi {

    @GET("users/") // Corrected path to match backend
    Call<List<User>> getAllUsers();

    @GET("users/{id}")
    Call<User> getUserById(@Path("id") Long id);

    @Multipart
    @POST("users/") // Corrected path to match backend
    Call<User> createUser(
            @Part MultipartBody.Part profileImage,
            @Part("displayName") RequestBody displayName,
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password
    );

    @Multipart
    @PUT("users/{id}")
    Call<User> updateUser(
            @Path("id") Long id,
            @Part MultipartBody.Part profileImage,
            @Part("displayName") RequestBody displayName,
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("password") RequestBody password
    );

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") Long id);

    // Login endpoint
    @POST("users/login")
    Call<User> loginUser(@Body User loginUser);
}