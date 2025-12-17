package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户已读消息记录实体类
 * 对应数据库表：user_read_messages
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Entity
@Table(name = "user_read_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(UserReadMessage.UserReadMessageId.class)
public class UserReadMessage {

    /**
     * 用户ID
     */
    @Id
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 消息ID
     */
    @Id
    @Column(name = "message_id", length = 36, nullable = false)
    private String messageId;

    /**
     * 阅读时间
     */
    @Column(name = "read_at", nullable = false)
    @Builder.Default
    private LocalDateTime readAt = LocalDateTime.now();

    /**
     * 复合主键类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserReadMessageId implements Serializable {
        private String userId;
        private String messageId;
    }
}
