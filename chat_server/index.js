import express from 'express'
import { createServer } from 'http'
import { Server } from 'socket.io'
import cors from 'cors'
import dotenv from 'dotenv'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'
import multer from 'multer'
import { initDatabase } from './config/database.js'
import authRoutes from './routes/auth.js'
import chatRoutes from './routes/chat.js'
import adminChatRoutes from './routes/adminChat.js'

dotenv.config()

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const app = express()
const httpServer = createServer(app)
const io = new Server(httpServer, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST'],
  },
})

const uploadDir = join(__dirname, process.env.UPLOAD_DIR || './uploads')

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, uploadDir),
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9)
    cb(null, uniqueSuffix + '-' + file.originalname)
  },
})

const upload = multer({
  storage,
  limits: {
    fileSize: parseInt(process.env.MAX_FILE_SIZE) || 10 * 1024 * 1024,
  },
})

app.use(cors())
app.use(express.json())
app.use(express.urlencoded({ extended: true }))
app.use('/uploads', express.static(uploadDir))

app.use((req, res, next) => {
  req.io = io
  next()
})

app.use('/api/auth', authRoutes)
app.use('/api/chat', chatRoutes)
app.use('/api/admin/chat', adminChatRoutes)

const userSockets = new Map()
const socketUsers = new Map()

io.on('connection', (socket) => {
  console.log('User connected:', socket.id)

  socket.on('authenticate', ({ userId, username }) => {
    userSockets.set(userId, socket.id)
    socketUsers.set(socket.id, { userId, username })
    socket.join(`user_${userId}`)
    console.log(`User ${username} (ID: ${userId}) authenticated`)
  })

  socket.on('join_group', ({ groupId, userId }) => {
    socket.join(`group_${groupId}`)
    socket.to(`group_${groupId}`).emit('user_joined', {
      groupId,
      userId,
      timestamp: new Date().toISOString(),
    })
  })

  socket.on('leave_group', ({ groupId, userId }) => {
    socket.leave(`group_${groupId}`)
    socket.to(`group_${groupId}`).emit('user_left', {
      groupId,
      userId,
      timestamp: new Date().toISOString(),
    })
  })

  socket.on('send_message', (data) => {
    const { groupId, message } = data
    io.to(`group_${groupId}`).emit('new_message', message)
  })

  socket.on('typing', (data) => {
    const { groupId, userId, username, isTyping } = data
    socket.to(`group_${groupId}`).emit('user_typing', {
      groupId,
      userId,
      username,
      isTyping,
    })
  })

  socket.on('disconnect', () => {
    const userInfo = socketUsers.get(socket.id)
    if (userInfo) {
      userSockets.delete(userInfo.userId)
      socketUsers.delete(socket.id)
    }
    console.log('User disconnected:', socket.id)
  })
})

const PORT = parseInt(process.env.PORT) || 3001

async function startServer() {
  try {
    await initDatabase()
    httpServer.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`)
      console.log(`Upload directory: ${uploadDir}`)
    })
  } catch (error) {
    console.error('Failed to start server:', error)
    process.exit(1)
  }
}

startServer()