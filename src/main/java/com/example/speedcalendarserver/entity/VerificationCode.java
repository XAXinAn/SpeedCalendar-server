package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 验证码实体类
 * 对应数据库表：verification_codes
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Entity
@Table(name = "verification_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerificationCode {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    /**
     * 验证码（6位数字）
     */
    @Column(name = "code", length = 10, nullable = false)
    private String code;

    /**
     * 类型：login-登录，register-注册
     */
    @Column(name = "type", length = 20, nullable = false)
    private String type = "login";

    /**
     * 状态：0-未使用，1-已使用，2-已过期
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 请求IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 过期时间（5分钟后）
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 使用时间
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * JPA回调：插入前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 检查验证码是否有效
     *
     * @return true-有效，false-无效
     */
    public boolean isValid() {
        return this.status == 0 && LocalDateTime.now().isBefore(this.expiresAt);
    }

    /**
     * 标记验证码为已使用
     */
    public void markAsUsed() {
        this.status = 1;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * 标记验证码为已过期
     */
    public void markAsExpired() {
        this.status = 2;
    }
}
