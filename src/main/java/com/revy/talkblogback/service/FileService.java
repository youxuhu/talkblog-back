package com.revy.talkblogback.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.comment-images-dir:comment-images}")
    private String commentImagesDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

    public List<String> uploadCommentImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> imageUrls = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir, commentImagesDir);

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("无法创建上传目录: " + e.getMessage());
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            validateFile(file);
            String fileName = generateFileName(file.getOriginalFilename());
            Path filePath = uploadPath.resolve(fileName);

            try {
                file.transferTo(filePath.toFile());
                String imageUrl = "/" + uploadDir + "/" + commentImagesDir + "/" + fileName;
                imageUrls.add(imageUrl);
            } catch (IOException e) {
                throw new IllegalArgumentException("文件上传失败: " + e.getMessage());
            }
        }

        return imageUrls;
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过5MB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("只允许上传图片文件 (JPEG, PNG, GIF, WebP)");
        }
    }

    private String generateFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
