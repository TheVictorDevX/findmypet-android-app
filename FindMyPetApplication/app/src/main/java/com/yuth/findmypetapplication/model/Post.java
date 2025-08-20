package com.yuth.findmypetapplication.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Post implements Serializable {

    @SerializedName("id")
    private Long id;

    @SerializedName("postImageUrl")
    private String postImageUrl;

    @SerializedName("postTitle")
    private String postTitle;

    @SerializedName("postDescription")
    private String postDescription;

    @SerializedName("phone")
    private String phone;

    @SerializedName("userId")
    private Long userId;

    @SerializedName("createDate")
    private String createDate;

    @SerializedName("updateDate")
    private String updateDate;

    private String userDisplayName;

    // A no-argument constructor is required for Gson to work properly.
    public Post() {}

    public Post(String postImageUrl, String postTitle, String postDescription, String phone, Long userId) {
        this.postImageUrl = postImageUrl;
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.phone = phone;
        this.userId = userId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPostImageUrl() {
        return postImageUrl;
    }

    public void setPostImageUrl(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }
}