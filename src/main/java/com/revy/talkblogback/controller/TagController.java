package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Tag;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Tag>>> list(@RequestParam(required = false) String q) {
        List<Tag> tags = tagService.search(q);
        return ResponseEntity.ok(ApiResponse.success("success", tags));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Tag>> detail(@PathVariable Long id) {
        Tag tag = tagService.getById(id);
        if (tag == null) {
            return ResponseEntity.ok(ApiResponse.failure("Tag not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("success", tag));
    }

    @PostMapping
    @RequireRoles
    public ResponseEntity<ApiResponse<Tag>> create(@RequestBody Tag tag) {
        try {
            Tag created = tagService.create(tag);
            return ResponseEntity.ok(ApiResponse.success("Tag created", created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        boolean deleted = tagService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Tag deleted"));
        }
        return ResponseEntity.ok(ApiResponse.failure("Delete failed"));
    }
}
