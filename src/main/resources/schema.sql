CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================
-- Users & Auth
-- ============================================================

CREATE TABLE IF NOT EXISTS roles (
    role_id   SERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS users (
    user_id        BIGSERIAL PRIMARY KEY,
    username       VARCHAR(100) NOT NULL,
    email          VARCHAR(255) NOT NULL UNIQUE,
    phone          VARCHAR(20),
    password_hash  VARCHAR(255),
    face_vector_id BIGINT,
    login_type     VARCHAR(20) DEFAULT 'EMAIL',
    avatar_url     VARCHAR(500),
    status         SMALLINT DEFAULT 1,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_time TIMESTAMP
);

CREATE TABLE IF NOT EXISTS face_vectors (
    vector_id     BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(user_id),
    face_vector   VECTOR(512),
    face_image_url VARCHAR(500),
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    role_id INT NOT NULL REFERENCES roles(role_id),
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- Blog
-- ============================================================

CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    slug        VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    parent_id   BIGINT REFERENCES categories(id),
    sort_order  INT DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tags (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(50) NOT NULL UNIQUE,
    slug       VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS series (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    author_id   BIGINT NOT NULL REFERENCES users(user_id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS blogs (
    id          BIGSERIAL PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    content     TEXT NOT NULL,
    author_id   BIGINT NOT NULL REFERENCES users(user_id),
    status      SMALLINT DEFAULT 1,
    like_count  INTEGER DEFAULT 0,
    view_count  INTEGER DEFAULT 0,
    category_id BIGINT REFERENCES categories(id),
    series_id   BIGINT REFERENCES series(id),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE blogs ADD COLUMN IF NOT EXISTS series_id BIGINT REFERENCES series(id);
ALTER TABLE blogs ADD COLUMN IF NOT EXISTS view_count BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS blog_tags (
    blog_id BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (blog_id, tag_id)
);

CREATE TABLE IF NOT EXISTS blog_likes (
    blog_id    BIGINT NOT NULL REFERENCES blogs(id),
    user_id    BIGINT NOT NULL REFERENCES users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (blog_id, user_id)
);

CREATE TABLE IF NOT EXISTS user_favorites (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(user_id),
    blog_id    BIGINT NOT NULL REFERENCES blogs(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, blog_id)
);

CREATE TABLE IF NOT EXISTS blog_views (
    id         BIGSERIAL PRIMARY KEY,
    blog_id    BIGINT NOT NULL REFERENCES blogs(id) ON DELETE CASCADE,
    ip_address VARCHAR(45),
    user_id    BIGINT REFERENCES users(user_id),
    viewed_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Comments
-- ============================================================

CREATE TABLE IF NOT EXISTS comments (
    comment_id      BIGSERIAL PRIMARY KEY,
    blog_id         BIGINT NOT NULL,
    user_id         BIGINT NOT NULL REFERENCES users(user_id),
    parent_id       BIGINT REFERENCES comments(comment_id),
    reply_to_user_id BIGINT REFERENCES users(user_id),
    content         TEXT NOT NULL,
    status          SMALLINT DEFAULT 0,
    like_count      INTEGER DEFAULT 0,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS comment_likes (
    comment_id BIGINT NOT NULL REFERENCES comments(comment_id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (comment_id, user_id)
);

CREATE TABLE IF NOT EXISTS comment_images (
    image_id    BIGSERIAL PRIMARY KEY,
    comment_id  BIGINT NOT NULL REFERENCES comments(comment_id) ON DELETE CASCADE,
    image_url   VARCHAR(500) NOT NULL,
    image_order INTEGER DEFAULT 0,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Chat
-- ============================================================

CREATE TABLE IF NOT EXISTS chat_groups (
    group_id    BIGSERIAL PRIMARY KEY,
    group_name  VARCHAR(100) NOT NULL,
    group_avatar VARCHAR(500),
    owner_id    BIGINT NOT NULL REFERENCES users(user_id),
    group_type  SMALLINT DEFAULT 1,
    is_public   SMALLINT DEFAULT 1,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS group_members (
    id        BIGSERIAL PRIMARY KEY,
    group_id  BIGINT NOT NULL REFERENCES chat_groups(group_id) ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES users(user_id),
    role      SMALLINT DEFAULT 1,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    message_id   BIGSERIAL PRIMARY KEY,
    group_id     BIGINT REFERENCES chat_groups(group_id) ON DELETE CASCADE,
    sender_id    BIGINT NOT NULL REFERENCES users(user_id),
    message_type SMALLINT DEFAULT 1,
    content      TEXT,
    file_url     VARCHAR(500),
    file_name    VARCHAR(255),
    file_size    BIGINT,
    is_deleted   SMALLINT DEFAULT 0,
    is_recalled  SMALLINT DEFAULT 0,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS group_join_requests (
    id         BIGSERIAL PRIMARY KEY,
    group_id   BIGINT NOT NULL REFERENCES chat_groups(group_id),
    user_id    BIGINT NOT NULL REFERENCES users(user_id),
    status     SMALLINT DEFAULT 0,
    reason     TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (group_id, user_id)
);

CREATE TABLE IF NOT EXISTS group_ban_records (
    id         BIGSERIAL PRIMARY KEY,
    group_id   BIGINT NOT NULL REFERENCES chat_groups(group_id) ON DELETE CASCADE,
    user_id    BIGINT NOT NULL REFERENCES users(user_id),
    reason     VARCHAR(255),
    banned_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expired_at TIMESTAMP,
    banned_by  BIGINT NOT NULL REFERENCES users(user_id)
);

-- ============================================================
-- Recommendation
-- ============================================================

CREATE TABLE IF NOT EXISTS user_behavior_log (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT NOT NULL REFERENCES users(user_id),
    blog_id       BIGINT NOT NULL REFERENCES blogs(id),
    behavior_type VARCHAR(20) NOT NULL,
    score         DECIMAL(3,1) DEFAULT 1.0,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_interest_tags (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(user_id),
    tag_id     BIGINT NOT NULL REFERENCES tags(id),
    weight     DECIMAL(10,4) DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, tag_id)
);

CREATE TABLE IF NOT EXISTS user_preferred_categories (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(user_id),
    category_id BIGINT NOT NULL REFERENCES categories(id),
    weight      DECIMAL(10,4) DEFAULT 0,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (user_id, category_id)
);

-- ============================================================
-- Indexes
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_face_vectors_user_id ON face_vectors(user_id);

CREATE INDEX IF NOT EXISTS idx_blogs_author_id ON blogs(author_id);
CREATE INDEX IF NOT EXISTS idx_blogs_status ON blogs(status);
CREATE INDEX IF NOT EXISTS idx_blogs_category_id ON blogs(category_id);
CREATE INDEX IF NOT EXISTS idx_blogs_created_at ON blogs(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_comments_blog_id ON comments(blog_id);
CREATE INDEX IF NOT EXISTS idx_comments_user_id ON comments(user_id);
CREATE INDEX IF NOT EXISTS idx_comments_parent_id ON comments(parent_id);
CREATE INDEX IF NOT EXISTS idx_comments_status ON comments(status);
CREATE INDEX IF NOT EXISTS idx_comments_created_at ON comments(created_at);
CREATE INDEX IF NOT EXISTS idx_comment_images_comment_id ON comment_images(comment_id);

CREATE INDEX IF NOT EXISTS idx_group_members_group_id ON group_members(group_id);
CREATE INDEX IF NOT EXISTS idx_group_members_user_id ON group_members(user_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_group_id ON chat_messages(group_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_sender_id ON chat_messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_chat_messages_created_at ON chat_messages(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_group_ban_records_group_id ON group_ban_records(group_id);
CREATE INDEX IF NOT EXISTS idx_group_ban_records_user_id ON group_ban_records(user_id);
CREATE INDEX IF NOT EXISTS idx_gjr_group_id ON group_join_requests(group_id);
CREATE INDEX IF NOT EXISTS idx_gjr_user_id ON group_join_requests(user_id);
CREATE INDEX IF NOT EXISTS idx_gjr_status ON group_join_requests(status);

CREATE INDEX IF NOT EXISTS idx_ubl_user ON user_behavior_log(user_id);
CREATE INDEX IF NOT EXISTS idx_ubl_blog ON user_behavior_log(blog_id);
CREATE INDEX IF NOT EXISTS idx_ubl_type ON user_behavior_log(behavior_type);
CREATE INDEX IF NOT EXISTS idx_ubl_created ON user_behavior_log(created_at);

CREATE INDEX IF NOT EXISTS idx_blog_views_blog_id ON blog_views(blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_views_ip_blog ON blog_views(ip_address, blog_id);
CREATE INDEX IF NOT EXISTS idx_blog_views_viewed_at ON blog_views(viewed_at);

-- ============================================================
-- Seed data
-- ============================================================

INSERT INTO roles (role_name, description) VALUES
    ('USER', '普通用户'),
    ('ADMIN', '管理员'),
    ('SUPER_ADMIN', '超级管理员')
ON CONFLICT (role_name) DO NOTHING;
