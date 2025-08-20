package com.example.findmypetbackend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.findmypetbackend.model.User;
import com.example.findmypetbackend.repository.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // READ all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // READ a single user by ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // CREATE a new user with an image file
    public Optional<User> createUser(MultipartFile file, String displayName, String username, String email, String password) {
        try {
            String filePath = fileStorageService.storeFile(file);

            User newUser = new User();
            newUser.setDisplayName(displayName);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setPassword(password); // No encoding
            newUser.setProfileImageUrl(filePath);

            return Optional.of(userRepository.save(newUser));
        } catch (RuntimeException e) {
            System.err.println("Failed to create user due to file storage error: " + e.getMessage());
            return Optional.empty();
        }
    }

    // UPDATE an existing user
    public Optional<User> updateUser(Long id, MultipartFile file, String displayName, String username, String email, String password) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            // Handle image file update
            if (file != null && !file.isEmpty()) {
                // Delete old image file
                String oldFilename = existingUser.getProfileImageUrl();
                if (oldFilename != null) {
                    try {
                        Path oldFilePath = Paths.get("uploads").resolve(oldFilename).normalize();
                        Files.deleteIfExists(oldFilePath);
                    } catch (IOException e) {
                        System.err.println("Could not delete old file: " + oldFilename);
                        e.printStackTrace();
                    }
                }
                // Store new image file
                String newFilename = fileStorageService.storeFile(file);
                existingUser.setProfileImageUrl(newFilename);
            }

            // Update other user details based on what's provided in the request
            if (displayName != null) {
                existingUser.setDisplayName(displayName);
            }
            if (username != null) {
                existingUser.setUsername(username);
            }
            if (email != null) {
                existingUser.setEmail(email);
            }
            if (password != null) {
                existingUser.setPassword(password); // No encoding
            }

            return Optional.of(userRepository.save(existingUser));
        } else {
            return Optional.empty();
        }
    }

    // DELETE a user and their image
    public boolean deleteUser(Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    // Get the image file path
                    String filename = user.getProfileImageUrl();
                    if (filename != null) {
                        Path filePath = Paths.get("uploads").resolve(filename).normalize();
                        try {
                            // Delete the image file
                            Files.deleteIfExists(filePath);
                        } catch (IOException e) {
                            System.err.println("Could not delete file: " + filePath);
                            e.printStackTrace();
                        }
                    }
                    // Delete the user from the database
                    userRepository.delete(user);
                    return true;
                }).orElse(false);
    }

    // Find a user by email
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Validate user credentials for login using email
    public Optional<User> validateLogin(String email, String password) {
        Optional<User> userOptional = findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(password)) { // Plain text password comparison
                return userOptional;
            }
        }
        return Optional.empty();
    }
}