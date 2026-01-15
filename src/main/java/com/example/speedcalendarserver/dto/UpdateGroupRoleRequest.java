package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更新群组成员角色请求DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
public class UpdateGroupRoleRequest {

    /**
     * 角色：admin-管理员, member-普通成员
     */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "^(admin|member)$", message = "角色只能是 admin 或 member")
    private String role;
}
