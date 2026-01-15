package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 加入群组请求DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
public class JoinGroupRequest {

    /**
     * 邀请码
     */
    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;
}
