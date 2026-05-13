-- 添加评论图片表
-- 执行此文件将 comment_images 表添加到现有数据库

CREATE TABLE IF NOT EXISTS comment_images (
    image_id BIGSERIAL PRIMARY KEY,
    comment_id BIGINT NOT NULL REFERENCES comments(comment_id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    image_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_comment_images_comment_id ON comment_images(comment_id);

-- 验证表是否创建成功
-- SELECT table_name FROM information_schema.tables WHERE table_name = 'comment_images';
