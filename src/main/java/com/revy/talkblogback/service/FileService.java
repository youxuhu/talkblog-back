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

    @Value("${file.blog-images-dir:blog-images}")
    private String blogImagesDir;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private Path resolveUploadPath(String subDir) {
        Path base = Paths.get(System.getProperty("user.dir"), uploadDir);
        return base.resolve(subDir);
    }

    public List<String> uploadCommentImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> imageUrls = new ArrayList<>();
        Path uploadPath = resolveUploadPath(commentImagesDir);

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

    public String uploadBlogImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        validateFile(file);
        Path uploadPath = resolveUploadPath(blogImagesDir);
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("无法创建上传目录");
        }

        String fileName = generateFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(fileName);
        try {
            file.transferTo(filePath.toFile());
            return "/" + uploadDir + "/" + blogImagesDir + "/" + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("文件上传失败: " + e.getClass().getSimpleName() + " - " + e.getMessage() + " (path: " + filePath.toAbsolutePath() + ")");
        }
    }

    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        validateFile(file);
        Path uploadPath = resolveUploadPath("avatars");
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new IllegalArgumentException("无法创建上传目录");
        }
        String fileName = generateFileName(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(fileName);
        try {
            file.transferTo(filePath.toFile());
            return "/" + uploadDir + "/avatars/" + fileName;
        } catch (IOException e) {
            throw new IllegalArgumentException("头像上传失败");
        }
    }

    public void deleteBlogImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("图片地址不能为空");
        }

        String relativePath = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
        Path base = Paths.get(System.getProperty("user.dir"));
        Path filePath = base.resolve(relativePath).normalize();

        Path uploadPath = resolveUploadPath(blogImagesDir).normalize();
        if (!filePath.startsWith(uploadPath)) {
            throw new IllegalArgumentException("不允许删除该路径下的文件");
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new IllegalArgumentException("删除图片失败: " + e.getMessage());
        }
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
