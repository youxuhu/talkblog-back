CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS face_vectors (
    vector_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    face_vector vector(512),
    face_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(50),
    password_hash VARCHAR(255) NOT NULL,
    face_vector_id BIGINT,
    login_type VARCHAR(20) DEFAULT 'EMAIL',
    avatar_url VARCHAR(500),
    status SMALLINT DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_login_time TIMESTAMP
);

ALTER TABLE users DROP CONSTRAINT IF EXISTS fk_users_face_vector;
ALTER TABLE users ADD CONSTRAINT fk_users_face_vector
    FOREIGN KEY (face_vector_id) REFERENCES face_vectors(vector_id);
ALTER TABLE face_vectors DROP CONSTRAINT IF EXISTS fk_face_vectors_user;
ALTER TABLE face_vectors ADD CONSTRAINT fk_face_vectors_user
    FOREIGN KEY (user_id) REFERENCES users(user_id) DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE IF NOT EXISTS series (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    author_id BIGINT NOT NULL REFERENCES users(user_id),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS blogs (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL REFERENCES users(user_id),
    category VARCHAR(50) DEFAULT '',
    series_id BIGINT REFERENCES series(id),
    scheduled_at TIMESTAMP,
    view_count BIGINT DEFAULT 0,
    status SMALLINT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
