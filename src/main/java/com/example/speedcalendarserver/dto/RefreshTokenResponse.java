package com.example.speedcalendarserver.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 刷新Token响应
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenResponse {

    /**
     * 新的访问令牌
     */
    private String token;

    /**
     * 新的刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌有效期（秒）
     */
    private Long expiresIn;
}
