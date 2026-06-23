-- Roles table (if not exists)
CREATE TABLE IF NOT EXISTS roles (
    role_id INTEGER PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    role_description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- User roles junction table (if not exists)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    assigned_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, role_id)
);

-- Seed initial roles
INSERT INTO roles (role_id, role_name, role_description)
VALUES 
    (1, 'USER', 'Regular user'),
    (2, 'ADMIN', 'Administrator'),
    (3, 'SUPER_ADMIN', 'Super administrator')
ON CONFLICT (role_id) DO NOTHING;
