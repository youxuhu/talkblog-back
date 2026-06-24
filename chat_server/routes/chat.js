import express from 'express'
import pool from '../config/database.js'
import { verifyToken } from '../middleware/auth.js'

const router = express.Router()

router.post('/groups', verifyToken, async (req, res) => {
  try {
    const { groupName, groupType = 1, isPublic = 1, memberIds = [] } = req.body
    const ownerId = req.userId

    if (!groupName) {
      return res.status(400).json({ success: false, message: '请输入群组名称' })
    }

    const groupTypeInt = groupType === 'private' ? 2 : 1
    const isPublicInt = isPublic === 'private' ? 2 : 1

    const [result] = await pool.execute(
      'INSERT INTO chat_groups (group_name, owner_id, group_type, is_public) VALUES (?, ?, ?, ?)',
      [groupName, ownerId, groupTypeInt, isPublicInt]
    )

    const groupId = result.insertId

    await pool.execute(
      'INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)',
      [groupId, ownerId, 3]
    )

    for (const userId of memberIds) {
      try {
        await pool.execute(
          'INSERT IGNORE INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)',
          [groupId, userId, 1]
        )
      } catch (e) {
        console.error('Failed to add member:', e)
      }
    }

    res.json({ success: true, message: '群组创建成功', data: { groupId, groupName } })
  } catch (error) {
    console.error('Create group error:', error)
    res.status(500).json({ success: false, message: '创建群组失败' })
  }
})

router.get('/groups', verifyToken, async (req, res) => {
  try {
    const userId = req.userId

    const [groups] = await pool.execute(
      `SELECT g.group_id, g.group_name, g.group_avatar, g.group_type, g.is_public, g.created_at,
              (SELECT COUNT(*) FROM group_members WHERE group_id = g.group_id) as member_count
       FROM group_members gm
       JOIN chat_groups g ON gm.group_id = g.group_id
       WHERE gm.user_id = ?
       ORDER BY g.created_at DESC`,
      [userId]
    )

    res.json({ success: true, data: groups })
  } catch (error) {
    console.error('Get groups error:', error)
    res.status(500).json({ success: false, message: '获取群组列表失败' })
  }
})

router.get('/groups/:groupId', verifyToken, async (req, res) => {
  try {
    const { groupId } = req.params
    const userId = req.userId

    const [members] = await pool.execute(
      'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
      [groupId, userId]
    )

    if (members.length === 0) {
      return res.status(403).json({ success: false, message: '您不是群组成员' })
    }

    const [groups] = await pool.execute('SELECT * FROM chat_groups WHERE group_id = ?', [groupId])

    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: '群组不存在' })
    }

    res.json({ success: true, data: groups[0] })
  } catch (error) {
    console.error('Get group error:', error)
    res.status(500).json({ success: false, message: '获取群组信息失败' })
  }
})

router.post('/groups/:groupId/join', verifyToken, async (req, res) => {
  try {
    const { groupId } = req.params
    const userId = req.userId

    const [groups] = await pool.execute('SELECT * FROM chat_groups WHERE group_id = ?', [groupId])
    if (groups.length === 0) {
      return res.status(404).json({ success: false, message: '群组不存在' })
    }

    const group = groups[0]
    if (group.is_public === 2) {
      return res.status(403).json({ success: false, message: '该群组为私密群组，需要邀请才能加入' })
    }

    const [banned] = await pool.execute(
      'SELECT * FROM group_ban_records WHERE group_id = ? AND user_id = ? AND (expired_at IS NULL OR expired_at > NOW())',
      [groupId, userId]
    )
    if (banned.length > 0) {
      return res.status(403).json({ success: false, message: '您已被禁言，无法加入' })
    }

    await pool.execute(
      'INSERT IGNORE INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)',
      [groupId, userId, 1]
    )

    res.json({ success: true, message: '加入群组成功' })
  } catch (error) {
    console.error('Join group error:', error)
    res.status(500).json({ success: false, message: '加入群组失败' })
  }
})

router.post('/groups/:groupId/leave', verifyToken, async (req, res) => {
  try {
    const { groupId } = req.params
    const userId = req.userId

    const [members] = await pool.execute(
      'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
      [groupId, userId]
    )

    if (members.length === 0) {
      return res.status(404).json({ success: false, message: '您不是群组成员' })
    }

    if (members[0].role === 3) {
      return res.status(400).json({ success: false, message: '群主无法退群，请转让群主后再退出' })
    }

    await pool.execute('DELETE FROM group_members WHERE group_id = ? AND user_id = ?', [groupId, userId])

    res.json({ success: true, message: '已退出群组' })
  } catch (error) {
    console.error('Leave group error:', error)
    res.status(500).json({ success: false, message: '退出群组失败' })
  }
})

