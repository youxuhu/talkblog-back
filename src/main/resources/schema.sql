-- Chatroom Management Tables for TalkBlog
-- PostgreSQL Database Schema
-- Auto-generated from actual database (2026-05-13)

-- Trigger function for auto-updating updated_at
CREATE OR REPLACE FUNCTION update_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create chatrooms table
CREATE TABLE IF NOT EXISTS chatrooms (
    chatroom_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    member_count INTEGER NOT NULL DEFAULT 0,
    message_count INTEGER NOT NULL DEFAULT 0,
    max_members INTEGER NOT NULL DEFAULT 100,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    creator_id BIGINT
);

-- Auto-update updated_at on chatrooms
DROP TRIGGER IF EXISTS trg_chatrooms_updated_at ON chatrooms;
CREATE TRIGGER trg_chatrooms_updated_at
    BEFORE UPDATE ON chatrooms
    FOR EACH ROW
    EXECUTE FUNCTION update_timestamp();

-- Create chatroom_members table
CREATE TABLE IF NOT EXISTS chatroom_members (
    chatroom_id BIGINT NOT NULL REFERENCES chatrooms(chatroom_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    nickname VARCHAR(100),
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_active_time TIMESTAMP,
    message_count INTEGER NOT NULL DEFAULT 0,
    muted_until TIMESTAMP,
    PRIMARY KEY (chatroom_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_cm_user_id ON chatroom_members(user_id);

-- Create chatroom_messages table
CREATE TABLE IF NOT EXISTS chatroom_messages (
    message_id BIGSERIAL PRIMARY KEY,
    chatroom_id BIGINT NOT NULL REFERENCES chatrooms(chatroom_id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_messages_chatroom_created ON chatroom_messages(chatroom_id, created_at);

-- Create daily_stats table
CREATE TABLE IF NOT EXISTS daily_stats (
    chatroom_id BIGINT NOT NULL REFERENCES chatrooms(chatroom_id) ON DELETE CASCADE,
    date DATE NOT NULL,
    message_count INTEGER NOT NULL DEFAULT 0,
    active_members INTEGER NOT NULL DEFAULT 0,
    new_members INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (chatroom_id, date)
);
