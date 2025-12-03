package com.example.speedcalendarserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读消息数量响应 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnreadCountResponse {

    /**
     * 未读消息数量
     */
    private Long unreadCount;
}
