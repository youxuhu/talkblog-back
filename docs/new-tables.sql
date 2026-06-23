-- Series table
CREATE TABLE IF NOT EXISTS series (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(200) DEFAULT '',
    author_id BIGINT NOT NULL REFERENCES users(user_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_series_author_id ON series(author_id);

-- Add series_id and scheduled_at to blogs table
ALTER TABLE blogs ADD COLUMN IF NOT EXISTS series_id BIGINT REFERENCES series(id);
ALTER TABLE blogs ADD COLUMN IF NOT EXISTS scheduled_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_blogs_series_id ON blogs(series_id);

-- Notifications table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id),
    type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    link VARCHAR(500),
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
