-- =============================================
-- SpeedCalendar AI 聊天功能建表语句
-- 创建时间：2025-11-26
-- =============================================

-- =============================================
-- 表: chat_sessions (聊天会话表)
-- 功能：管理 AI 聊天会话，支持会话隔离
-- =============================================
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS chat_sessions;

CREATE TABLE chat_sessions (
    -- 主键：使用VARCHAR存储UUID
    session_id VARCHAR(64) NOT NULL COMMENT '会话唯一ID (UUID)',

    -- 关联用户
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',

    -- 会话信息
    -- TODO: 后续实现异步生成会话标题
    title VARCHAR(200) DEFAULT NULL COMMENT '会话标题',
    
    -- 会话状态
    status TINYINT NOT NULL DEFAULT 1 COMMENT '会话状态：0-已关闭，1-活跃',
    
    -- 消息计数（便于分页和统计）
    message_count INT NOT NULL DEFAULT 0 COMMENT '消息总数',

    -- 时间戳
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_message_at DATETIME DEFAULT NULL COMMENT '最后消息时间',

    -- 逻辑删除
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',

    -- 约束
    PRIMARY KEY (session_id),
    KEY idx_user_id (user_id),
    KEY idx_user_status (user_id, status),
    KEY idx_last_message_at (last_message_at),

    -- 外键约束
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

-- =============================================
-- 表: chat_messages (聊天消息表)
-- 功能：存储聊天消息，支持上下文维护
-- =============================================
CREATE TABLE chat_messages (
    -- 主键：自增ID
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',

    -- 关联会话
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',

    -- 关联用户（冗余字段，便于查询和会话隔离校验）
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',

    -- 消息内容
    role ENUM('user', 'assistant', 'system') NOT NULL COMMENT '角色：user-用户，assistant-AI助手，system-系统',
    content TEXT NOT NULL COMMENT '消息内容',

    -- 消息元数据（可选）
    tokens_used INT DEFAULT NULL COMMENT '消耗的token数量',
    
    -- 消息序号（用于保持顺序，支持上下文窗口）
    sequence_num INT NOT NULL COMMENT '消息序号（会话内递增）',

    -- 时间戳
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    -- 约束
    PRIMARY KEY (id),
    KEY idx_session_id (session_id),
    KEY idx_user_id (user_id),
    KEY idx_session_sequence (session_id, sequence_num),

    -- 外键约束
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';
