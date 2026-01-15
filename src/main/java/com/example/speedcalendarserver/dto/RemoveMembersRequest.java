package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量移除成员请求DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
public class RemoveMembersRequest {

    /**
     * 要移除的用户ID列表
     */
    @NotEmpty(message = "移除列表不能为空")
    private List<String> userIds;
}
