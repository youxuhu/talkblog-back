-- ================================================================
-- 迁移 talkblog 数据库：补齐缺失的表/列/约束以匹配 postgres
-- 执行方式: psql -h localhost -U postgres -d talkblog -f <file>
-- ================================================================

BEGIN;

-- 1. users 表统一约束（email NOT NULL, password_hash 可空）
ALTER TABLE users ALTER COLUMN email SET NOT NULL;
ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

-- 2. comment_likes 替换为带 like_id 序列主键的版本
DROP TABLE comment_likes CASCADE;
CREATE SEQUENCE IF NOT EXISTS comment_likes_like_id_seq;
CREATE TABLE comment_likes (
    like_id    BIGINT PRIMARY KEY DEFAULT nextval('comment_likes_like_id_seq'),
    comment_id BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE comment_likes_like_id_seq OWNED BY comment_likes.like_id;
CREATE UNIQUE INDEX comment_likes_comment_id_user_id_key ON comment_likes(comment_id, user_id);
ALTER TABLE comment_likes ADD CONSTRAINT comment_likes_comment_id_fkey
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE;
ALTER TABLE comment_likes ADD CONSTRAINT comment_likes_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;

-- 3. blogs 补齐 like_count、view_count
ALTER TABLE blogs ADD COLUMN IF NOT EXISTS like_count INTEGER DEFAULT 0;
ALTER TABLE blogs ADD COLUMN IF NOT EXISTS view_count INTEGER DEFAULT 0;

-- 4. blog_likes
CREATE TABLE IF NOT EXISTS blog_likes (
    blog_id    BIGINT NOT NULL,
    user_id    BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (blog_id, user_id)
);

-- 5. blog_views（含序列 + 索引 + FK）
CREATE SEQUENCE IF NOT EXISTS blog_views_id_seq;
CREATE TABLE IF NOT EXISTS blog_views (
    id         BIGINT PRIMARY KEY DEFAULT nextval('blog_views_id_seq'),
    blog_id    BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_id    BIGINT,
    viewed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER SEQUENCE blog_views_id_seq OWNED BY blog_views.id;
CREATE INDEX IF NOT EXISTS idx_blog_views_blog_id ON blog_views(blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_views_ip_blog ON blog_views(ip_address, blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_views_viewed_at ON blog_views(viewed_at);
ALTER TABLE blog_views ADD CONSTRAINT blog_views_blog_id_fkey
    FOREIGN KEY (blog_id) REFERENCES blogs(id) ON DELETE CASCADE;
ALTER TABLE blog_views ADD CONSTRAINT blog_views_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(user_id);

-- 6. user_favorites（含序列 + 唯一约束）
CREATE SEQUENCE IF NOT EXISTS user_favorites_id_seq;
CREATE TABLE IF NOT EXISTS user_favorites (
    id         BIGINT PRIMARY KEY DEFAULT nextval('user_favorites_id_seq'),
    user_id    BIGINT NOT NULL,
    blog_id    BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, blog_id)
);
ALTER SEQUENCE user_favorites_id_seq OWNED BY user_favorites.id;

COMMIT;
