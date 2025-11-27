# 前端开发指南 (Frontend Development Guide)

本文档旨在为前端开发人员提供一份清晰的指南，确保在开发和修改前端页面时，不会对后端已有功能造成任何意外影响。

## 黄金法则

1.  **遵守 API 契约**: 前端与后端的交互 **必须** 且 **只能** 通过本文档中定义的 API 接口进行。将后端视为一个提供数据的黑盒服务。
2.  **禁止直接操作**: **严禁** 任何绕过 API 的操作，例如直接访问或修改数据库、调用未公开的后端内部服务等。
3.  **API 版本与前缀**: 所有 API 均以 `/api` 作为前缀。本文档描述的是当前版本的 API，任何变更都应先在后端完成并更新此文档。
4.  **关注点分离**: 前端的职责是构建用户界面 (UI)、管理前端状态，并通过 API 与后端进行数据交换。

---

## API 接口详解

以下是后端当前提供的所有可用 API 接口的详细列表。

### 1. 认证 (Authentication)

- **Controller**: `AuthController`
- **基础路径**: `/api/auth`

#### 1.1 发送验证码

- **功能**: 向指定手机号发送登录/注册验证码。
- **Endpoint**: `POST /api/auth/code`
- **请求体 (Request Body)**:
  ```json
  {
    "phone": "13800138000"
  }
  ```
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "验证码发送成功",
    "data": null
  }
  ```

#### 1.2 手机号登录

- **功能**: 使用手机号和验证码进行登录。
- **Endpoint**: `POST /api/auth/login/phone`
- **请求体 (Request Body)**:
  ```json
  {
    "phone": "13800138000",
    "code": "123456"
  }
  ```
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "userId": "用户唯一ID",
      "token": "jwt-access-token",
      "refreshToken": "jwt-refresh-token",
      "expiresIn": 7200,
      "userInfo": {
        "userId": "...",
        "username": "...",
        "avatar": "...",
        "phone": "..."
      }
    }
  }
  ```

#### 1.3 更新用户信息

- **功能**: 更新用户的昵称或头像 URL。
- **Endpoint**: `PUT /api/auth/user/{userId}`
- **URL 参数**: `userId` - 需要更新的用户的 ID。
- **请求体 (Request Body)**:
  ```json
  {
    "username": "新的用户昵称",
    "avatar": "新的头像URL"
  }
  ```
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": {
      "userId": "...",
      "username": "新的用户昵称",
      "avatar": "新的头像URL",
      "phone": "..."
    }
  }
  ```

#### 1.4 获取用户信息

- **功能**: 获取指定用户的信息，会根据隐私设置过滤字段。
- **Endpoint**: `GET /api/auth/user/{userId}`
- **URL 参数**:
    - `userId`: 被查看者的用户 ID。
    - `requesterId` (可选): 请求者的用户 ID。如果提供，后端会根据被查看者的隐私设置决定返回哪些信息。
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "userId": "...",
      "username": "...",
      "avatar": "...",
      "phone": "138****0000" // 示例：号码可能被脱敏
    }
  }
  ```

---

### 2. 头像管理 (Avatar Management)

- **Controller**: `AvatarController`
- **基础路径**: `/api/avatar`

#### 2.1 上传头像

- **功能**: 为指定用户上传新的头像文件。
- **Endpoint**: `POST /api/avatar/upload`
- **请求类型**: `multipart/form-data`
- **表单字段**:
    - `file`: 图像文件本身。
    - `userId`: 该头像所属的用户 ID。
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "上传成功",
    "data": {
      "avatarUrl": "https://<domain>/api/files/avatars/<generated-filename>.jpg"
    }
  }
  ```
- **注意**: 响应中的 `avatarUrl` 是完整的、可直接访问的 URL，前端应直接使用此 URL 来显示头像。

#### 2.2 删除头像

- **功能**: 删除用户的自定义头像，恢复为系统默认头像。
- **Endpoint**: `DELETE /api/avatar/{userId}`
- **URL 参数**: `userId` - 用户 ID。
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

---

### 3. 隐私设置 (Privacy Settings)

- **Controller**: `PrivacyController`
- **基础路径**: `/api/privacy`

#### 3.1 获取隐私设置

- **功能**: 获取指定用户的所有隐私配置项。
- **Endpoint**: `GET /api/privacy/settings/{userId}`
- **URL 参数**: `userId` - 用户 ID。
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": [
      {
        "fieldName": "phone",
        "visibilityLevel": "PRIVATE"
      },
      {
        "fieldName": "realName",
        "visibilityLevel": "FRIENDS_ONLY"
      }
    ]
  }
  ```
  - `visibilityLevel` 的可能值: `PUBLIC`, `FRIENDS_ONLY`, `PRIVATE`。

#### 3.2 更新隐私设置

- **功能**: 批量更新用户的隐私设置。
- **Endpoint**: `PUT /api/privacy/settings/{userId}`
- **URL 参数**: `userId` - 用户 ID。
- **请求体 (Request Body)**:
  ```json
  {
    "settings": [
      {
        "fieldName": "phone",
        "visibilityLevel": "PRIVATE"
      },
      {
        "fieldName": "email",
        "visibilityLevel": "FRIENDS_ONLY"
      }
    ]
  }
  ```
- **成功响应 (Success Response)**:
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

---

### 4. 文件访问 (File Access) - 本地开发

- **Controller**: `FileController`
- **基础路径**: `/api/files`
- **注意**: 此接口主要用于 **本地开发环境**，用于直接访问存储在服务器本地的静态文件。在生产环境中，文件可能由 OSS 或 CDN 提供服务，前端应始终使用由其他 API (如“上传头像”接口) 返回的完整 URL。

#### 4.1 访问头像文件

- **功能**: 获取存储在本地的头像图片。
- **Endpoint**: `GET /api/files/avatars/{filename}`
- **URL 参数**: `filename` - 文件名 (通常由 `avatarUrl` 中解析得到)。
- **响应**: 直接返回图片文件流。
