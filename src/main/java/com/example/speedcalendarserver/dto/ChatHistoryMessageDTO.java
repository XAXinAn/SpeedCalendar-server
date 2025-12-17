package com.example.speedcalendarserver.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

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
     * 消息唯一标识
     */
    private String id;

    /**
     * 所属会话ID
     */
    private String sessionId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 角色：user-用户，assistant-AI助手，system-系统
     */
    private String role;

    /**
     * 消耗的 token 数量
     */
    private Integer tokensUsed;

    /**
     * 消息序号（会话内递增）
     */
    private Integer sequenceNum;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
