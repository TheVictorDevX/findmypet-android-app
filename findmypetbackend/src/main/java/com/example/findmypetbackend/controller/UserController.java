package com.example.findmypetbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.findmypetbackend.model.User;
import com.example.findmypetbackend.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // READ all users
    @GetMapping("/")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // READ a single user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // CREATE a new user with an image
    @PostMapping("/")
    public ResponseEntity<User> createUser(
            @RequestParam("profileImage") MultipartFile file,
            @RequestParam("displayName") String displayName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password) {

        Optional<User> newUser = userService.createUser(file, displayName, username, email, password);
        return newUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(500).build());
    }

    // UPDATE a user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestParam(value = "profileImage", required = false) MultipartFile file,
            @RequestParam(value = "displayName", required = false) String displayName,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password) {

        Optional<User> updatedUser = userService.updateUser(id, file, displayName, username, email, password);

        return updatedUser
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE a user
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (userService.deleteUser(id)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Login endpoint using email
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User loginUser) {
        return userService.validateLogin(loginUser.getEmail(), loginUser.getPassword())
                .map(user -> ResponseEntity.ok().body(user))
                .orElse(ResponseEntity.status(401).build());
    }
}