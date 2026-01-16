-- =============================================
-- SpeedCalendar 数据库完整初始化脚本
-- 合并时间: 2025-12-17
-- v1.1: 修正了建表语句保持的排序规则(Collation)不一致问题
-- v1.2: 新增日程表字段 (color, notes, reminder_minutes, repeat_type, repeat_end_date)
--       新增日程附件表 (schedule_attachments)
-- v1.3: 新增日程表字段 (category, is_ai_generated)
-- v1.4: 新增日程表字段 (is_important)
-- v1.5: 更新群组表结构 (description, created_at, joined_at)
-- =============================================

drop database if exists speed_calendar;

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

INSERT INTO users (user_id, phone, email, password, username, avatar, gender, birthday, bio, status, login_type, last_login_time, last_login_ip, created_at, updated_at, is_deleted) VALUES
('3791049e-973c-4872-9534-00f6b47f6974', '13958689939', NULL, '$2a$10$.N0CUCmM5UsQ.gTZe/xwyO84AMTz/HbCWrCp46ptpKxm1P9daIgnO', '用户9939', 'https://api.dicebear.com/7.x/initials/svg?seed=9939', NULL, NULL, NULL, 1, 'phone', '2026-01-15 21:18:06', '192.168.43.1', '2026-01-15 20:50:49', '2026-01-15 21:18:06', 0),
('41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '18006569106', NULL, '$2a$10$BT4R0Ojd9JjKALRz90MDeOmtllPSJE.TE9Z/qKH.dzisZ36UnzNpq', '用户9106', 'https://api.dicebear.com/7.x/initials/svg?seed=9106', NULL, NULL, NULL, 1, 'phone', '2026-01-15 21:22:19', '192.168.43.1', '2026-01-15 20:47:16', '2026-01-15 21:22:19', 0),
('5ac36d14-46fa-4e24-81c3-6493b80ca204', '18258699359', NULL, '$2a$10$cmP6/.Mr2ZrXaF6WQ12Ll.z5drr/G8KPTlgeH0W1/qAuJWYwUm78a', '用户9359', 'https://api.dicebear.com/7.x/initials/svg?seed=9359', NULL, NULL, NULL, 1, 'phone', '2026-01-15 23:10:08', '192.168.43.1', '2026-01-15 20:57:38', '2026-01-15 23:10:08', 0),
('dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '13326027275', NULL, '$2a$10$kRv.Kh2jVQyu6TjssIzDEe2FDViFU5oOfFKmrsOBF2KdvCYiVWbRy', '用户7275', 'https://api.dicebear.com/7.x/initials/svg?seed=7275', NULL, NULL, NULL, 1, 'phone', '2026-01-15 21:00:25', '192.168.43.1', '2026-01-15 21:00:20', '2026-01-15 21:00:25', 0);

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
-- 表5: group & user_group (群组相关表) - v1.5 更新
-- =============================================
CREATE TABLE `group` (
  `id` varchar(64) NOT NULL COMMENT '群组ID',
  `name` varchar(255) NOT NULL COMMENT '群组名称',
  `description` varchar(500) DEFAULT NULL COMMENT '群组简介',
  `owner_id` varchar(64) NOT NULL COMMENT '群主ID',
  `invitation_code` varchar(20) NOT NULL COMMENT '邀请码',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitation_code` (`invitation_code`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

INSERT INTO `group` (id, name, description, owner_id, invitation_code, created_at) VALUES
('2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群', '', '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'YZGDKF', '2026-01-15 20:48:04'),
('43705e23-7790-4545-bd17-513e470ad9b5', '3群', '', '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'HURTXU', '2026-01-15 20:48:07'),
('7104b3f7-118f-4532-be7d-4cca3624168b', '5群', '', '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'O2TKL2', '2026-01-15 20:48:15'),
('8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群', '', '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'IV6NOP', '2026-01-15 20:48:11'),
('f1df54be-a39a-4de4-a885-01f388103d88', '1群', '', '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'PBMCC4', '2026-01-15 20:48:00'),
('f79addf3-72d1-4369-a512-2adaca83436a', '6群', '', '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'PMQNDO', '2026-01-15 20:48:19');

CREATE TABLE `user_group` (
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `group_id` varchar(64) NOT NULL COMMENT '群组ID',
  `role` varchar(20) DEFAULT 'member' COMMENT '角色：owner-群主, admin-管理员, member-成员',
  `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (`user_id`,`group_id`),
  KEY `idx_group_id` (`group_id`),
  CONSTRAINT `fk_user_group_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_user_group_group` FOREIGN KEY (`group_id`) REFERENCES `group` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组用户关联表';

INSERT INTO `user_group` (user_id, group_id, role, joined_at) VALUES
('3791049e-973c-4872-9534-00f6b47f6974', '2e283c9a-d40e-4067-a487-d33f9164e9e9', 'member', '2026-01-15 20:52:08'),
('3791049e-973c-4872-9534-00f6b47f6974', '43705e23-7790-4545-bd17-513e470ad9b5', 'member', '2026-01-15 20:56:08'),
('3791049e-973c-4872-9534-00f6b47f6974', '7104b3f7-118f-4532-be7d-4cca3624168b', 'member', '2026-01-15 20:56:18'),
('3791049e-973c-4872-9534-00f6b47f6974', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', 'member', '2026-01-15 20:56:28'),
('3791049e-973c-4872-9534-00f6b47f6974', 'f1df54be-a39a-4de4-a885-01f388103d88', 'member', '2026-01-15 20:56:37'),
('3791049e-973c-4872-9534-00f6b47f6974', 'f79addf3-72d1-4369-a512-2adaca83436a', 'member', '2026-01-15 20:56:44'),
('41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '2e283c9a-d40e-4067-a487-d33f9164e9e9', 'owner', '2026-01-15 20:48:04'),
('41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '43705e23-7790-4545-bd17-513e470ad9b5', 'owner', '2026-01-15 20:48:07'),
('41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '7104b3f7-118f-4532-be7d-4cca3624168b', 'owner', '2026-01-15 20:48:15'),
('41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', 'owner', '2026-01-15 20:48:11'),
('41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'f1df54be-a39a-4de4-a885-01f388103d88', 'owner', '2026-01-15 20:48:00'),
('41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'f79addf3-72d1-4369-a512-2adaca83436a', 'owner', '2026-01-15 20:48:19'),
('5ac36d14-46fa-4e24-81c3-6493b80ca204', '2e283c9a-d40e-4067-a487-d33f9164e9e9', 'admin', '2026-01-15 20:52:08'),
('5ac36d14-46fa-4e24-81c3-6493b80ca204', '43705e23-7790-4545-bd17-513e470ad9b5', 'admin', '2026-01-15 20:56:08'),
('5ac36d14-46fa-4e24-81c3-6493b80ca204', '7104b3f7-118f-4532-be7d-4cca3624168b', 'admin', '2026-01-15 20:56:18'),
('5ac36d14-46fa-4e24-81c3-6493b80ca204', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', 'admin', '2026-01-15 20:56:28'),
('5ac36d14-46fa-4e24-81c3-6493b80ca204', 'f1df54be-a39a-4de4-a885-01f388103d88', 'admin', '2026-01-15 20:56:37'),
('5ac36d14-46fa-4e24-81c3-6493b80ca204', 'f79addf3-72d1-4369-a512-2adaca83436a', 'admin', '2026-01-15 20:56:44'),
('dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '2e283c9a-d40e-4067-a487-d33f9164e9e9', 'member', '2026-01-15 20:52:08'),
('dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '43705e23-7790-4545-bd17-513e470ad9b5', 'member', '2026-01-15 20:56:08'),
('dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '7104b3f7-118f-4532-be7d-4cca3624168b', 'member', '2026-01-15 20:56:18'),
('dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', 'member', '2026-01-15 20:56:28'),
('dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', 'f1df54be-a39a-4de4-a885-01f388103d88', 'member', '2026-01-15 20:56:37'),
('dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', 'f79addf3-72d1-4369-a512-2adaca83436a', 'member', '2026-01-15 20:56:44');

-- =============================================
-- 表6: schedules (日程表) - v1.4 更新
-- 新增字段: color, notes, reminder_minutes, repeat_type, repeat_end_date
-- 新增字段: category, is_ai_generated
-- 新增字段: is_important
-- =============================================
CREATE TABLE schedules (
    schedule_id VARCHAR(64) NOT NULL COMMENT '日程唯一ID (UUID)',
    user_id VARCHAR(64) NOT NULL COMMENT '创建者用户ID',
    group_id VARCHAR(64) DEFAULT NULL COMMENT '关联的群组ID, NULL表示个人日程',
    title VARCHAR(200) NOT NULL COMMENT '日程标题',
    schedule_date DATE NOT NULL COMMENT '日程日期 (YYYY-MM-DD)',
    start_time TIME DEFAULT NULL COMMENT '开始时间 (HH:mm)',
    end_time TIME DEFAULT NULL COMMENT '结束时间 (HH:mm)',
    location VARCHAR(200) DEFAULT NULL COMMENT '日程地点',
    is_all_day TINYINT NOT NULL DEFAULT 0 COMMENT '是否全天：0-否，1-是',
    -- 新增字段 v1.4 (前端需求)
    is_important TINYINT NOT NULL DEFAULT 0 COMMENT '是否重要：0-否，1-是',
    -- 新增字段 v1.2 (前端需求)
    color VARCHAR(20) DEFAULT '#4AC4CF' COMMENT '日程颜色 (十六进制颜色值)',
    notes TEXT DEFAULT NULL COMMENT '日程备注/笔记',
    reminder_minutes INT DEFAULT NULL COMMENT '提醒时间（分钟）：提前多少分钟提醒，NULL表示不提醒',
    repeat_type ENUM('none', 'daily', 'weekly', 'monthly', 'yearly') DEFAULT 'none' COMMENT '重复类型',
    repeat_end_date DATE DEFAULT NULL COMMENT '重复结束日期',
    -- 新增字段 v1.3 (前端需求)
    category VARCHAR(50) DEFAULT '其他' COMMENT '日程分类：工作, 学习, 个人, 生活, 健康, 运动, 社交, 家庭, 差旅, 其他',
    is_ai_generated TINYINT NOT NULL DEFAULT 0 COMMENT '是否由AI生成：0-否，1-是',
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
-- 批量插入日程测试数据 (共100条，2026年1月)
-- =============================================

-- 用户 u9939 (3791049e...) 的 25 条日程
INSERT INTO schedules (schedule_id, user_id, group_id, title, schedule_date, start_time, end_time, location, is_all_day, is_important, color, notes, category, is_ai_generated) VALUES
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '元旦个人规划', '2026-01-01', NULL, NULL, '家里', 1, 1, '#F44336', '新的一年，新的开始', '工作', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群新年团建', '2026-01-02', '18:00:00', '21:00:00', '火锅店', 0, 0, '#FF9800', '记得准时到', '社交', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '早起跑步', '2026-01-03', '07:00:00', '08:00:00', '公园', 0, 0, '#4CAF50', '5公里慢跑', '运动', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群项目周会', '2026-01-05', '10:00:00', '11:30:00', '腾讯会议', 0, 1, '#2196F3', '准备PPT', '工作', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '买生活用品', '2026-01-06', '19:00:00', '20:30:00', '超市', 0, 0, '#607D8B', '补充库存', '生活', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '43705e23-7790-4545-bd17-513e470ad9b5', '3群技术分享', '2026-01-08', '14:00:00', '16:00:00', 'B会谈室', 0, 0, '#9C27B0', '学习新框架', '学习', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '牙科检查', '2026-01-10', '09:30:00', '10:30:00', '诊所', 0, 1, '#00BCD4', '定期洁牙', '健康', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群篮球赛', '2026-01-11', '15:00:00', '17:00:00', '体育馆', 0, 0, '#4CAF50', '友谊第一', '运动', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '读完《三体》', '2026-01-13', NULL, NULL, NULL, 1, 0, '#FFEB3B', '最后一百页', '学习', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群志愿者活动', '2026-01-15', '09:00:00', '12:00:00', '社区中心', 0, 0, '#FF9800', '帮助独居老人', '社交', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '缴纳房租', '2026-01-16', NULL, NULL, NULL, 1, 1, '#F44336', '不要忘记了', '生活', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群聚餐', '2026-01-18', '18:30:00', '20:30:00', '居酒屋', 0, 0, '#9C27B0', '欢迎新人', '社交', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '整理电子邮箱', '2026-01-20', '21:00:00', '22:00:00', NULL, 0, 0, '#607D8B', '清理垃圾邮件', '工作', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群紧急会议', '2026-01-21', '14:00:00', '15:00:00', '语音频道', 0, 1, '#F44336', '讨论Bug修复', '工作', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '给家里打电话', '2026-01-22', '20:00:00', '20:30:00', NULL, 0, 0, '#FFEB3B', '沟通近况', '家庭', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群代码重构', '2026-01-24', '09:00:00', '18:00:00', '办公室', 0, 0, '#2196F3', 'Legacy清理', '工作', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '去电影院', '2026-01-25', '19:30:00', '22:00:00', '影城', 0, 0, '#9C27B0', '看流浪地球3', '生活', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '43705e23-7790-4545-bd17-513e470ad9b5', '3群论文研讨', '2026-01-26', '15:00:00', '17:00:00', '图书馆', 0, 0, '#FFEB3B', '讨论创新点', '学习', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '备忘: 信用卡还款', '2026-01-27', NULL, NULL, NULL, 1, 1, '#F44336', '金额: 2000', '生活', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群羽毛球', '2026-01-28', '19:00:00', '21:00:00', '羽毛球馆', 0, 0, '#4CAF50', '记得带拍子', '运动', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '写周报', '2026-01-30', '16:00:00', '17:00:00', NULL, 0, 0, '#2196F3', '总结本月工作', '工作', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群线上桌游', '2026-01-31', '20:00:00', '23:00:00', 'Discord', 0, 0, '#9C27B0', '跨月狂欢', '社交', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '深度睡眠日', '2026-01-14', NULL, NULL, NULL, 1, 0, '#00BCD4', '休息是最好的药', '健康', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群资料整理', '2026-01-07', '10:00:00', '12:00:00', NULL, 0, 0, '#607D8B', '归档旧文档', '工作', 0),
(UUID(), '3791049e-973c-4872-9534-00f6b47f6974', NULL, '日语单词背诵', '2026-01-04', '21:00:00', '22:00:00', NULL, 0, 0, '#FFEB3B', 'N2单词', '学习', 0);

-- 用户 u9106 (41f671a8...) 的 25 条日程
INSERT INTO schedules (schedule_id, user_id, group_id, title, schedule_date, start_time, end_time, location, is_all_day, is_important, color, notes, category, is_ai_generated) VALUES
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '群主月度规划', '2026-01-01', '09:00:00', '10:00:00', '办公室', 0, 1, '#F44336', '管理全群任务', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群群务管理', '2026-01-03', '14:00:00', '15:00:00', NULL, 0, 0, '#607D8B', '审核新人', '其他', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '去健身房', '2026-01-04', '18:00:00', '20:00:00', '健身中心', 0, 0, '#4CAF50', '练胸', '运动', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群核心讨论', '2026-01-06', '16:00:00', '17:30:00', 'VIP会议室', 0, 1, '#2196F3', '商业机密', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '陪孩子玩耍', '2026-01-07', '19:00:00', '21:00:00', '家里', 0, 0, '#FFEB3B', '乐高时间', '家庭', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '43705e23-7790-4545-bd17-513e470ad9b5', '3群学习督促', '2026-01-09', '20:00:00', '21:00:00', NULL, 0, 0, '#FF9800', '抽查进度', '学习', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '出差北京', '2026-01-12', NULL, NULL, '北京', 1, 1, '#9C27B0', '总部汇报', '差旅', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群财务结算', '2026-01-14', '10:00:00', '11:00:00', NULL, 0, 0, '#607D8B', '上月结余', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '休息日', '2026-01-15', NULL, NULL, NULL, 1, 0, '#00BCD4', '睡到自然醒', '健康', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群策划评审', '2026-01-17', '14:00:00', '16:00:00', NULL, 0, 1, '#F44336', '定稿', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '晚宴', '2026-01-19', '19:00:00', '21:30:00', '希尔顿酒店', 0, 0, '#FF9800', '商务社交', '社交', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群系统维护', '2026-01-21', '01:00:00', '04:00:00', NULL, 0, 0, '#2196F3', '凌晨停机维护', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '给车辆保养', '2026-01-23', '10:00:00', '12:00:00', '4S店', 0, 0, '#607D8B', '换机油', '生活', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群月度大会', '2026-01-25', '15:00:00', '17:00:00', '大会议室', 0, 1, '#F44336', '全员必到', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '陪父母逛街', '2026-01-26', '14:00:00', '18:00:00', '步行街', 0, 0, '#FFEB3B', '过年买衣服', '家庭', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群奖金发放', '2026-01-28', '10:00:00', '11:00:00', NULL, 0, 1, '#4CAF50', '犒劳大家', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '写年终总结', '2026-01-30', '09:00:00', '12:00:00', NULL, 0, 1, '#2196F3', '发给董事会', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '43705e23-7790-4545-bd17-513e470ad9b5', '3群假期安排', '2026-01-31', '10:00:00', '11:00:00', NULL, 0, 0, '#607D8B', '春节值班表', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '买年货', '2026-01-29', NULL, NULL, NULL, 1, 0, '#F44336', '烟酒茶糖', '生活', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群年会排练', '2026-01-20', '18:00:00', '20:00:00', NULL, 0, 0, '#FF9800', '舞蹈节目', '社交', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '私人SPA', '2026-01-11', '16:00:00', '18:00:00', '会所', 0, 0, '#00BCD4', '放松身心', '健康', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群合作洽谈', '2026-01-05', '14:00:00', '15:30:00', '咖啡厅', 0, 0, '#2196F3', '外包商面试', '工作', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '整理书架', '2026-01-08', '21:00:00', '22:30:00', NULL, 0, 0, '#FFEB3B', '断舍离', '生活', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群团拜预演', '2026-01-27', '19:00:00', '20:00:00', NULL, 0, 0, '#FF9800', '练习话术', '社交', 0),
(UUID(), '41f671a8-73bc-4ad1-b4cc-60afd5bdde86', NULL, '看财务报表', '2026-01-18', '10:00:00', '12:00:00', NULL, 0, 1, '#F44336', '严格把关', '工作', 0);

-- 用户 u9359 (5ac36d14...) 的 25 条日程
INSERT INTO schedules (schedule_id, user_id, group_id, title, schedule_date, start_time, end_time, location, is_all_day, is_important, color, notes, category, is_ai_generated) VALUES
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '考研复习开始', '2026-01-01', '08:00:00', '22:00:00', '自习室', 0, 1, '#2196F3', '政治/英语', '学习', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群资料分享', '2026-01-02', '10:00:00', '11:00:00', NULL, 0, 0, '#FFEB3B', '上传笔记', '学习', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '游泳', '2026-01-04', '15:00:00', '16:30:00', '游泳馆', 0, 0, '#4CAF50', '有氧运动', '运动', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群志愿者会议', '2026-01-06', '19:00:00', '20:00:00', NULL, 0, 0, '#FF9800', '讨论活动细节', '社交', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '看中医', '2026-01-08', '09:00:00', '11:00:00', '中医院', 0, 1, '#00BCD4', '调理身体', '健康', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '43705e23-7790-4545-bd17-513e470ad9b5', '3群论文盲审', '2026-01-10', NULL, NULL, NULL, 1, 1, '#F44336', '截止日期', '学习', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '去花市', '2026-01-12', '14:00:00', '16:00:00', '花卉市场', 0, 0, '#FFEB3B', '买点年宵花', '生活', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群桌游之夜', '2026-01-14', '19:00:00', '23:00:00', '桌游吧', 0, 0, '#9C27B0', '剧本杀', '社交', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '整理衣柜', '2026-01-16', '10:00:00', '12:00:00', NULL, 0, 0, '#607D8B', '冬装整理', '生活', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群线上观影', '2026-01-18', '20:00:00', '22:00:00', NULL, 0, 0, '#2196F3', '一起看纪录片', '其他', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '给猫打疫苗', '2026-01-20', '15:00:00', '16:00:00', '宠物医院', 0, 0, '#FF9800', '记得带证件', '生活', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群摄影交流', '2026-01-22', '14:00:00', '17:00:00', '植物园', 0, 0, '#4CAF50', '拍腊梅', '社交', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '去图书馆还书', '2026-01-24', '10:00:00', '11:00:00', '图书馆', 0, 0, '#2196F3', '超期要罚款', '学习', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群编程马拉松', '2026-01-25', '09:00:00', '21:00:00', '线上', 0, 1, '#F44336', '挑战24小时', '学习', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '家庭大扫除', '2026-01-27', NULL, NULL, NULL, 1, 1, '#FFEB3B', '辞旧迎新', '家庭', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群年货互换', '2026-01-28', '14:00:00', '16:00:00', NULL, 0, 0, '#FF9800', '交换家乡特产', '社交', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '写寒假计划', '2026-01-30', '20:00:00', '21:00:00', NULL, 0, 0, '#607D8B', '目标设定', '学习', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '43705e23-7790-4545-bd17-513e470ad9b5', '3群假期通知', '2026-01-31', '09:00:00', '10:00:00', NULL, 0, 0, '#2196F3', '确认放假时间', '其他', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '背50个单词', '2026-01-03', '21:00:00', '22:00:00', NULL, 0, 0, '#FFEB3B', '打卡', '学习', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群场地预约', '2026-01-05', '10:00:00', '10:30:00', NULL, 0, 0, '#607D8B', '订下周球场', '工作', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '看心理医生', '2026-01-13', '14:00:00', '15:00:00', '咨询室', 0, 0, '#00BCD4', '解压咨询', '健康', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群团购接龙', '2026-01-15', NULL, NULL, NULL, 1, 0, '#FF9800', '买车厘子', '生活', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '修剪头发', '2026-01-19', '18:00:00', '19:00:00', '理发店', 0, 0, '#9C27B0', '换个发型', '生活', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群设备归还', '2026-01-23', '11:00:00', '12:00:00', '器材室', 0, 0, '#607D8B', '还相机', '其他', 0),
(UUID(), '5ac36d14-46fa-4e24-81c3-6493b80ca204', NULL, '给老师拜年', '2026-01-31', '19:00:00', '20:00:00', NULL, 0, 1, '#F44336', '发短信/微信', '社交', 0);

-- 用户 u7275 (dfa2e2e8...) 的 25 条日程
INSERT INTO schedules (schedule_id, user_id, group_id, title, schedule_date, start_time, end_time, location, is_all_day, is_important, color, notes, category, is_ai_generated) VALUES
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '新年第一练', '2026-01-01', '10:00:00', '12:00:00', '操场', 0, 0, '#4CAF50', '慢跑10km', '运动', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群需求调研', '2026-01-02', '14:00:00', '16:00:00', '线上', 0, 1, '#2196F3', '访谈核心用户', '工作', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '洗车', '2026-01-04', '09:00:00', '10:00:00', '洗车场', 0, 0, '#607D8B', '内外清洗', '生活', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群文档归档', '2026-01-06', '10:00:00', '12:00:00', NULL, 0, 0, '#607D8B', '清理旧Wiki', '工作', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '约会', '2026-01-07', '18:30:00', '21:30:00', '西餐厅', 0, 1, '#F44336', '纪念日', '社交', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '43705e23-7790-4545-bd17-513e470ad9b5', '3群课程答疑', '2026-01-09', '19:00:00', '20:30:00', '直播间', 0, 0, '#FFEB3B', '回答学生问题', '学习', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '体检', '2026-01-12', '08:00:00', '11:30:00', '体检中心', 0, 1, '#00BCD4', '空腹前往', '健康', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群物资采购', '2026-01-14', '14:00:00', '17:00:00', '五金城', 0, 0, '#FF9800', '为活动买耗材', '其他', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '去银行', '2026-01-15', '10:00:00', '11:00:00', '建设银行', 0, 0, '#607D8B', '激活新卡', '生活', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群海报设计', '2026-01-17', NULL, NULL, NULL, 1, 0, '#9C27B0', '完成初稿', '工作', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '看画展', '2026-01-19', '14:00:00', '16:00:00', '美术馆', 0, 0, '#FFEB3B', '莫奈特展', '社交', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群项目复盘', '2026-01-21', '10:00:00', '12:00:00', '线上', 0, 1, '#F44336', '总结经验教训', '工作', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '买高铁票', '2026-01-23', '08:00:00', '08:30:00', '12306', 0, 1, '#2196F3', '抢回家的票', '差旅', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', 'f1df54be-a39a-4de4-a885-01f388103d88', '1群春节福利', '2026-01-25', '10:00:00', '11:00:00', NULL, 0, 0, '#4CAF50', '领取大礼包', '生活', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '给车加油', '2026-01-26', '19:00:00', '19:30:00', '加油站', 0, 0, '#607D8B', '加满', '生活', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '2e283c9a-d40e-4067-a487-d33f9164e9e9', '2群线上春晚', '2026-01-28', '20:00:00', '22:00:00', '视频会议', 0, 0, '#FF9800', '表演个节目', '社交', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '写拜年贺词', '2026-01-30', '14:00:00', '15:00:00', NULL, 0, 0, '#FFEB3B', '群发准备', '其他', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '43705e23-7790-4545-bd17-513e470ad9b5', '3群假期值班', '2026-01-31', '09:00:00', '18:00:00', '公司', 0, 1, '#F44336', '第一天值班', '工作', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '准备年夜饭', '2026-01-29', '14:00:00', '20:00:00', '厨房', 0, 1, '#FF9800', '主厨担当', '家庭', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '8ee1787b-2246-4940-bbfa-bbf11bc14ae6', '4群新年祝福', '2026-01-31', '00:00:00', '00:30:00', NULL, 0, 0, '#F44336', '抢红包', '社交', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '滑雪', '2026-01-11', '09:00:00', '17:00:00', '滑雪场', 0, 0, '#00BCD4', '爽滑一天', '运动', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', '7104b3f7-118f-4532-be7d-4cca3624168b', '5群UI切图', '2026-01-05', '10:00:00', '18:00:00', NULL, 0, 0, '#2196F3', '交给后端', '工作', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '学习Kotlin', '2026-01-08', '20:00:00', '22:00:00', NULL, 0, 0, '#FFEB3B', '协程部分', '学习', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', 'f79addf3-72d1-4369-a512-2adaca83436a', '6群摄影后期', '2026-01-27', '21:00:00', '23:00:00', NULL, 0, 0, '#607D8B', '修图发朋友圈', '生活', 0),
(UUID(), 'dfa2e2e8-4f2b-4bda-9a59-908fbc34e375', NULL, '看牙医复诊', '2026-01-18', '15:00:00', '16:00:00', '诊所', 0, 0, '#00BCD4', '检查牙套', '健康', 0);