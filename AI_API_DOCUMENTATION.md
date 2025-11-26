# SpeedCalendar AI功能接口文档

## 目录
- [1. OCR识别接口](#1-ocr识别接口)
- [2. 通用AI工具接口规范](#2-通用ai工具接口规范)
- [3. 待开发工具接口预留](#3-待开发工具接口预留)

---

## 1. OCR识别接口

### 1.1 上传图片并识别文字

**接口描述**：上传图片，返回识别出的文字内容

**请求方式**：`POST`

**接口路径**：`/api/ai/ocr/recognize`

**Content-Type**：`multipart/form-data`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 图片文件（支持 jpg, png, jpeg, bmp）|
| userId | String | 是 | 用户ID |
| language | String | 否 | 识别语言（默认：auto，可选：zh-CN, en-US） |

**请求示例**：
```http
POST /api/ai/ocr/recognize
Content-Type: multipart/form-data

file: [图片文件]
userId: "d60347da-da6f-4d3e-8adc-0bcdbba53692"
language: "auto"
```

**响应格式**：
```json
{
  "code": 200,
  "message": "识别成功",
  "data": {
    "text": "识别出的完整文字内容",
    "confidence": 0.95,
    "language": "zh-CN",
    "words": [
      {
        "text": "单个词语",
        "confidence": 0.98,
        "boundingBox": {
          "x": 100,
          "y": 200,
          "width": 50,
          "height": 30
        }
      }
    ],
    "processTime": 1234,
    "imageUrl": "http://localhost:8080/api/files/ocr/xxx.jpg"
  }
}
```

**响应字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| text | String | 识别出的完整文字 |
| confidence | Float | 整体置信度（0-1） |
| language | String | 检测到的语言 |
| words | Array | 单词/字符详细信息数组 |
| words[].text | String | 单个词语内容 |
| words[].confidence | Float | 单词置信度 |
| words[].boundingBox | Object | 文字位置坐标 |
| processTime | Long | 处理耗时（毫秒） |
| imageUrl | String | 原图保存地址（可选） |

**错误响应**：
```json
{
  "code": 400,
  "message": "文件格式不支持",
  "data": null
}
```

**常见错误码**：

| 错误码 | 说明 |
|--------|------|
| 400 | 请求参数错误（文件为空、格式不支持等） |
| 413 | 文件过大（超过10MB） |
| 500 | OCR服务异常 |
| 503 | OCR服务不可用 |

---

### 1.2 获取OCR识别历史

**接口描述**：获取用户的OCR识别历史记录

**请求方式**：`GET`

**接口路径**：`/api/ai/ocr/history`

**请求参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID |
| page | Integer | 否 | 页码（默认：1） |
| pageSize | Integer | 否 | 每页数量（默认：20，最大100） |

**请求示例**：
```http
GET /api/ai/ocr/history?userId=xxx&page=1&pageSize=20
```

**响应格式**：
```json
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "total": 50,
    "page": 1,
    "pageSize": 20,
    "records": [
      {
        "id": "record-id-1",
        "userId": "user-id",
        "imageUrl": "http://localhost:8080/api/files/ocr/xxx.jpg",
        "text": "识别内容",
        "confidence": 0.95,
        "language": "zh-CN",
        "createdAt": "2025-11-18T19:30:00"
      }
    ]
  }
}
```

---

### 1.3 删除OCR识别记录

**接口描述**：删除指定的OCR识别记录

**请求方式**：`DELETE`

**接口路径**：`/api/ai/ocr/history/{recordId}`

**路径参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| recordId | String | 是 | 记录ID |

**Query参数**：

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | String | 是 | 用户ID（验证权限） |

**请求示例**：
```http
DELETE /api/ai/ocr/history/record-id-1?userId=user-id
```

**响应格式**：
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

---

## 2. 通用AI工具接口规范

所有AI工具接口遵循以下规范：

### 2.1 统一响应格式

```json
{
  "code": 200,
  "message": "成功/失败信息",
  "data": {
    // 具体数据
  }
}
```

### 2.2 统一错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（Token无效） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 413 | 请求体过大 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

### 2.3 文件上传限制

- **图片文件**：最大10MB，支持格式：jpg, jpeg, png, bmp, webp
- **文档文件**：最大20MB，支持格式：pdf, doc, docx, txt
- **音频文件**：最大50MB，支持格式：mp3, wav, m4a
- **视频文件**：最大100MB，支持格式：mp4, avi, mov

### 2.4 速率限制

- **普通用户**：每分钟最多10次请求
- **VIP用户**：每分钟最多50次请求

超出限制返回 `429 Too Many Requests`

---

## 3. 待开发工具接口预留

### 3.1 AI翻译工具

**接口路径**：`/api/ai/translate`

**功能说明**：
- 支持多语言互译
- 支持文本、图片、语音翻译
- 实时翻译

**接口预留**：
- `POST /api/ai/translate/text` - 文本翻译
- `POST /api/ai/translate/image` - 图片翻译
- `POST /api/ai/translate/voice` - 语音翻译
- `GET /api/ai/translate/languages` - 获取支持的语言列表

---

### 3.2 AI写作助手

**接口路径**：`/api/ai/writing`

**功能说明**：
- 文章续写
- 文案优化
- 错别字检查
- 语法纠正

**接口预留**：
- `POST /api/ai/writing/continue` - 文章续写
- `POST /api/ai/writing/optimize` - 文案优化
- `POST /api/ai/writing/check` - 错别字检查
- `POST /api/ai/writing/grammar` - 语法纠正

---

### 3.3 AI图像处理

**接口路径**：`/api/ai/image`

**功能说明**：
- 图片智能裁剪
- 图片增强
- 背景移除
- 图片风格转换

**接口预留**：
- `POST /api/ai/image/crop` - 智能裁剪
- `POST /api/ai/image/enhance` - 图片增强
- `POST /api/ai/image/remove-bg` - 背景移除
- `POST /api/ai/image/style-transfer` - 风格转换

---

### 3.4 AI语音识别

**接口路径**：`/api/ai/speech`

**功能说明**：
- 语音转文字
- 语音情绪分析
- 语音合成

**接口预留**：
- `POST /api/ai/speech/recognize` - 语音识别
- `POST /api/ai/speech/emotion` - 情绪分析
- `POST /api/ai/speech/synthesize` - 语音合成

---

### 3.5 AI对话助手

**接口路径**：`/api/ai/chat`

**功能说明**：
- 智能问答
- 上下文对话
- 知识库问答

**接口预留**：
- `POST /api/ai/chat/send` - 发送消息
- `GET /api/ai/chat/history` - 对话历史
- `DELETE /api/ai/chat/clear` - 清除对话

---

### 3.6 AI智能推荐

**接口路径**：`/api/ai/recommend`

**功能说明**：
- 个性化推荐
- 日程智能安排
- 习惯分析

**接口预留**：
- `GET /api/ai/recommend/schedule` - 日程推荐
- `GET /api/ai/recommend/events` - 活动推荐
- `GET /api/ai/recommend/analysis` - 行为分析

---

## 4. 后端实现建议

### 4.1 技术选型

**OCR服务**：
- **方案1**：百度AI OCR（推荐，国内稳定）
- **方案2**：腾讯云OCR
- **方案3**：阿里云OCR
- **方案4**：Tesseract（开源，需自行部署）

**存储方案**：
- 开发环境：本地文件系统
- 生产环境：阿里云OSS

### 4.2 数据库设计

**ocr_records 表**：
```sql
CREATE TABLE ocr_records (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    image_url VARCHAR(500),
    original_image_url VARCHAR(500),
    recognized_text TEXT,
    confidence DECIMAL(5,4),
    language VARCHAR(20),
    words_detail JSON,
    process_time INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**ai_tool_usage 表**（工具使用统计）：
```sql
CREATE TABLE ai_tool_usage (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    tool_id VARCHAR(50) NOT NULL,
    usage_count INT DEFAULT 0,
    last_used_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_tool (user_id, tool_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 4.3 配置文件示例

在 `application.yml` 中添加：

```yaml
# AI服务配置
ai:
  ocr:
    provider: baidu # baidu, tencent, aliyun, tesseract
    api-key: ${OCR_API_KEY:}
    secret-key: ${OCR_SECRET_KEY:}
    max-file-size: 10485760 # 10MB
    allowed-types: jpg,jpeg,png,bmp,webp
    rate-limit:
      normal-user: 10 # 每分钟10次
      vip-user: 50 # 每分钟50次

  storage:
    type: local # local 或 oss
    local:
      upload-dir: D:/speedcalendar/uploads/ocr
      base-url: http://localhost:8080/api/files/ocr
```

### 4.4 Controller 示例框架

```java
@RestController
@RequestMapping("/ai/ocr")
@Slf4j
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping("/recognize")
    public ApiResponse<OcrResult> recognize(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam(value = "language", defaultValue = "auto") String language
    ) {
        // 实现OCR识别逻辑
    }

    @GetMapping("/history")
    public ApiResponse<PageResult<OcrRecord>> getHistory(
            @RequestParam("userId") String userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize
    ) {
        // 实现历史查询逻辑
    }

    @DeleteMapping("/history/{recordId}")
    public ApiResponse<Void> deleteRecord(
            @PathVariable String recordId,
            @RequestParam("userId") String userId
    ) {
        // 实现删除逻辑
    }
}
```

---

## 5. 前端调用示例

### 5.1 创建 OcrApiService

```kotlin
interface OcrApiService {
    @Multipart
    @POST("ai/ocr/recognize")
    suspend fun recognizeImage(
        @Part("userId") userId: RequestBody,
        @Part file: MultipartBody.Part,
        @Part("language") language: RequestBody
    ): Response<ApiResponse<OcrResult>>

    @GET("ai/ocr/history")
    suspend fun getHistory(
        @Query("userId") userId: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): Response<ApiResponse<PageResult<OcrRecord>>>

    @DELETE("ai/ocr/history/{recordId}")
    suspend fun deleteRecord(
        @Path("recordId") recordId: String,
        @Query("userId") userId: String
    ): Response<ApiResponse<Void>>
}
```

### 5.2 数据模型

```kotlin
data class OcrResult(
    val text: String,
    val confidence: Float,
    val language: String,
    val words: List<OcrWord>,
    val processTime: Long,
    val imageUrl: String?
)

data class OcrWord(
    val text: String,
    val confidence: Float,
    val boundingBox: BoundingBox
)

data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
```

---

## 6. 测试计划

### 6.1 功能测试

- [ ] 上传图片并识别文字
- [ ] 识别中文文字
- [ ] 识别英文文字
- [ ] 识别混合语言
- [ ] 查询识别历史
- [ ] 删除识别记录
- [ ] 文件格式验证
- [ ] 文件大小限制
- [ ] 速率限制

### 6.2 性能测试

- [ ] 单个请求响应时间 < 3秒
- [ ] 并发10个请求
- [ ] 并发50个请求
- [ ] 大图片（8MB）识别
- [ ] 小图片（100KB）识别

### 6.3 异常测试

- [ ] 空文件上传
- [ ] 非图片文件上传
- [ ] 超大文件上传（>10MB）
- [ ] 无效的用户ID
- [ ] OCR服务异常
- [ ] 网络超时

---

## 7. 部署说明

### 7.1 环境变量配置

```bash
# OCR服务配置
export OCR_API_KEY="your-api-key"
export OCR_SECRET_KEY="your-secret-key"

# 文件存储配置（生产环境）
export FILE_STORAGE_TYPE="oss"
export OSS_ACCESS_KEY="your-oss-key"
export OSS_SECRET_KEY="your-oss-secret"
```

### 7.2 依赖包

```xml
<!-- pom.xml -->
<dependencies>
    <!-- 百度OCR SDK -->
    <dependency>
        <groupId>com.baidu.aip</groupId>
        <artifactId>java-sdk</artifactId>
        <version>4.16.8</version>
    </dependency>

    <!-- 阿里云OSS -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
        <version>3.15.0</version>
    </dependency>
</dependencies>
```

---

## 8. 注意事项

1. **安全性**：
   - 所有接口需要Token认证
   - 文件上传需要验证用户权限
   - 敏感信息（API Key）使用环境变量

2. **性能优化**：
   - 图片压缩后再上传到OCR服务
   - 使用异步处理大批量识别
   - 添加Redis缓存识别结果

3. **成本控制**：
   - 限制每个用户每日调用次数
   - 大图片自动压缩
   - 缓存相同图片的识别结果

4. **用户体验**：
   - 显示识别进度
   - 识别失败提供重试机制
   - 支持批量识别

---

**文档版本**：v1.0
**更新日期**：2025-11-18
**维护人员**：SpeedCalendar Team
