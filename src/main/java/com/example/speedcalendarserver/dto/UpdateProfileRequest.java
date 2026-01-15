package com.example.speedcalendarserver.dto;

import lombok.Data;

/**
 * 更新个人资料请求DTO (对接前端 V2.0)
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
public class UpdateProfileRequest {

    /**
     * 用户ID (前端传参，需校验与Token一致)
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 手机号 (通常不允许直接修改，此处仅接收)
     */
    private String phone;

    /**
     * 性别 (中文：男/女/保密)
     */
    private String gender;

    /**
     * 角色 (不允许修改，仅接收)
     */
    private String role;

    /**
     * 头像URL
     */
    private String avatar;
}
