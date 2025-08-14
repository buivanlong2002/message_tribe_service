package com.example.message_service.service.util;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "uploads"; // Thư mục uploads trong root project

    public FileStorageService() {
        // Test tạo thư mục khi khởi tạo service
        try {
            Path uploadPath = Paths.get(uploadDir);
            System.out.println("FileStorageService constructor - Testing upload directory: " + uploadPath.toAbsolutePath());
            
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating upload directory in constructor...");
                Files.createDirectories(uploadPath);
                System.out.println("Upload directory created successfully");
            } else {
                System.out.println("Upload directory already exists");
            }
            
            // Test write permission
            Path testFile = uploadPath.resolve("test.txt");
            Files.writeString(testFile, "test");
            Files.delete(testFile);
            System.out.println("Write permission test passed");
            
        } catch (Exception e) {
            System.err.println("Error in FileStorageService constructor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String uploadFile(MultipartFile file) throws IOException {
        try {
            System.out.println("FileStorageService.uploadFile - Starting upload for: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize() + " bytes");
            System.out.println("Content type: " + file.getContentType());
            
            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir);
            System.out.println("Upload path: " + uploadPath.toAbsolutePath());
            
            if (!Files.exists(uploadPath)) {
                System.out.println("Creating upload directory...");
                Files.createDirectories(uploadPath);
            }

            // Tạo fileName ngẫu nhiên để tránh trùng
            String originalFileName = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFileName);
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            
            System.out.println("Original filename: " + originalFileName);
            System.out.println("File extension: " + fileExtension);
            System.out.println("Unique filename: " + uniqueFileName);

            // Lưu file vào thư mục uploads
            Path filePath = uploadPath.resolve(uniqueFileName);
            System.out.println("Full file path: " + filePath.toAbsolutePath());
            
            Files.copy(file.getInputStream(), filePath);
            System.out.println("File copied successfully");

            // Trả về URL hoặc đường dẫn file (ở đây đơn giản trả path tương đối)
            String result = "/uploads/" + uniqueFileName;
            System.out.println("Returning URL: " + result);
            return result;
            
        } catch (Exception e) {
            System.err.println("Error in FileStorageService.uploadFile: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
