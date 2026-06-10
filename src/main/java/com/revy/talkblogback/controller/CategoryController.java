package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.Category;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> list() {
        List<Category> tree = categoryService.getTree();
        return ResponseEntity.ok(ApiResponse.success("success", tree));
    }

    @GetMapping("/flat")
    public ResponseEntity<ApiResponse<List<Category>>> flat() {
        List<Category> list = categoryService.getAll();
        return ResponseEntity.ok(ApiResponse.success("success", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> detail(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return ResponseEntity.ok(ApiResponse.failure("Category not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("success", category));
    }

    @PostMapping
    @RequireRoles
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category category) {
        try {
            Category created = categoryService.create(category);
            return ResponseEntity.ok(ApiResponse.success("Category created", created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Category>> update(@PathVariable Long id, @RequestBody Category category) {
        try {
            Category updated = categoryService.update(id, category);
            return ResponseEntity.ok(ApiResponse.success("Category updated", updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.failure(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @RequireRoles
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        boolean deleted = categoryService.delete(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Category deleted"));
        }
        return ResponseEntity.ok(ApiResponse.failure("Delete failed"));
    }
}
