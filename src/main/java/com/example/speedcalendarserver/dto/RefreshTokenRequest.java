package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 刷新Token请求
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {

    /**
     * 刷新令牌
     */
    @NotBlank(message = "refreshToken不能为空")
    private String refreshToken;
}
