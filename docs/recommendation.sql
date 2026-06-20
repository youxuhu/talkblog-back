-- 推荐系统：行为日志表
CREATE TABLE IF NOT EXISTS user_behavior_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    blog_id BIGINT NOT NULL REFERENCES blogs(id),
    behavior_type VARCHAR(20) NOT NULL,
    score DECIMAL(3,1) DEFAULT 1.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ubl_user ON user_behavior_log(user_id);
CREATE INDEX IF NOT EXISTS idx_ubl_blog ON user_behavior_log(blog_id);
CREATE INDEX IF NOT EXISTS idx_ubl_type ON user_behavior_log(behavior_type);
CREATE INDEX IF NOT EXISTS idx_ubl_created ON user_behavior_log(created_at);

-- 推荐系统：用户兴趣标签（按需聚合计算）
CREATE TABLE IF NOT EXISTS user_interest_tags (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    tag_id BIGINT NOT NULL REFERENCES tags(id),
    weight DECIMAL(10,4) DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, tag_id)
);

-- 推荐系统：用户偏好分类（按需聚合计算）
CREATE TABLE IF NOT EXISTS user_preferred_categories (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    weight DECIMAL(10,4) DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, category_id)
);
