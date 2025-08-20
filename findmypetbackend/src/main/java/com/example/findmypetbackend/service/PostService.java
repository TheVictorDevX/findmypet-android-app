package com.example.findmypetbackend.service;

import com.example.findmypetbackend.model.Post;
import com.example.findmypetbackend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private FileStorageService fileStorageService;

    public Post createPost(MultipartFile file, String postTitle, String postDescription, String phone, Long userId) {
        String filePath = fileStorageService.storeFile(file);

        Post newPost = new Post();
        newPost.setPostImageUrl(filePath);
        newPost.setPostTitle(postTitle);
        newPost.setPostDescription(postDescription);
        newPost.setPhone(phone);
        newPost.setUserId(userId);

        return postRepository.save(newPost);
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Transactional
    public boolean deletePost(Long id) {
        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isPresent()) {
            Post post = postOptional.get();

            // Delete the image file
            if (post.getPostImageUrl() != null && !post.getPostImageUrl().isEmpty()) {
                fileStorageService.deleteFile(post.getPostImageUrl());
            }

            // Delete the post from the database
            postRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<Post> updatePost(Long id, MultipartFile file, String postTitle, String postDescription, String phone) {
        Optional<Post> postOptional = postRepository.findById(id);

        if (postOptional.isPresent()) {
            Post existingPost = postOptional.get();

            if (file != null && !file.isEmpty()) {
                // Delete old image and store new one
                if (existingPost.getPostImageUrl() != null) {
                    fileStorageService.deleteFile(existingPost.getPostImageUrl());
                }
                String newFilename = fileStorageService.storeFile(file);
                existingPost.setPostImageUrl(newFilename);
            }

            if (postTitle != null) {
                existingPost.setPostTitle(postTitle);
            }
            if (postDescription != null) {
                existingPost.setPostDescription(postDescription);
            }
            if (phone != null) {
                existingPost.setPhone(phone);
            }

            return Optional.of(postRepository.save(existingPost));
        } else {
            return Optional.empty();
        }
    }

    public List<Post> getPostsByUserId(Long userId) {
    return postRepository.findByUserId(userId);
}
}