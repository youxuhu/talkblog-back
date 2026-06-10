package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequireRoles
public class FileUploadController {

    private final FileStorageService fileStorageService;

    public FileUploadController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("请选择文件"));
        }

        String fileUrl = fileStorageService.storeFile(file);
        String originalName = file.getOriginalFilename();
        String contentType = file.getContentType();

        Map<String, Object> data = Map.of(
                "fileUrl", fileUrl,
                "fileName", originalName != null ? originalName : "unknown",
                "fileSize", file.getSize(),
                "contentType", contentType != null ? contentType : "application/octet-stream"
        );

        return ResponseEntity.ok(ApiResponse.success("文件上传成功", data));
    }
}