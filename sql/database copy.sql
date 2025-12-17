-- =============================================
-- SpeedCalendar 数据库完整初始化脚本
-- 合并时间: 2025-12-17
-- v1.1: 修正了建表语句的排序规则(Collation)不一致问题
-- v1.2: 新增日程表字段 (color, notes, reminder_minutes, repeat_type, repeat_end_date)
--       新增日程附件表 (schedule_attachments)
-- =============================================

drop database speed_calendar;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS speed_calendar
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE speed_calendar;

-- =============================================
-- 统一删除所有表，注意有外键依赖的表要先删除
-- =============================================
DROP TABLE IF EXISTS user_read_messages;
DROP TABLE IF EXISTS activity_messages;
DROP TABLE IF EXISTS user_tokens;
DROP TABLE IF EXISTS user_privacy_settings;
DROP TABLE IF EXISTS verification_codes;
DROP TABLE IF EXISTS schedule_attachments;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS chat_sessions;
DROP TABLE IF EXISTS user_group;
DROP TABLE IF EXISTS `group`;
DROP TABLE IF EXISTS users;

-- =============================================
-- 表1: users (用户表)
-- =============================================
CREATE TABLE users (
    user_id VARCHAR(64) NOT NULL COMMENT '用户唯一ID (UUID)',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号（主要登录方式）',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱（备用登录方式）',
    password VARCHAR(255) DEFAULT NULL COMMENT '密码（BCrypt加密，邮箱登录使用）',
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名/昵称',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    birthday DATE DEFAULT NULL COMMENT '生日',
    bio VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常',
    login_type VARCHAR(20) NOT NULL DEFAULT 'phone' COMMENT '注册方式：phone-手机号，email-邮箱',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 表2: verification_codes (验证码表)
-- =============================================
CREATE TABLE verification_codes (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    code VARCHAR(10) NOT NULL COMMENT '验证码（6位数字）',
    type VARCHAR(20) NOT NULL DEFAULT 'login' COMMENT '类型：login-登录，register-注册',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '请求IP地址',
    expires_at DATETIME NOT NULL COMMENT '过期时间（5分钟后）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    used_at DATETIME DEFAULT NULL COMMENT '使用时间',
    PRIMARY KEY (id),
    KEY idx_phone_code (phone, code),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';

-- =============================================
-- 表3: user_tokens (用户Token表)
-- =============================================
CREATE TABLE user_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    access_token VARCHAR(500) NOT NULL COMMENT '访问令牌（JWT）',
    refresh_token VARCHAR(500) NOT NULL COMMENT '刷新令牌（JWT）',
    access_token_expires_at DATETIME NOT NULL COMMENT '访问令牌过期时间',
    refresh_token_expires_at DATETIME NOT NULL COMMENT '刷新令牌过期时间',
    device_type VARCHAR(20) DEFAULT NULL COMMENT '设备类型：android，ios，web',
    device_id VARCHAR(100) DEFAULT NULL COMMENT '设备唯一标识',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '登录IP地址',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '用户代理（浏览器信息）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-失效，1-有效',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_access_token (access_token(255)),
    UNIQUE KEY uk_refresh_token (refresh_token(255)),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_device (device_type, device_id),
    CONSTRAINT fk_user_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户Token表';

-- =============================================
-- 表4: user_privacy_settings (用户隐私设置表)
-- =============================================
CREATE TABLE user_privacy_settings (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    field_name VARCHAR(50) NOT NULL COMMENT '字段名：phone/email/birthday/gender/bio',
    visibility_level ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE') NOT NULL DEFAULT 'PUBLIC' COMMENT '可见性：PUBLIC-公开，FRIENDS_ONLY-仅好友（预留），PRIVATE-私密',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_field (user_id, field_name),
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户隐私设置表';

-- =============================================
-- 表5: group & user_group (群组相关表)
-- =============================================
CREATE TABLE `group` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `owner_id` varchar(255) DEFAULT NULL,
  `invitation_code` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitation_code` (`invitation_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_group` (
  `user_id` varchar(255) NOT NULL,
  `group_id` varchar(255) NOT NULL,
  `role` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`,`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 表6: schedules (日程表) - v1.2 更新
-- 新增字段: color, notes, reminder_minutes, repeat_type, repeat_end_date
-- =============================================
CREATE TABLE schedules (
    schedule_id VARCHAR(64) NOT NULL COMMENT '日程唯一ID (UUID)',
    user_id VARCHAR(64) NOT NULL COMMENT '创建者用户ID',
    group_id VARCHAR(255) DEFAULT NULL COMMENT '关联的群组ID, NULL表示个人日程',
    title VARCHAR(200) NOT NULL COMMENT '日程标题',
    schedule_date DATE NOT NULL COMMENT '日程日期 (YYYY-MM-DD)',
    start_time TIME DEFAULT NULL COMMENT '开始时间 (HH:mm)',
    end_time TIME DEFAULT NULL COMMENT '结束时间 (HH:mm)',
    location VARCHAR(200) DEFAULT NULL COMMENT '日程地点',
    is_all_day TINYINT NOT NULL DEFAULT 0 COMMENT '是否全天：0-否，1-是',
    -- 新增字段 v1.2 (前端需求)
    color VARCHAR(20) DEFAULT '#4AC4CF' COMMENT '日程颜色 (十六进制颜色值)',
    notes TEXT DEFAULT NULL COMMENT '日程备注/笔记',
    reminder_minutes INT DEFAULT NULL COMMENT '提醒时间（分钟）：提前多少分钟提醒，NULL表示不提醒',
    repeat_type ENUM('none', 'daily', 'weekly', 'monthly', 'yearly') DEFAULT 'none' COMMENT '重复类型',
    repeat_end_date DATE DEFAULT NULL COMMENT '重复结束日期',
    -- 原有字段
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (schedule_id),
    KEY idx_user_id (user_id),
    KEY idx_group_id (group_id),
    KEY idx_schedule_date (schedule_date),
    KEY idx_user_date (user_id, schedule_date),
    CONSTRAINT fk_schedules_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_schedules_group FOREIGN KEY (group_id) REFERENCES `group`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程表';

-- =============================================
-- 表6.1: schedule_attachments (日程附件表) - 新增
-- =============================================
CREATE TABLE schedule_attachments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    schedule_id VARCHAR(64) NOT NULL COMMENT '日程ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_url VARCHAR(500) NOT NULL COMMENT '文件URL',
    file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型 (image/jpeg, application/pdf 等)',
    file_size BIGINT UNSIGNED DEFAULT NULL COMMENT '文件大小（字节）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_schedule_id (schedule_id),
    CONSTRAINT fk_schedule_attachments_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(schedule_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程附件表';

-- =============================================
-- 表7: chat_sessions & chat_messages (AI聊天相关表)
-- =============================================
CREATE TABLE chat_sessions (
    session_id VARCHAR(64) NOT NULL COMMENT '会话唯一ID (UUID)',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    title VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '会话状态：0-已关闭，1-活跃',
    message_count INT NOT NULL DEFAULT 0 COMMENT '消息总数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_message_at DATETIME DEFAULT NULL COMMENT '最后消息时间',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    PRIMARY KEY (session_id),
    KEY idx_user_id (user_id),
    KEY idx_user_status (user_id, status),
    KEY idx_last_message_at (last_message_at),
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

CREATE TABLE chat_messages (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    role ENUM('user', 'assistant', 'system') NOT NULL COMMENT '角色：user-用户，assistant-AI助手，system-系统',
    content TEXT NOT NULL COMMENT '消息内容',
    tokens_used INT DEFAULT NULL COMMENT '消耗的token数量',
    sequence_num INT NOT NULL COMMENT '消息序号（会话内递增）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_session_id (session_id),
    KEY idx_user_id (user_id),
    KEY idx_session_sequence (session_id, sequence_num),
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';

-- =============================================
-- 表8: activity_messages (活动消息表)
-- =============================================
CREATE TABLE activity_messages (
    id VARCHAR(36) NOT NULL COMMENT '消息ID (UUID)',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容描述',
    image_url VARCHAR(500) DEFAULT NULL COMMENT '图片URL (可选)',
    tag VARCHAR(50) DEFAULT NULL COMMENT '标签，如 "新功能"、"活动"',
    link_type VARCHAR(20) NOT NULL DEFAULT 'none' COMMENT '链接类型: none/internal/webview',
    link_url VARCHAR(500) DEFAULT NULL COMMENT '跳转链接 (可选)',
    is_active TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否有效/上线',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_at DATETIME DEFAULT NULL COMMENT '过期时间 (可选，null表示永不过期)',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序权重，越大越靠前',
    PRIMARY KEY (id),
    KEY idx_is_active (is_active),
    KEY idx_created_at (created_at),
    KEY idx_expire_at (expire_at),
    KEY idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动消息表';

-- =============================================
-- 表9: user_read_messages (用户已读记录表)
-- =============================================
CREATE TABLE user_read_messages (
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    message_id VARCHAR(36) NOT NULL COMMENT '消息ID',
    read_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (user_id, message_id),
    KEY idx_user_id (user_id),
    KEY idx_message_id (message_id),
    CONSTRAINT fk_user_read_messages_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_user_read_messages_message FOREIGN KEY (message_id) REFERENCES activity_messages(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户已读消息记录表';

-- =============================================
-- 插入测试数据: 活动消息
-- =============================================
INSERT INTO activity_messages (id, title, content, image_url, tag, link_type, link_url, created_at, sort_order) VALUES
('msg_001', '新的一天从 04:00 开始', '为了完整记录你的深夜学习，我们已将每日数据刷新时间延后至凌晨4点。\n\n升级到 V5.9.17 以上即可体验，不升级的话，则仍在 24 点结算。\n（鸿蒙设备需升级到 V5.8.35 以上）', NULL, '新功能', 'none', NULL, '2024-12-03 19:55:00', 10),
('msg_002', '复习机制更新提醒', '为了提高复习效率，减少复习压力。新版本新增了「新学单词 - 次日仅复习错词」的复习方式。\n如需应用，可前往「学习设置」配置。', NULL, NULL, 'internal', '/settings', '2024-07-26 19:58:00', 5),
('msg_003', '冲刺季打卡挑战开始报名啦！！', '年底最后一波冲刺，和好友一起坚持！\n轻轻松松赢酷币，戳我报名 >>>', 'https://example.com/banner.png', '打卡挑战', 'webview', 'https://example.com/activity', '2024-11-13 22:34:00', 8);
