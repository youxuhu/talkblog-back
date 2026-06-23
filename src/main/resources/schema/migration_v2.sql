-- ============================================================
-- TalkBlog v2 数据库迁移
-- 新增: category, view_count, bookmarks, follows, blog_versions, sensitive_words
-- ============================================================

-- 1. blogs 表新增字段
ALTER TABLE blogs ADD COLUMN IF NOT EXISTS category VARCHAR(50) DEFAULT '';
ALTER TABLE blogs ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;

-- 2. 收藏表
CREATE TABLE IF NOT EXISTS bookmarks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, blog_id)
);
CREATE INDEX IF NOT EXISTS idx_bookmarks_user_id ON bookmarks(user_id);
CREATE INDEX IF NOT EXISTS idx_bookmarks_blog_id ON bookmarks(blog_id);

-- 3. 关注表
CREATE TABLE IF NOT EXISTS follows (
    id BIGSERIAL PRIMARY KEY,
    follower_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    followee_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(follower_id, followee_id),
    CHECK (follower_id != followee_id)
);
CREATE INDEX IF NOT EXISTS idx_follows_follower ON follows(follower_id);
CREATE INDEX IF NOT EXISTS idx_follows_followee ON follows(followee_id);

-- 4. 博客版本历史表
CREATE TABLE IF NOT EXISTS blog_versions (
    id BIGSERIAL PRIMARY KEY,
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) DEFAULT '',
    series_id BIGINT,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_blog_versions_blog_id ON blog_versions(blog_id);

-- 5. 敏感词表
CREATE TABLE IF NOT EXISTS sensitive_words (
    id BIGSERIAL PRIMARY KEY,
    word VARCHAR(255) NOT NULL UNIQUE,
    replacement VARCHAR(255) DEFAULT '***',
    created_at TIMESTAMP DEFAULT NOW()
);

-- 插入默认敏感词
INSERT INTO sensitive_words (word, replacement) VALUES
    ('fuck', '***'),
    ('shit', '***'),
    ('damn', '***'),
    ('asshole', '***'),
    ('傻逼', '***'),
    ('妈的', '***'),
    ('草泥马', '***'),
    ('sb', '***')
ON CONFLICT (word) DO NOTHING;

-- 6. 给已有数据分配默认分类（基于 series 映射）
UPDATE blogs SET category = 'tech' WHERE series_id IN (SELECT id FROM series WHERE name = '技术前沿') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'technology' WHERE series_id IN (SELECT id FROM series WHERE name = '科技洞察') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'lifestyle' WHERE series_id IN (SELECT id FROM series WHERE name = '生活随笔') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'finance' WHERE series_id IN (SELECT id FROM series WHERE name = '财经观察') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'education' WHERE series_id IN (SELECT id FROM series WHERE name = '教育思考') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'culture' WHERE series_id IN (SELECT id FROM series WHERE name = '文化漫谈') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'sports' WHERE series_id IN (SELECT id FROM series WHERE name = '体育世界') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'digital' WHERE series_id IN (SELECT id FROM series WHERE name = '数码评测') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'gaming' WHERE series_id IN (SELECT id FROM series WHERE name = '游戏天地') AND (category IS NULL OR category = '');
UPDATE blogs SET category = 'entertainment' WHERE series_id IN (SELECT id FROM series WHERE name = '影视娱乐') AND (category IS NULL OR category = '');
