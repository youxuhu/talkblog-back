import express from 'express'
import bcrypt from 'bcryptjs'
import jwt from 'jsonwebtoken'
import pool from '../config/database.js'
import { verifyToken, isAdmin } from '../middleware/auth.js'

const router = express.Router()

router.post('/register', async (req, res) => {
  try {
    const { email, username, password } = req.body

    if (!email || !username || !password) {
      return res.status(400).json({ success: false, message: '请填写完整信息' })
    }

    const [existing] = await pool.execute('SELECT user_id FROM users WHERE email = ? OR username = ?', [email, username])
    if (existing.length > 0) {
      return res.status(400).json({ success: false, message: '邮箱或用户名已存在' })
    }

    const hashedPassword = await bcrypt.hash(password, 10)

    const [result] = await pool.execute(
      'INSERT INTO users (username, email, password, roles, status, login_type) VALUES (?, ?, ?, ?, ?, ?)',
      [username, email, hashedPassword, JSON.stringify(['USER']), 1, 'password']
    )

    res.json({ success: true, message: '注册成功', data: { userId: result.insertId, username, email } })
  } catch (error) {
    console.error('Register error:', error)
    res.status(500).json({ success: false, message: '注册失败，请稍后重试' })
  }
})

router.post('/login', async (req, res) => {
  try {
    const { email, password } = req.body

    if (!email || !password) {
      return res.status(400).json({ success: false, message: '请输入邮箱和密码' })
    }

    const [users] = await pool.execute('SELECT * FROM users WHERE email = ?', [email])
    const user = users[0]

    if (!user) {
      return res.status(401).json({ success: false, message: '用户不存在' })
    }

    const validPassword = await bcrypt.compare(password, user.password)
    if (!validPassword) {
      return res.status(401).json({ success: false, message: '密码错误' })
    }

    if (user.status === 3) {
      return res.status(403).json({ success: false, message: '账户已被禁用' })
    }

    await pool.execute('UPDATE users SET last_login_time = NOW() WHERE user_id = ?', [user.user_id])

    const token = jwt.sign(
      { userId: user.user_id, username: user.username, roles: JSON.parse(user.roles || '[]') },
      process.env.JWT_SECRET,
      { expiresIn: process.env.JWT_EXPIRES_IN }
    )

    res.json({
      success: true,
      message: '登录成功',
      data: {
        accessToken: token,
        user: {
          userId: user.user_id,
          username: user.username,
          email: user.email,
          avatar: user.avatar,
          roles: JSON.parse(user.roles || '[]'),
          loginType: user.login_type,
        },
      },
    })
  } catch (error) {
    console.error('Login error:', error)
    res.status(500).json({ success: false, message: '登录失败，请稍后重试' })
  }
})

router.get('/me', verifyToken, async (req, res) => {
  try {
    const [users] = await pool.execute('SELECT user_id, username, email, avatar, roles, status, login_type, created_at FROM users WHERE user_id = ?', [req.userId])

    if (users.length === 0) {
      return res.status(404).json({ success: false, message: '用户不存在' })
    }

    const user = users[0]
    res.json({
      success: true,
      data: {
        userId: user.user_id,
        username: user.username,
        email: user.email,
        avatar: user.avatar,
        roles: JSON.parse(user.roles || '[]'),
        status: user.status,
        loginType: user.login_type,
      },
    })
  } catch (error) {
    console.error('Get profile error:', error)
    res.status(500).json({ success: false, message: '获取用户信息失败' })
  }
})

export default router