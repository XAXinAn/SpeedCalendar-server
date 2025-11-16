package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 用户Token实体类
 * 对应数据库表：user_tokens
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Entity
@Table(name = "user_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserToken {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 访问令牌（JWT）
     */
    @Column(name = "access_token", length = 500, nullable = false, unique = true)
    private String accessToken;

    /**
     * 刷新令牌（JWT）
     */
    @Column(name = "refresh_token", length = 500, nullable = false, unique = true)
    private String refreshToken;

    /**
     * 访问令牌过期时间
     */
    @Column(name = "access_token_expires_at", nullable = false)
    private LocalDateTime accessTokenExpiresAt;

    /**
     * 刷新令牌过期时间
     */
    @Column(name = "refresh_token_expires_at", nullable = false)
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * 设备类型：android，ios，web
     */
    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * 设备唯一标识
     */
    @Column(name = "device_id", length = 100)
    private String deviceId;

    /**
     * 登录IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 用户代理（浏览器信息）
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 状态：0-失效，1-有效
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA回调：插入前自动设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * JPA回调：更新前自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查AccessToken是否有效
     *
     * @return true-有效，false-无效
     */
    public boolean isAccessTokenValid() {
        return this.status == 1 && LocalDateTime.now().isBefore(this.accessTokenExpiresAt);
    }

    /**
     * 检查RefreshToken是否有效
     *
     * @return true-有效，false-无效
     */
    public boolean isRefreshTokenValid() {
        return this.status == 1 && LocalDateTime.now().isBefore(this.refreshTokenExpiresAt);
    }

    /**
     * 失效Token
     */
    public void invalidate() {
        this.status = 0;
    }
}
