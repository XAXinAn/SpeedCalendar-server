# 生产环境OSS迁移指南

本文档列出了从开发环境（本地存储）迁移到生产环境（阿里云OSS）时需要修改的所有地方。

## 📋 迁移检查清单

### 1. 添加OSS依赖

**文件：** `pom.xml`

在 `<dependencies>` 中添加：

```xml
<!-- 阿里云OSS SDK -->
<dependency>
    <groupId>com.aliyun.oss</groupId>
    <artifactId>aliyun-sdk-oss</artifactId>
    <version>3.17.1</version>
</dependency>
```

---

### 2. 修改配置文件

**文件：** `src/main/resources/application.yml`

#### 方式一：直接修改主配置（不推荐）
```yaml
file:
  storage:
    type: oss  # 改为 oss
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com  # 填写实际endpoint
      bucket: speedcalendar-avatars  # 填写bucket名称
      access-key: ${OSS_ACCESS_KEY}  # 从环境变量读取
      secret-key: ${OSS_SECRET_KEY}  # 从环境变量读取
      cdn-domain: https://cdn.speedcalendar.com  # 可选：CDN域名
```

#### 方式二：使用生产环境配置（推荐）
取消 `application.yml` 中第147-155行的注释并填写实际配置：

```yaml
# 生产环境配置
---
spring:
  config:
    activate:
      on-profile: prod

file:
  storage:
    type: oss
    oss:
      endpoint: oss-cn-hangzhou.aliyuncs.com
      bucket: speedcalendar-avatars
      access-key: ${OSS_ACCESS_KEY}
      secret-key: ${OSS_SECRET_KEY}
      cdn-domain: https://cdn.speedcalendar.com
```

然后启动时使用：`java -jar app.jar --spring.profiles.active=prod`

---

### 3. 实现OSS服务

**文件：** `src/main/java/com/example/speedcalendarserver/service/OSSFileStorageService.java`

当前这个文件只是框架，需要完整实现以下方法：

#### 3.1 添加OSS客户端初始化

```java
private OSS ossClient;

@PostConstruct
public void init() {
    this.ossClient = new OSSClientBuilder().build(
        config.getOss().getEndpoint(),
        config.getOss().getAccessKey(),
        config.getOss().getSecretKey()
    );
    log.info("【OSS客户端】初始化成功");
}

@PreDestroy
public void destroy() {
    if (ossClient != null) {
        ossClient.shutdown();
        log.info("【OSS客户端】已关闭");
    }
}
```

#### 3.2 实现uploadAvatar方法

```java
@Override
public String uploadAvatar(MultipartFile file, String userId) throws Exception {
    validateFile(file);

    // 生成文件名
    String originalFilename = file.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
    String filename = userId + "_" + System.currentTimeMillis() + extension;
    String objectKey = "avatars/" + filename;

    // 上传到OSS
    ossClient.putObject(
        config.getOss().getBucket(),
        objectKey,
        file.getInputStream()
    );

    // 返回URL
    if (config.getOss().getCdnDomain() != null) {
        return config.getOss().getCdnDomain() + "/" + objectKey;
    } else {
        return "https://" + config.getOss().getBucket() + "." +
               config.getOss().getEndpoint() + "/" + objectKey;
    }
}
```

#### 3.3 实现deleteAvatar方法

```java
@Override
public void deleteAvatar(String fileUrl) throws Exception {
    if (fileUrl == null || fileUrl.isEmpty()) {
        return;
    }

    // 从URL提取objectKey
    String objectKey;
    if (fileUrl.contains("/avatars/")) {
        objectKey = "avatars/" + fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    } else {
        return;
    }

    // 删除OSS对象
    ossClient.deleteObject(config.getOss().getBucket(), objectKey);
}
```

---

### 4. 配置环境变量

在服务器上设置环境变量：

```bash
export OSS_ACCESS_KEY="your-access-key"
export OSS_SECRET_KEY="your-secret-key"
```

或在Docker中：

```yaml
environment:
  - OSS_ACCESS_KEY=your-access-key
  - OSS_SECRET_KEY=your-secret-key
```

---

### 5. 创建OSS Bucket

1. 登录阿里云OSS控制台
2. 创建Bucket：`speedcalendar-avatars`
3. 设置权限：私有读写
4. （可选）配置CDN加速
5. （可选）配置CORS规则（如果前端直传）

---

### 6. 删除本地存储相关代码（可选）

生产环境不需要 `FileController`，可以：

- 保留（不影响功能）
- 或删除 `FileController.java`

因为已经添加了 `@ConditionalOnProperty`，当 `type=oss` 时不会加载。

---

### 7. 测试迁移

1. 启动应用并检查日志：
   ```
   【OSS客户端】初始化成功
   ```

2. 上传一张测试图片

3. 检查：
   - 数据库中的 `avatar` 字段是否为OSS URL
   - OSS控制台中是否能看到上传的文件
   - 前端是否能正常显示

---

## 🔍 排查TODO标记

使用以下命令查找所有需要修改的地方：

```bash
cd SpeedCalendar-server
grep -r "TODO.*生产环境" --include="*.java" --include="*.yml"
```

或在IDE中搜索：`TODO.*生产环境`

---

## 📝 配置总结

| 配置项 | 开发环境 | 生产环境 |
|--------|----------|----------|
| `file.storage.type` | `local` | `oss` |
| `file.storage.local.base-url` | `http://localhost:8080/api/files` | 不使用 |
| `file.storage.oss.endpoint` | 不配置 | `oss-cn-hangzhou.aliyuncs.com` |
| `file.storage.oss.bucket` | 不配置 | `speedcalendar-avatars` |
| `file.storage.oss.access-key` | 不配置 | 从环境变量读取 |
| `file.storage.oss.secret-key` | 不配置 | 从环境变量读取 |
| `file.storage.oss.cdn-domain` | 不配置 | `https://cdn.speedcalendar.com`（可选） |

---

## ⚠️ 注意事项

1. **安全性**：永远不要在代码中硬编码 AccessKey
2. **备份**：迁移前备份数据库中的用户头像数据
3. **成本**：OSS按存储量和流量计费，注意控制成本
4. **迁移**：如果有历史数据，需要将本地文件迁移到OSS
5. **HTTPS**：生产环境必须使用HTTPS

---

## 📞 技术支持

如有问题，请查阅：
- [阿里云OSS Java SDK文档](https://help.aliyun.com/document_detail/32008.html)
- [Spring Boot文件上传文档](https://docs.spring.io/spring-boot/docs/current/reference/html/web.html#web.servlet.multipart)
