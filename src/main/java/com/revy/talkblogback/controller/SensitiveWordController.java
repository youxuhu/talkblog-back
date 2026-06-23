package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.SensitiveWord;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.service.SensitiveWordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sensitive-words")
@RequireRoles({"ADMIN", "SUPER_ADMIN"})
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    public SensitiveWordController(SensitiveWordService sensitiveWordService) {
        this.sensitiveWordService = sensitiveWordService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SensitiveWord>>> list() {
        List<SensitiveWord> words = sensitiveWordService.findAll();
        return ResponseEntity.ok(ApiResponse.success("获取成功", words));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> add(@RequestBody SensitiveWord request) {
        if (request.getWord() == null || request.getWord().isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.failure("敏感词不能为空"));
        }
        try {
            sensitiveWordService.add(request.getWord(), request.getReplacement());
            return ResponseEntity.ok(ApiResponse.success("添加成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            sensitiveWordService.deleteById(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }
}
