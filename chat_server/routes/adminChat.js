import express from 'express'
import pool from '../config/database.js'
import { verifyToken, isAdmin } from '../middleware/auth.js'

const router = express.Router()

router.get('/groups', verifyToken, isAdmin, async (req, res) => {
  try {
    const { page = 1, size = 10, keyword = '' } = req.query
    const offset = (parseInt(page) - 1) * parseInt(size)

    let whereClause = ''
    const params = []

    if (keyword) {
      whereClause = ' WHERE g.group_name LIKE ?'
      params.push(`%${keyword}%`)
    }

    const [groups] = await pool.execute(
      `SELECT g.group_id, g.group_name, g.group_avatar, g.group_type, g.is_public, g.created_at,
              u.username as owner_name,
              (SELECT COUNT(*) FROM group_members WHERE group_id = g.group_id) as member_count
       FROM chat_groups g
       JOIN users u ON g.owner_id = u.user_id
       ${whereClause}
       ORDER BY g.created_at DESC
       LIMIT ? OFFSET ?`,
      [...params, parseInt(size), offset]
    )

    const [[{ total }]] = await pool.execute(
      `SELECT COUNT(*) as total FROM chat_groups g ${whereClause}`,
      params
    )

    res.json({ success: true, data: { list: groups, total } })
  } catch (error) {
    console.error('Get all groups error:', error)
    res.status(500).json({ success: false, message: '获取群组列表失败' })
  }
})

router.get('/groups/:groupId', verifyToken, isAdmin, async (req, res) => {
  try {
    const { groupId } = req.params

    const [groups] = await pool.execute(
      `SELECT g.*, u.username as owner_name
       FROM chat_groups g
       JOIN users u ON g.owner_id = u.user_id
       WHERE g.group_id = ?`,
      [groupId]
    )

    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: '群组不存在' })
    }

    const [members] = await pool.execute(
      `SELECT u.user_id, u.username, u.email, gm.role, gm.joined_at
       FROM group_members gm
       JOIN users u ON gm.user_id = u.user_id
       WHERE gm.group_id = ?
       ORDER BY gm.role DESC`,
      [groupId]
    )

    const [messages] = await pool.execute(
      `SELECT COUNT(*) as total FROM chat_messages WHERE group_id = ? AND is_deleted = 0`,
      [groupId]
    )

    res.json({
      success: true,
      data: {
        ...groups[0],
        members,
        messageCount: messages[0]?.total || 0,
      },
    })
  } catch (error) {
    console.error('Get group detail error:', error)
    res.status(500).json({ success: false, message: '获取群组详情失败' })
  }
})

router.post('/groups/:groupId/dismiss', verifyToken, isAdmin, async (req, res) => {
  try {
    const { groupId } = req.params
    const adminId = req.userId

    const [groups] = await pool.execute('SELECT * FROM chat_groups WHERE group_id = ?', [groupId])

    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: '群组不存在' })
    }

    await pool.execute('DELETE FROM chat_messages WHERE group_id = ?', [groupId])
    await pool.execute('DELETE FROM group_ban_records WHERE group_id = ?', [groupId])
    await pool.execute('DELETE FROM group_members WHERE group_id = ?', [groupId])
    await pool.execute('DELETE FROM chat_groups WHERE group_id = ?', [groupId])

    if (req.io) {
      req.io.to(`group_${groupId}`).emit('group_dismissed', { groupId, dismissedBy: adminId })
    }

    res.json({ success: true, message: '群组已解散' })
  } catch (error) {
    console.error('Dismiss group error:', error)
    res.status(500).json({ success: false, message: '解散群组失败' })
  }
})

