package com.example.speedcalendarserver.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * 聊天消息响应 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageResponse {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * AI 回复的消息内容
     */
    private String message;

    /**
     * 消息时间戳（毫秒）
     */
    private Long timestamp;
}
