package com.example.speedcalendarserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 活动消息列表响应 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityMessageListResponse {

    /**
     * 未读消息数量
     */
    private Long unreadCount;

    /**
     * 消息列表
     */
    private List<ActivityMessageDTO> messages;

    /**
     * 总数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer pageSize;
}