router.post('/groups/:groupId/ban', verifyToken, isAdmin, async (req, res) => {
  try {
    const { groupId } = req.params
    const { userId, reason, duration } = req.body
    const bannedBy = req.userId

    const [groups] = await pool.execute('SELECT * FROM chat_groups WHERE group_id = ?', [groupId])
    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: '群组不存在' })
    }

    const [targetMember] = await pool.execute(
      'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
      [groupId, userId]
    )
    if (targetMember.length === 0) {
      return res.status(404).json({ success: false, message: '该用户不是群成员' })
    }

    if (targetMember[0].role === 3) {
      return res.status(400).json({ success: false, message: '无法禁言群主' })
    }

    const expiredAt = duration ? new Date(Date.now() + duration * 60 * 1000) : null

    await pool.execute(
      'INSERT INTO group_ban_records (group_id, user_id, reason, expired_at, banned_by) VALUES (?, ?, ?, ?, ?)',
      [groupId, userId, reason || '', expiredAt, bannedBy]
    )

    await pool.execute('DELETE FROM group_members WHERE group_id = ? AND user_id = ?', [groupId, userId])

    if (req.io) {
      req.io.to(`user_${userId}`).emit('banned_from_group', {
        groupId,
        reason,
        duration,
      })
    }

    res.json({ success: true, message: '已将用户移出群组并禁言' })
  } catch (error) {
    console.error('Ban user error:', error)
    res.status(500).json({ success: false, message: '操作失败' })
  }
})

router.post('/groups/:groupId/set-admin', verifyToken, isAdmin, async (req, res) => {
  try {
    const { groupId } = req.params
    const { userId, role } = req.body

    const [targetMember] = await pool.execute(
      'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
      [groupId, userId]
    )

    if (targetMember.length === 0) {
      return res.status(404).json({ success: false, message: '该用户不是群成员' })
    }

    if (targetMember[0].role === 3) {
      return res.status(400).json({ success: false, message: '无法修改群主权限' })
    }

    const newRole = role === 'admin' ? 2 : 1
    await pool.execute('UPDATE group_members SET role = ? WHERE group_id = ? AND user_id = ?', [newRole, groupId, userId])

    res.json({ success: true, message: `已将用户设为${role === 'admin' ? '管理员' : '普通成员'}` })
  } catch (error) {
    console.error('Set admin error:', error)
    res.status(500).json({ success: false, message: '操作失败' })
  }
})

router.get('/messages', verifyToken, isAdmin, async (req, res) => {
  try {
    const { page = 1, size = 20, keyword, groupId, userId } = req.query
    const offset = (parseInt(page) - 1) * parseInt(size)

    let whereClause = ' WHERE m.is_deleted = 0 '
    const params = []

    if (groupId) {
      whereClause += ' AND m.group_id = ? '
      params.push(parseInt(groupId))
    }

    if (userId) {
      whereClause += ' AND m.sender_id = ? '
      params.push(parseInt(userId))
    }

    if (keyword) {
      whereClause += ' AND m.content LIKE ? '
      params.push(`%${keyword}%`)
    }

    const [messages] = await pool.execute(
      `SELECT m.message_id, m.group_id, m.sender_id, m.message_type, m.content, m.file_url, m.file_name, m.created_at,
              u.username as sender_username, g.group_name
       FROM chat_messages m
       JOIN users u ON m.sender_id = u.user_id
       JOIN chat_groups g ON m.group_id = g.group_id
       ${whereClause}
       ORDER BY m.created_at DESC
       LIMIT ? OFFSET ?`,
      [...params, parseInt(size), offset]
    )

    const [[{ total }]] = await pool.execute(
      `SELECT COUNT(*) as total FROM chat_messages m ${whereClause}`,
      params
    )

    res.json({ success: true, data: { list: messages, total } })
  } catch (error) {
    console.error('Get all messages error:', error)
    res.status(500).json({ success: false, message: '获取消息列表失败' })
  }
})

router.delete('/messages/:messageId', verifyToken, isAdmin, async (req, res) => {
  try {
    const { messageId } = req.params

    const [messages] = await pool.execute('SELECT * FROM chat_messages WHERE message_id = ?', [messageId])

    if (messages.length === 0) {
      return res.status(404).json({ success: false, message: '消息不存在' })
    }

    await pool.execute('UPDATE chat_messages SET is_deleted = 1 WHERE message_id = ?', [messageId])

    const message = messages[0]
    if (req.io) {
      req.io.to(`group_${message.group_id}`).emit('message_deleted', { messageId })
    }

    res.json({ success: true, message: '消息已删除' })
  } catch (error) {
    console.error('Admin delete message error:', error)
    res.status(500).json({ success: false, message: '删除消息失败' })
  }
})

export default router