-- ============================================================
-- TalkBlog v3 迁移
-- 新增: is_pinned(评论置顶), comment_reports(举报)
-- ============================================================

-- 1. comments 表新增置顶字段
ALTER TABLE comments ADD COLUMN IF NOT EXISTS is_pinned BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. 评论举报表
CREATE TABLE IF NOT EXISTS comment_reports (
    id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comments(comment_id) ON DELETE CASCADE,
    reporter_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    reason VARCHAR(50) NOT NULL,
    description TEXT,
    status SMALLINT NOT NULL DEFAULT 0,
    handled_by BIGINT REFERENCES users(user_id),
    handled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comment_reports_comment_id ON comment_reports(comment_id);
CREATE INDEX IF NOT EXISTS idx_comment_reports_status ON comment_reports(status);

-- 3. comments 表新增编辑历史字段（记录编辑次数）
ALTER TABLE comments ADD COLUMN IF NOT EXISTS edit_count INT NOT NULL DEFAULT 0;
