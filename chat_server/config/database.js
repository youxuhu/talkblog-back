import mysql from 'mysql2/promise'
import dotenv from 'dotenv'

dotenv.config()

const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  port: parseInt(process.env.DB_PORT) || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'talkblog',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
})

export async function initDatabase() {
  const connection = await mysql.createConnection({
    host: process.env.DB_HOST || 'localhost',
    port: parseInt(process.env.DB_PORT) || 3306,
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASSWORD || '',
  })

  await connection.execute(`CREATE DATABASE IF NOT EXISTS \`${process.env.DB_NAME || 'talkblog'}\``)
  await connection.end()

  await pool.execute(`

CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  email VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  phone VARCHAR(20),
  avatar VARCHAR(500),
  roles JSON,
  status TINYINT DEFAULT 1 COMMENT '1:正常 2:冻结 3:禁用',
  login_type VARCHAR(20),
  last_login_time DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_groups (
  group_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_name VARCHAR(100) NOT NULL,
  group_avatar VARCHAR(500),
  owner_id BIGINT NOT NULL,
  group_type TINYINT DEFAULT 1 COMMENT '1:群组 2:私聊',
  is_public TINYINT DEFAULT 1 COMMENT '1:公开 2:私密',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (owner_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS group_members (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role TINYINT DEFAULT 1 COMMENT '1:成员 2:管理员 3:群主',
  joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY unique_member (group_id, user_id),
  FOREIGN KEY (group_id) REFERENCES chat_groups(group_id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
  message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_id BIGINT,
  sender_id BIGINT NOT NULL,
  message_type TINYINT DEFAULT 1 COMMENT '1:文本 2:图片 3:文件',
  content TEXT,
  file_url VARCHAR(500),
  file_name VARCHAR(255),
  file_size BIGINT,
  is_deleted TINYINT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (group_id) REFERENCES chat_groups(group_id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS group_ban_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  reason VARCHAR(255),
  banned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  expired_at DATETIME,
  banned_by BIGINT NOT NULL,
  FOREIGN KEY (group_id) REFERENCES chat_groups(group_id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (banned_by) REFERENCES users(user_id)
);

  `)

  console.log('Database initialized successfully')
}

export default pool