package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 发送聊天消息请求 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {

    /**
     * 用户消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 会话ID（可选，为空时自动创建新会话）
     */
    private String sessionId;

    /**
     * 用户ID（可从 token 获取，前端可选传递）
     */
    private String userId;
}
