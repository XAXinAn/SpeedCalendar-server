package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求
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
}
