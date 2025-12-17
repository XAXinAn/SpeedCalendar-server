-- =============================================
-- 数据库迁移脚本 V1.2
-- 为 schedules 表添加新字段支持
-- 执行时间: 2025-12-17
-- =============================================

-- 添加 color 字段（日程颜色）
ALTER TABLE schedules 
ADD COLUMN IF NOT EXISTS color VARCHAR(20) DEFAULT '#4AC4CF' COMMENT '日程颜色 (十六进制颜色值)' AFTER is_all_day;

-- 添加 notes 字段（日程备注）
ALTER TABLE schedules 
ADD COLUMN IF NOT EXISTS notes TEXT DEFAULT NULL COMMENT '日程备注/笔记' AFTER color;

-- 添加 reminder_minutes 字段（提醒时间）
ALTER TABLE schedules 
ADD COLUMN IF NOT EXISTS reminder_minutes INT DEFAULT NULL COMMENT '提醒时间（分钟）：提前多少分钟提醒，NULL表示不提醒' AFTER notes;

-- 添加 repeat_type 字段（重复类型）
ALTER TABLE schedules 
ADD COLUMN IF NOT EXISTS repeat_type VARCHAR(20) DEFAULT 'none' COMMENT '重复类型：none-不重复，daily-每天，weekly-每周，monthly-每月，yearly-每年' AFTER reminder_minutes;

-- 添加 repeat_end_date 字段（重复结束日期）
ALTER TABLE schedules 
ADD COLUMN IF NOT EXISTS repeat_end_date DATE DEFAULT NULL COMMENT '重复结束日期' AFTER repeat_type;

-- =============================================
-- 如果数据库不支持 IF NOT EXISTS，使用以下备选语句：
-- =============================================
-- ALTER TABLE schedules ADD COLUMN color VARCHAR(20) DEFAULT '#4AC4CF' COMMENT '日程颜色 (十六进制颜色值)';
-- ALTER TABLE schedules ADD COLUMN notes TEXT DEFAULT NULL COMMENT '日程备注/笔记';
-- ALTER TABLE schedules ADD COLUMN reminder_minutes INT DEFAULT NULL COMMENT '提醒时间（分钟）';
-- ALTER TABLE schedules ADD COLUMN repeat_type VARCHAR(20) DEFAULT 'none' COMMENT '重复类型';
-- ALTER TABLE schedules ADD COLUMN repeat_end_date DATE DEFAULT NULL COMMENT '重复结束日期';

-- =============================================
-- 创建日程附件表（如需要附件功能）
-- =============================================
CREATE TABLE IF NOT EXISTS schedule_attachments (
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
