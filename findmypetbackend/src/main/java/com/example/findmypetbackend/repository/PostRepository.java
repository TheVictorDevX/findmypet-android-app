package com.example.findmypetbackend.repository;

import com.example.findmypetbackend.model.Post;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // This method will automatically find posts by the 'userId' field
    List<Post> findByUserId(Long userId);
}
