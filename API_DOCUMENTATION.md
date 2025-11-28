# **SpeedCalendar 全平台 API 接口文档**

**版本:** 1.1
**基础路径:** `/api`

---

## **目录**

1.  [**通用规范**](#通用规范)
    *   [认证方式](#认证方式)
    *   [统一响应格式](#统一响应格式)
    *   [统一错误码](#统一错误码)
2.  [**认证 (Auth) & 用户 (User)**](#认证-auth--用户-user)
    *   [发送手机验证码](#发送手机验证码)
    *   [手机号登录/注册](#手机号登录注册)
    *   [获取用户信息](#获取用户信息)
    *   [更新用户信息](#更新用户信息)
3.  [**头像 (Avatar)**](#头像-avatar)
    *   [上传头像](#上传头像)
    *   [删除头像](#删除头像)
4.  [**隐私 (Privacy)**](#隐私-privacy)
    *   [获取隐私设置](#获取隐私设置)
    *   [更新隐私设置](#更新隐私设置)
5.  [**日程 (Schedule)**](#日程-schedule)
    *   [获取指定月份的日程](#获取指定月份的日程)
    *   [创建新日程](#创建新日程)
    *   [更新日程](#更新日程)
    *   [删除日程](#删除日程)
6.  [**群组 (Group)**](#群组-group)
    *   [新建群组](#新建群组)
    *   [加入群组](#加入群组)
    *   [获取我加入的群组列表](#获取我加入的群组列表)
7.  [**AI 功能**](#ai-功能)
    *   [OCR 识别](#ocr-识别)
    *   [待开发工具接口](#待开发工具接口)

---

## **通用规范**

### **认证方式**
所有需要用户认证的接口（除非特别说明），都需要在请求头 (Header) 中携带 `Authorization` 字段，值为 `Bearer <用户的JWT Token>`。

### **统一响应格式**
```json
{
  "code": 200,
  "message": "成功/失败信息",
  "data": {
    // 具体业务数据
  }
}
```

### **统一错误码**
| 错误码 | 说明 |
|---|---|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效或缺失） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突（如：用户已在群组中） |
| 413 | 请求体过大 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

---

## **认证 (Auth) & 用户 (User)**

### **发送手机验证码**
- **路径:** `/auth/code`
- **方法:** `POST`
- **认证:** 无需
- **请求体:** `{"phone": "string"}`
- **成功响应:** `{"code": 200, "message": "验证码发送成功", "data": null}`

### **手机号登录/注册**
- **路径:** `/auth/login/phone`
- **方法:** `POST`
- **认证:** 无需
- **请求体:** `{"phone": "string", "code": "string"}`
- **成功响应:**
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "userId": "string",
      "token": "string",
      "refreshToken": "string",
      "expiresIn": 7200,
      "userInfo": { ... }
    }
  }
  ```

### **获取用户信息**
- **路径:** `/auth/user/{userId}`
- **方法:** `GET`
- **认证:** 需要
- **Query参数:** `requesterId` (可选, 用于隐私计算)
- **成功响应:** `{"code": 200, "message": "获取成功", "data": { ... }}`

### **更新用户信息**
- **路径:** `/auth/user/{userId}`
- **方法:** `PUT`
- **认证:** 需要
- **请求体:** `{"username": "string", "avatar": "string"}`
- **成功响应:** `{"code": 200, "message": "更新成功", "data": { ... }}`

---

## **头像 (Avatar)**

### **上传头像**
- **路径:** `/avatar/upload`
- **方法:** `POST`
- **认证:** 需要
- **Content-Type:** `multipart/form-data`
- **表单字段:** `file` (文件), `userId` (字符串)
- **成功响应:** `{"code": 200, "message": "上传成功", "data": {"avatarUrl": "string"}}`

### **删除头像**
- **路径:** `/avatar/{userId}`
- **方法:** `DELETE`
- **认证:** 需要
- **成功响应:** `{"code": 200, "message": "删除成功", "data": null}`

---

## **隐私 (Privacy)**

### **获取隐私设置**
- **路径:** `/privacy/settings/{userId}`
- **方法:** `GET`
- **认证:** 需要
- **成功响应:** `{"code": 200, "message": "获取成功", "data": [{"fieldName": "string", "visibilityLevel": "string"}]}`

### **更新隐私设置**
- **路径:** `/privacy/settings/{userId}`
- **方法:** `PUT`
- **认证:** 需要
- **请求体:** `{"settings": [{"fieldName": "string", "visibilityLevel": "string"}]}`
- **成功响应:** `{"code": 200, "message": "更新成功", "data": null}`

---

## **日程 (Schedule)**

### **获取指定月份的日程**
- **路径:** `/schedules`
- **方法:** `GET`
- **认证:** 需要
- **Query参数:** `year` (Integer), `month` (Integer)
- **成功响应:** `{"code": 200, "message": "获取成功", "data": [{...}]}`

### **创建新日程**
- **路径:** `/schedules`
- **方法:** `POST`
- **认证:** 需要
- **请求体:** `{"title": "string", "scheduleDate": "string", ...}`
- **成功响应:** `{"code": 200, "message": "创建成功", "data": {...}}`

### **更新日程**
- **路径:** `/schedules/{scheduleId}`
- **方法:** `PUT`
- **认证:** 需要
- **请求体:** `{"title": "string", "scheduleDate": "string", ...}`
- **成功响应:** `{"code": 200, "message": "更新成功", "data": {...}}`

### **删除日程**
- **路径:** `/schedules/{scheduleId}`
- **方法:** `DELETE`
- **认证:** 需要
- **成功响应:** `{"code": 200, "message": "删除成功", "data": null}`

---

## **群组 (Group)**

### **新建群组**
- **路径:** `/groups`
- **方法:** `POST`
- **认证:** 需要
- **请求体:** `{"name": "string"}`
- **成功响应:** `{"id": "string", "name": "string", "ownerId": "string"}` (注意: 此处响应格式与通用格式不同)

### **加入群组**
- **路径:** `/groups/{groupId}/join`
- **方法:** `POST`
- **认证:** 需要
- **成功响应:** `{"id": "string", "name": "string", "ownerId": "string"}` (注意: 此处响应格式与通用格式不同)

### **获取我加入的群组列表**
- **路径:** `/groups/my`
- **方法:** `GET`
- **认证:** 需要
- **成功响应:** `[{"groupId": "string", "groupName": "string", "role": "string"}]` (注意: 此处响应格式与通用格式不同)

---

## **AI 功能**

### **OCR 识别**

#### **上传图片并识别文字**
- **路径:** `/ai/ocr/recognize`
- **方法:** `POST`
- **认证:** 需要
- **Content-Type:** `multipart/form-data`
- **表单字段:** `file` (文件), `userId` (字符串), `language` (可选)
- **成功响应:** `{"code": 200, "message": "识别成功", "data": {...}}`

#### **获取OCR识别历史**
- **路径:** `/ai/ocr/history`
- **方法:** `GET`
- **认证:** 需要
- **Query参数:** `userId`, `page`, `pageSize`
- **成功响应:** `{"code": 200, "message": "查询成功", "data": {"total": int, "records": [{...}]}}`

#### **删除OCR识别记录**
- **路径:** `/ai/ocr/history/{recordId}`
- **方法:** `DELETE`
- **认证:** 需要
- **Query参数:** `userId`
- **成功响应:** `{"code": 200, "message": "删除成功", "data": null}`

### **待开发工具接口**
以下接口为未来规划，暂未实现。
- **/ai/translate**: AI翻译
- **/ai/writing**: AI写作助手
- **/ai/image**: AI图像处理
- **/ai/speech**: AI语音识别
- **/ai/chat**: AI对话助手
- **/ai/recommend**: AI智能推荐

---
