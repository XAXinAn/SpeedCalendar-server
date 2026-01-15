package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Data
public class UpdateUserInfoRequest {

    /**
     * 用户昵称
     */
    @Size(max = 20, message = "昵称长度不能超过20个字符")
    private String username;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Min(value = 0, message = "性别值无效")
    @Max(value = 2, message = "性别值无效")
    private Integer gender;

    /**
     * 生日 (YYYY-MM-DD)
     */
    private String birthday;

    /**
     * 个人简介
     */
    @Size(max = 200, message = "简介长度不能超过200个字符")
    private String bio;
}
