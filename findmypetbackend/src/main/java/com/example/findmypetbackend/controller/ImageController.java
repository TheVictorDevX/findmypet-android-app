package com.example.findmypetbackend.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/images") // New base path for image-related endpoints
public class ImageController {

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path file = Paths.get("uploads").resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                // Determine the correct content type based on the file extension
                String fileExtension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
                MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;

                switch (fileExtension) {
                    case "png":
                        contentType = MediaType.IMAGE_PNG;
                        break;
                    case "jpg":
                    case "jpeg":
                        contentType = MediaType.IMAGE_JPEG;
                        break;
                    case "gif":
                        contentType = MediaType.IMAGE_GIF;
                        break;
                    // Add more cases for other image types as needed
                }

                return ResponseEntity.ok()
                        .contentType(contentType)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }
}