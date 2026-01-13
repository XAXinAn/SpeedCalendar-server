# 手机号密码登录功能接口文档

## 接口详情

### 📱 手机号密码登录

*   **接口地址**: `/api/auth/login`
*   **请求方法**: `POST`
*   **Content-Type**: `application/json`

#### 1. 请求参数 (Body)

```json
{
  "phone": "13800138000",  // 必填，11位手机号
  "password": "your_password" // 必填，用户密码
}
```

#### 2. 成功响应 (200 OK)

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "userId": "user_123456789",
    "token": "eyJhbGciOiJIUzI1NiJ9...",      // JWT Access Token，后续请求需放在 Header: Authorization: Bearer {token}
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...", // 用于刷新 Token
    "expiresIn": 7200,                       // Token 有效期（秒）
    "userInfo": {                            // 用户基本信息
      "userId": "user_123456789",
      "username": "SpeedUser",
      "phone": "13800138000",
      "avatar": "http://...",
      "role": "user"
    }
  }
}
```

#### 3. 失败响应示例

*   **参数错误 (400)**:

```json
{
  "code": 400,
  "message": "手机号格式不正确",
  "data": null
}
```

*   **密码错误或用户不存在**:

```json
{
  "code": 500,
  "message": "用户名或密码错误",
  "data": null
}
```

#### 💡 前端开发提示

1.  **表单验证**: 提交前请校验手机号格式（`^1[3-9]\d{9}$`）和密码非空。
2.  **Token 存储**: 登录成功后，请将 `data.token` 保存到本地（如 `localStorage`），并在后续所有需要认证的接口请求头中携带：`Authorization: Bearer <token>`。
3.  **自动登录**: 可以利用 `refreshToken` 实现 Token 过期后的无感刷新。
