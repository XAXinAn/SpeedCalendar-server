package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建群组请求DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
public class CreateGroupRequest {

    /**
     * 群组名称
     */
    @NotBlank(message = "群组名称不能为空")
    @Size(max = 50, message = "群组名称不能超过50个字符")
    private String name;

    /**
     * 群组简介
     */
    @Size(max = 200, message = "群组简介不能超过200个字符")
    private String description;
}
