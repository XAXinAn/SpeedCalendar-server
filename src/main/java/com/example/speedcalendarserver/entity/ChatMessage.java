package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * 对应数据库表：chat_messages
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    /**
     * 主键ID（自增）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 64, nullable = false)
    private String sessionId;

    /**
     * 用户ID（冗余字段，便于会话隔离校验）
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 角色：user-用户，assistant-AI助手，system-系统
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MessageRole role;

    /**
     * 消息内容
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 消耗的token数量（可选）
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    /**
     * 消息序号（会话内递增，用于保持顺序）
     */
    @Column(name = "sequence_num", nullable = false)
    private Integer sequenceNum;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 创建前自动设置时间戳
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 消息角色枚举
     */
    public enum MessageRole {
        user, // 用户
        assistant, // AI助手
        system // 系统
    }
}
