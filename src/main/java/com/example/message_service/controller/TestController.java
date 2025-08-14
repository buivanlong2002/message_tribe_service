package com.example.message_service.controller;

import com.example.message_service.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @PostMapping("/upload-test")
    public ApiResponse<String> testUpload(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Test upload - file: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());
            
            // Test tạo thư mục
            String uploadDir = "uploads";
            Path uploadPath = Paths.get(uploadDir);
            System.out.println("Upload path: " + uploadPath.toAbsolutePath());
            
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating upload directory...");
                Files.createDirectories(uploadPath);
                System.out.println("Upload directory created successfully");
            }
            
            // Test write permission
            Path testFile = uploadPath.resolve("test.txt");
            Files.writeString(testFile, "test");
            Files.delete(testFile);
            System.out.println("Write permission test passed");
            
            // Test upload file thật
            String uniqueFileName = UUID.randomUUID().toString() + ".jpg";
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File uploaded successfully to: " + filePath.toAbsolutePath());
            
            return ApiResponse.success("00", "Test upload thành công", "/uploads/" + uniqueFileName);
            
        } catch (Exception e) {
            System.err.println("Test upload error: " + e.getMessage());
            e.printStackTrace();
            return ApiResponse.error("01", "Test upload thất bại: " + e.getMessage());
        }
    }
    
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("00", "Service is running", "OK");
    }
}

