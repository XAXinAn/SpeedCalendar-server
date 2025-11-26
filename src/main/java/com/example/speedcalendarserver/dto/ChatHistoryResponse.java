package com.example.speedcalendarserver.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 聊天历史记录响应 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatHistoryResponse {

    /**
     * 消息列表
     */
    private List<ChatHistoryMessageDTO> messages;
}
