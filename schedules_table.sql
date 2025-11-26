-- =============================================
-- SpeedCalendar 日程表初始化脚本
-- 功能：日程管理（创建、查询、更新、删除）
-- =============================================

USE speed_calendar;

-- =============================================
-- 表: schedules (日程表)
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
-- 查询验证
-- =============================================
-- 查看表结构
-- SHOW CREATE TABLE schedules;

-- 查看索引
-- SHOW INDEX FROM schedules;

-- 示例查询：获取某用户在某年某月的所有日程
-- SELECT * FROM schedules
-- WHERE user_id = 'your-user-id'
--   AND schedule_date >= '2024-07-01'
--   AND schedule_date < '2024-08-01'
--   AND is_deleted = 0
-- ORDER BY schedule_date ASC, start_time ASC;
