package com.revy.talkblogback.controller;

import com.revy.talkblogback.auth.RequireRoles;
import com.revy.talkblogback.pojo.request.BatchReviewRequest;
import com.revy.talkblogback.pojo.request.UpdateCommentStatusRequest;
import com.revy.talkblogback.pojo.response.ApiResponse;
import com.revy.talkblogback.pojo.response.CommentRow;
import com.revy.talkblogback.pojo.response.CommentStats;
import com.revy.talkblogback.pojo.response.PageResult;
import com.revy.talkblogback.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comments")
@RequireRoles({"ADMIN", "SUPER_ADMIN"})
public class AdminCommentController {

    private final CommentService commentService;

    public AdminCommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResult<CommentRow>>> getComments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String blogId) {
        Short statusValue = null;
        if (status != null && !status.equals("NaN") && !status.isEmpty()) {
            try {
                statusValue = Short.parseShort(status);
            } catch (NumberFormatException e) {
                statusValue = null;
            }
        }
        PageResult<CommentRow> result = commentService.getAdminComments(page, size, keyword, statusValue, blogId);
        return ResponseEntity.ok(ApiResponse.success("获取成功", result));
    }

    @PatchMapping("/{commentId}/status")
    public ResponseEntity<ApiResponse<Void>> reviewComment(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentStatusRequest request) {
        try {
            commentService.reviewComment(commentId, request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("审核成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.adminDeleteComment(commentId);
            return ResponseEntity.ok(ApiResponse.success("评论已删除", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @PatchMapping("/batch-status")
    public ResponseEntity<ApiResponse<Void>> batchReview(@Valid @RequestBody BatchReviewRequest request) {
        try {
            commentService.batchReviewComments(request.getCommentIds(), request.getStatus());
            return ResponseEntity.ok(ApiResponse.success("批量操作成功", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<CommentStats>> getStats(
            @RequestParam(defaultValue = "30") int days) {
        CommentStats stats = commentService.getCommentStats(days);
        return ResponseEntity.ok(ApiResponse.success("获取统计数据成功", stats));
    }
}
