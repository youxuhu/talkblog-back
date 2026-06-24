import jwt from 'jsonwebtoken'

export function verifyToken(req, res, next) {
  const authHeader = req.headers.authorization

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, message: '请先登录' })
  }

  const token = authHeader.substring(7)

  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET)
    req.userId = decoded.userId
    req.username = decoded.username
    req.roles = decoded.roles
    next()
  } catch (error) {
    return res.status(401).json({ success: false, message: '登录已过期，请重新登录' })
  }
}

export function isAdmin(req, res, next) {
  if (!req.roles || !req.roles.some((r) => ['ADMIN', 'SUPER_ADMIN'].includes(r))) {
    return res.status(403).json({ success: false, message: '需要管理员权限' })
  }
  next()
}

export function isGroupAdmin(req, res, next) {
  next()
}

export function isGroupOwner(req, res, next) {
  next()
}