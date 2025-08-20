package com.example.findmypetbackend.controller;

import com.example.findmypetbackend.model.Post;
import com.example.findmypetbackend.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/")
    public ResponseEntity<Post> createPost(
        @RequestParam("postImage") MultipartFile file,
        @RequestParam("postTitle") String postTitle,
        @RequestParam("postDescription") String postDescription,
        @RequestParam("phone") String phone,
        @RequestParam("userId") Long userId) {

        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post image file is required.");
        }
        Post newPost = postService.createPost(file, postTitle, postDescription, phone, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPost);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        Optional<Post> post = postService.getPostById(id);
        return post.map(ResponseEntity::ok)
                   .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with ID: " + id));
    }

    @GetMapping("/")
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        if (posts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(posts);
    }

    // New endpoint to get posts by a user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getPostsByUserId(@PathVariable Long userId) {
        List<Post> posts = postService.getPostsByUserId(userId);
        if (posts.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(posts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        if (!postService.deletePost(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with ID: " + id);
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(
        @PathVariable Long id,
        @RequestParam(value = "postImage", required = false) MultipartFile file,
        @RequestParam(value = "postTitle", required = false) String postTitle,
        @RequestParam(value = "postDescription", required = false) String postDescription,
        @RequestParam(value = "phone", required = false) String phone) {
        
        Optional<Post> updatedPost = postService.updatePost(id, file, postTitle, postDescription, phone);

        return updatedPost.map(ResponseEntity::ok)
                          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with ID: " + id));
    }
}