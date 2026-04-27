# TalkBlog Back API 文档（Apifox 调试版）

## 1. 基本信息

- 服务地址：`http://localhost:8080`
- 数据格式：`application/json`
- 认证方式：当前接口未使用鉴权头（如 `Authorization`）

## 2. 接口分组

- 认证模块：`/api/auth`
- 人脸向量模块：`/api/face`
- 健康检查：`/actuator/health`

---

## 3. 认证模块

### 3.1 用户注册

- 方法：`POST`
- 路径：`/api/auth/register`
- 完整 URL：`http://localhost:8080/api/auth/register`
- 请求头：
  - `Content-Type: application/json`

#### 请求体（JSON）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| email | string | 是 | 用户邮箱（唯一标识） |
| username | string | 是 | 用户名 |
| password | string | 是 | 明文密码（后端会加密存储） |
| image | string | 是 | 人脸图片的 base64 字符串 |

#### 请求示例

```json
{
  "email": "user@example.com",
  "username": "revy",
  "password": "123456",
  "image": "data:image/jpeg;base64,/9j/4AAQSk..."
}
```

#### 成功响应示例（200）

```json
{
  "success": true,
  "message": "Registration successful"
}
```

#### 失败响应示例（200，业务失败）

```json
{
  "success": false,
  "message": "Email already exists"
}
```

#### 失败响应示例（400，参数/业务校验异常）

```json
{
  "success": false,
  "message": "具体错误信息"
}
```

---

### 3.2 人脸登录

- 方法：`POST`
- 路径：`/api/auth/login`
- 完整 URL：`http://localhost:8080/api/auth/login`
- 请求头：
  - `Content-Type: application/json`

#### 请求体（JSON）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| email | string | 是 | 用户邮箱 |
| image | string | 是 | 人脸图片 base64 字符串 |

#### 请求示例

```json
{
  "email": "user@example.com",
  "image": "data:image/jpeg;base64,/9j/4AAQSk..."
}
```

#### 成功响应示例（200）

```json
{
  "success": true,
  "message": "Login successful"
}
```

#### 失败响应示例（200，业务失败）

```json
{
  "success": false,
  "message": "Email or face not matched"
}
```

#### 失败响应示例（400，参数/业务校验异常）

```json
{
  "success": false,
  "message": "具体错误信息"
}
```

---

## 4. 人脸向量模块

### 4.1 注册人脸向量

- 方法：`POST`
- 路径：`/api/face/register`
- 完整 URL：`http://localhost:8080/api/face/register`
- 请求头：
  - `Content-Type: application/json`

#### 请求体（JSON）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| vectorId | number(int64) | 否 | 向量记录 ID |
| userId | number(int64) | 是 | 用户 ID |
| faceVector | number[] | 是 | 人脸向量数组（float 数组） |
| faceImageUrl | string | 否 | 人脸图片 URL |
| createdAt | string(date-time) | 否 | 创建时间 |
| updatedAt | string(date-time) | 否 | 更新时间 |

#### 请求示例

```json
{
  "userId": 1,
  "faceVector": [0.12, -0.08, 0.34, 0.91],
  "faceImageUrl": "https://example.com/face/1.jpg"
}
```

#### 成功响应示例（200）

```text
Face vector registered successfully!
```

---

### 4.2 根据用户 ID 查询人脸向量

- 方法：`GET`
- 路径：`/api/face/user/{userId}`
- 完整 URL：`http://localhost:8080/api/face/user/1`
- 路径参数：
  - `userId`：number(int64)，用户 ID

#### 成功响应示例（200）

```json
{
  "vectorId": 100,
  "userId": 1,
  "faceVector": [0.12, -0.08, 0.34, 0.91],
  "faceImageUrl": "https://example.com/face/1.jpg",
  "createdAt": "2026-04-18T10:30:00",
  "updatedAt": "2026-04-18T10:30:00"
}
```

---

### 4.3 人脸向量匹配

- 方法：`POST`
- 路径：`/api/face/match`
- 完整 URL：`http://localhost:8080/api/face/match`
- 请求头：
  - `Content-Type: application/json`

#### 请求体（JSON）

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| face_vector | number[] | 是 | 待匹配的人脸向量数组 |

#### 请求示例

```json
{
  "face_vector": [0.11, -0.05, 0.29, 0.88]
}
```

#### 成功响应示例（200）

```json
{
  "vectorId": 100,
  "userId": 1,
  "faceVector": [0.12, -0.08, 0.34, 0.91],
  "faceImageUrl": "https://example.com/face/1.jpg",
  "createdAt": "2026-04-18T10:30:00",
  "updatedAt": "2026-04-18T10:30:00"
}
```

#### 失败响应说明

- 当 `face_vector` 不是数组时，会抛出参数异常（通常表现为 4xx/5xx，取决于全局异常处理配置）。

---

## 5. 健康检查接口

### 5.1 服务健康检查

- 方法：`GET`
- 路径：`/actuator/health`
- 完整 URL：`http://localhost:8080/actuator/health`

#### 成功响应示例（200）

```json
{
  "status": "UP"
}
```

---

## 6. Apifox 导入/调试建议

1. 在 Apifox 新建项目，环境变量 `baseUrl` 设置为 `http://localhost:8080`。
2. 按以上分组创建接口（Auth、Face、Actuator）。
3. 所有 POST 接口的 Body 类型选择 `JSON`。
4. `faceVector` / `face_vector` 字段务必传数组，不要传字符串。
5. 登录与注册的 `image` 传 base64 字符串即可（可先用短字符串联调接口流程）。
