package com.example.speedcalendarserver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 聊天会话 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatSessionDTO {

    /**
     * 会话唯一标识 (UUID)
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 会话状态：0-已关闭，1-活跃
     */
    private Integer status;

    /**
     * 消息总数
     */
    private Integer messageCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 最后消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastMessageAt;
}
