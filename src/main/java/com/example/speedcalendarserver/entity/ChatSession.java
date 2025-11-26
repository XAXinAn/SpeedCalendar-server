package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 聊天会话实体类
 * 对应数据库表：chat_sessions
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Entity
@Table(name = "chat_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    /**
     * 会话唯一ID (UUID)
     */
    @Id
    @Column(name = "session_id", length = 64, nullable = false)
    private String sessionId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 会话标题
     * TODO: 后续实现异步生成会话标题
     */
    @Column(name = "title", length = 200)
    private String title;

    /**
     * 会话状态：0-已关闭，1-活跃
     */
    @Builder.Default
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 消息总数
     */
    @Builder.Default
    @Column(name = "message_count", nullable = false)
    private Integer messageCount = 0;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 最后消息时间
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * 逻辑删除：0-未删除，1-已删除
     */
    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

    /**
     * 创建前自动设置时间戳
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前自动设置时间戳
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
