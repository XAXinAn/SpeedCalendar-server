package com.example.speedcalendarserver.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 创建聊天会话请求 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSessionRequest {

    /**
     * 用户ID（可从 token 获取，前端可选传递）
     */
    private String userId;
}