router.get('/groups/:groupId/members', verifyToken, async (req, res) => {
  try {
    const { groupId } = req.params
    const userId = req.userId

    const [checkMember] = await pool.execute(
      'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
      [groupId, userId]
    )

    if (checkMember.length === 0) {
      return res.status(403).json({ success: false, message: '您不是群组成员' })
    }

    const [members] = await pool.execute(
      `SELECT u.user_id, u.username, u.email, u.avatar, gm.role, gm.joined_at
       FROM group_members gm
       JOIN users u ON gm.user_id = u.user_id
       WHERE gm.group_id = ?
       ORDER BY gm.role DESC, gm.joined_at ASC`,
      [groupId]
    )

    res.json({ success: true, data: members })
  } catch (error) {
    console.error('Get members error:', error)
    res.status(500).json({ success: false, message: '获取群成员列表失败' })
  }
})

router.get('/messages/:groupId', verifyToken, async (req, res) => {
  try {
    const { groupId } = req.params
    const { page = 1, size = 50 } = req.query
    const userId = req.userId

    const [checkMember] = await pool.execute(
      'SELECT * FROM group_members WHERE group_id = ? AND user_id = ?',
      [groupId, userId]
    )

    if (checkMember.length === 0) {
      return res.status(403).json({ success: false, message: '您不是群组成员' })
    }

    const offset = (parseInt(page) - 1) * parseInt(size)

    const [messages] = await pool.execute(
      `SELECT m.message_id, m.group_id, m.sender_id, m.message_type, m.content, m.file_url, m.file_name, m.file_size, m.created_at,
              u.username as sender_username, u.avatar as sender_avatar
       FROM chat_messages m
       JOIN users u ON m.sender_id = u.user_id
       WHERE m.group_id = ? AND m.is_deleted = 0
       ORDER BY m.created_at DESC
       LIMIT ? OFFSET ?`,
      [groupId, parseInt(size), offset]
    )

    res.json({ success: true, data: messages.reverse() })
  } catch (error) {
    console.error('Get messages error:', error)
    res.status(500).json({ success: false, message: '获取消息失败' })
  }
})

router.post('/messages/:groupId', verifyToken, async (req, res) => {
  try {
    const { groupId } = req.params
    const { messageType = 1, content, fileUrl, fileName, fileSize } = req.body
    const senderId = req.userId

    const [checkMember] = await pool.execute(
      'SELECT gm.* FROM group_members gm WHERE gm.group_id = ? AND gm.user_id = ?',
      [groupId, senderId]
    )

    if (checkMember.length === 0) {
      return res.status(403).json({ success: false, message: '您不是群组成员' })
    }

    const [banned] = await pool.execute(
      'SELECT * FROM group_ban_records WHERE group_id = ? AND user_id = ? AND (expired_at IS NULL OR expired_at > NOW())',
      [groupId, senderId]
    )
    if (banned.length > 0) {
      return res.status(403).json({ success: false, message: '您已被禁言，无法发送消息' })
    }

    const [result] = await pool.execute(
      'INSERT INTO chat_messages (group_id, sender_id, message_type, content, file_url, file_name, file_size) VALUES (?, ?, ?, ?, ?, ?, ?)',
      [groupId, senderId, messageType, content, fileUrl, fileName, fileSize]
    )

    const [messages] = await pool.execute(
      `SELECT m.message_id, m.group_id, m.sender_id, m.message_type, m.content, m.file_url, m.file_name, m.file_size, m.created_at,
              u.username as sender_username, u.avatar as sender_avatar
       FROM chat_messages m
       JOIN users u ON m.sender_id = u.user_id
       WHERE m.message_id = ?`,
      [result.insertId]
    )

    const message = messages[0]

    if (req.io) {
      req.io.to(`group_${groupId}`).emit('new_message', message)
    }

    res.json({ success: true, message: '消息发送成功', data: message })
  } catch (error) {
    console.error('Send message error:', error)
    res.status(500).json({ success: false, message: '发送消息失败' })
  }
})

router.delete('/messages/:messageId', verifyToken, async (req, res) => {
  try {
    const { messageId } = req.params
    const userId = req.userId

    const [messages] = await pool.execute('SELECT * FROM chat_messages WHERE message_id = ?', [messageId])

    if (messages.length === 0) {
      return res.status(404).json({ success: false, message: '消息不存在' })
    }

    const message = messages[0]
    const [member] = await pool.execute(
      'SELECT role FROM group_members WHERE group_id = ? AND user_id = ?',
      [message.group_id, userId]
    )

    const canDelete = message.sender_id === userId || (member.length > 0 && member[0].role > 1)

    if (!canDelete) {
      return res.status(403).json({ success: false, message: '您无权删除此消息' })
    }

    await pool.execute('UPDATE chat_messages SET is_deleted = 1 WHERE message_id = ?', [messageId])

    if (req.io) {
      req.io.to(`group_${message.group_id}`).emit('message_deleted', { messageId })
    }

    res.json({ success: true, message: '消息已删除' })
  } catch (error) {
    console.error('Delete message error:', error)
    res.status(500).json({ success: false, message: '删除消息失败' })
  }
})

export default router