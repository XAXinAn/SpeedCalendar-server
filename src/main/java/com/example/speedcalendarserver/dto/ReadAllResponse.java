package com.example.speedcalendarserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标记全部已读响应 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadAllResponse {

    /**
     * 标记已读的消息数量
     */
    private Integer readCount;
}
