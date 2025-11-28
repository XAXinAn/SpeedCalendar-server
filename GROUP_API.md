# **群组 (Group) 功能 API 接口文档**

**基础路径:** `/api`

所有需要用户认证的接口，都需要在请求头 (Header) 中携带 `Authorization` 字段，值为 `Bearer <用户的JWT Token>`。

---

### 1. 新建群组

此接口用于用户创建一个新的群组，创建者自动成为该群组的第一个管理员。

-   **路径:** `/groups`
-   **方法:** `POST`
-   **认证:** 需要用户认证

#### 请求

**请求头 (Headers):**

```
Authorization: Bearer <user_jwt_token>
Content-Type: application/json
```

**请求体 (Body):**

```json
{
  "name": "string"
}
```

-   `name`: (string, 必需) - 新群组的名称。

**请求示例:**

```json
{
  "name": "家庭共享日历"
}
```

#### 响应

**成功响应 (200 OK):**

返回新建的群组对象。

```json
{
  "id": "string",      // 群组的唯一ID
  "name": "string",    // 群组名称
  "ownerId": "string"  // 创建者（群主）的用户ID
}
```

**失败响应:**

-   `400 Bad Request`: 请求体不合法（如 `name` 字段缺失）。
-   `401 Unauthorized`: 用户未登录或Token无效。

---

### 2. 加入群组

此接口用于用户通过群组ID加入一个已经存在的群组。

-   **路径:** `/groups/{groupId}/join`
-   **方法:** `POST`
-   **认证:** 需要用户认证

#### 请求

**路径参数 (Path Parameters):**

-   `groupId`: (string, 必需) - 要加入的群组的唯一ID。

**请求头 (Headers):**

```
Authorization: Bearer <user_jwt_token>
```

**请求体 (Body):**
无

#### 响应

**成功响应 (200 OK):**

返回用户成功加入的那个群组对象。

```json
{
  "id": "string",
  "name": "string",
  "ownerId": "string"
}
```

**失败响应:**

-   `401 Unauthorized`: 用户未登录或Token无效。
-   `404 Not Found`: 具有该 `groupId` 的群组不存在。
-   `409 Conflict`: 用户已经是该群组的成员。

---

### 3. 获取我加入的群组列表

此接口用于获取当前登录用户所属的所有群组列表。

-   **路径:** `/groups/my`
-   **方法:** `GET`
-   **认证:** 需要用户认证

#### 请求

**请求头 (Headers):**

```
Authorization: Bearer <user_jwt_token>
```

**请求体 (Body):**
无

#### 响应

**成功响应 (200 OK):**

返回一个数组，数组的每一项都代表一个用户所属的群组及其在该群组中的角色。

```json
[
  {
    "groupId": "string",
    "groupName": "string",
    "role": "string"
  }
]
```

-   `groupId`: (string) - 群组的唯一ID。
-   `groupName`: (string) - 群组的名称。
-   `role`: (string) - 用户在该群组中的角色，值为 `"admin"` 或 `"member"`。

**响应示例:**

```json
[
  {
    "groupId": "grp_aBcDeF123",
    "groupName": "家庭共享日历",
    "role": "admin"
  },
  {
    "groupId": "grp_XyZ789",
    "groupName": "公司项目A组",
    "role": "member"
  }
]
```

**失败响应:**

-   `401 Unauthorized`: 用户未登录或Token无效。

---
