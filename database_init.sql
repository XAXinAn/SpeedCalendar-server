-- =============================================
-- SpeedCalendar 数据库初始化脚本
-- 功能：用户认证、登录、Token管理
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS speed_calendar
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE speed_calendar;

-- =============================================
-- 表1: users (用户表)
-- 功能：存储用户基本信息，支持手机号登录
-- =============================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    -- 主键：使用VARCHAR存储UUID，便于分布式扩展
    user_id VARCHAR(64) NOT NULL COMMENT '用户唯一ID (UUID)',

    -- 登录凭据
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号（主要登录方式）',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱（备用登录方式）',
    password VARCHAR(255) DEFAULT NULL COMMENT '密码（BCrypt加密，邮箱登录使用）',

    -- 用户资料
    username VARCHAR(50) DEFAULT NULL COMMENT '用户名/昵称',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    gender TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    birthday DATE DEFAULT NULL COMMENT '生日',
    bio VARCHAR(200) DEFAULT NULL COMMENT '个人简介',

    -- 账户状态
    status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：0-禁用，1-正常',
    login_type VARCHAR(20) NOT NULL DEFAULT 'phone' COMMENT '注册方式：phone-手机号，email-邮箱',

    -- 登录追踪
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',

    -- 时间戳
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 逻辑删除
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

    -- 约束
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_phone (phone),
    UNIQUE KEY uk_email (email),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';


-- =============================================
-- 表2: verification_codes (验证码表)
-- 功能：存储手机验证码，支持登录和注册
-- =============================================
DROP TABLE IF EXISTS verification_codes;
CREATE TABLE verification_codes (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 接收方
    phone VARCHAR(20) NOT NULL COMMENT '手机号',

    -- 验证码信息
    code VARCHAR(10) NOT NULL COMMENT '验证码（6位数字）',
    type VARCHAR(20) NOT NULL DEFAULT 'login' COMMENT '类型：login-登录，register-注册',

    -- 状态管理
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未使用，1-已使用，2-已过期',

    -- 安全追踪
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '请求IP地址',

    -- 时间管理
    expires_at DATETIME NOT NULL COMMENT '过期时间（5分钟后）',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    used_at DATETIME DEFAULT NULL COMMENT '使用时间',

    -- 约束
    PRIMARY KEY (id),
    KEY idx_phone_code (phone, code),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='验证码表';


-- =============================================
-- 表3: user_tokens (用户Token表)
-- 功能：管理JWT Token，支持多设备登录和Token刷新
-- =============================================
DROP TABLE IF EXISTS user_tokens;
CREATE TABLE user_tokens (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 关联用户
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',

    -- Token信息
    access_token VARCHAR(500) NOT NULL COMMENT '访问令牌（JWT）',
    refresh_token VARCHAR(500) NOT NULL COMMENT '刷新令牌（JWT）',

    -- Token有效期
    access_token_expires_at DATETIME NOT NULL COMMENT '访问令牌过期时间',
    refresh_token_expires_at DATETIME NOT NULL COMMENT '刷新令牌过期时间',

    -- 设备信息
    device_type VARCHAR(20) DEFAULT NULL COMMENT '设备类型：android，ios，web',
    device_id VARCHAR(100) DEFAULT NULL COMMENT '设备唯一标识',

    -- 登录信息
    ip_address VARCHAR(50) DEFAULT NULL COMMENT '登录IP地址',
    user_agent VARCHAR(500) DEFAULT NULL COMMENT '用户代理（浏览器信息）',

    -- 状态
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-失效，1-有效',

    -- 时间戳
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 约束
    PRIMARY KEY (id),
    UNIQUE KEY uk_access_token (access_token(255)),
    UNIQUE KEY uk_refresh_token (refresh_token(255)),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_device (device_type, device_id),

    -- 外键约束
    CONSTRAINT fk_user_tokens_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户Token表';


-- =============================================
-- 表4: user_privacy_settings (用户隐私设置表)
-- 功能：管理用户各字段的隐私级别，支持未来好友系统扩展
-- 性能优化：联合唯一索引，使用ENUM节省空间
-- =============================================
DROP TABLE IF EXISTS user_privacy_settings;
CREATE TABLE user_privacy_settings (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 关联用户
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',

    -- 隐私字段
    field_name VARCHAR(50) NOT NULL COMMENT '字段名：phone/email/birthday/gender/bio',

    -- 可见性级别（使用ENUM节省存储空间）
    visibility_level ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE') NOT NULL DEFAULT 'PUBLIC'
        COMMENT '可见性：PUBLIC-公开，FRIENDS_ONLY-仅好友（预留），PRIVATE-私密',

    -- 时间戳
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 约束
    PRIMARY KEY (id),
    -- 联合唯一索引：每个用户的每个字段只能有一条隐私设置
    UNIQUE KEY uk_user_field (user_id, field_name),
    -- 单列索引：用于快速查询某个用户的所有隐私设置
    KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户隐私设置表';


-- =============================================
-- 初始化数据（可选）
-- =============================================
-- 插入测试用户（密码：123456，BCrypt加密后的hash）
-- INSERT INTO users (user_id, phone, username, avatar, login_type)
-- VALUES (
--     UUID(),
--     '13800138000',
--     '测试用户',
--     'https://api.dicebear.com/7.x/initials/svg?seed=Test',
--     'phone'
-- );

-- =============================================
-- 查询验证
-- =============================================
-- 查看表结构
-- SHOW CREATE TABLE users;
-- SHOW CREATE TABLE verification_codes;
-- SHOW CREATE TABLE user_tokens;
-- SHOW CREATE TABLE user_privacy_settings;

-- 查看索引
-- SHOW INDEX FROM users;
-- SHOW INDEX FROM verification_codes;
-- SHOW INDEX FROM user_tokens;
-- SHOW INDEX FROM user_privacy_settings;


-- =============================================
-- 表5: schedules (日程表)
-- 功能：存储用户的日程安排
-- =============================================
DROP TABLE IF EXISTS schedules;
CREATE TABLE schedules (
    -- 主键：使用VARCHAR存储UUID
    schedule_id VARCHAR(64) NOT NULL COMMENT '日程唯一ID (UUID)',

    -- 关联用户
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',

    -- 日程基本信息
    title VARCHAR(200) NOT NULL COMMENT '日程标题',
    schedule_date DATE NOT NULL COMMENT '日程日期 (YYYY-MM-DD)',
    start_time TIME DEFAULT NULL COMMENT '开始时间 (HH:mm)',
    end_time TIME DEFAULT NULL COMMENT '结束时间 (HH:mm)',
    location VARCHAR(200) DEFAULT NULL COMMENT '日程地点',

    -- 日程类型
    is_all_day TINYINT NOT NULL DEFAULT 0 COMMENT '是否全天：0-否，1-是',

    -- 时间戳
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    -- 逻辑删除
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

    -- 约束
    PRIMARY KEY (schedule_id),
    KEY idx_user_id (user_id),
    KEY idx_schedule_date (schedule_date),
    KEY idx_user_date (user_id, schedule_date),
    KEY idx_created_at (created_at),

    -- 外键约束
    CONSTRAINT fk_schedules_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='日程表';


-- =============================================
-- 查询验证（日程表）
-- =============================================
-- 查看表结构
-- SHOW CREATE TABLE schedules;

-- 查看索引
-- SHOW INDEX FROM schedules;
