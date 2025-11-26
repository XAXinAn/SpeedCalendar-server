package com.example.speedcalendarserver.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

/**
 * 聊天历史记录中的单条消息 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatHistoryMessageDTO {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 角色：user-用户，assistant-AI助手（API文档中为 user/ai）
     */
    private String role;

    /**
     * 消息时间戳（毫秒）
     */
    private Long timestamp;
}
