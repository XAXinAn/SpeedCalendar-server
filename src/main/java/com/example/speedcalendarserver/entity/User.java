package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库表：users
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * 用户唯一ID (UUID)
     */
    @Id
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 手机号（主要登录方式）
     */
    @Column(name = "phone", length = 20, unique = true)
    private String phone;

    /**
     * 邮箱（备用登录方式）
     */
    @Column(name = "email", length = 100, unique = true)
    private String email;

    /**
     * 密码（BCrypt加密，邮箱登录使用）
     */
    @Column(name = "password", length = 255)
    private String password;

    /**
     * 用户名/昵称
     */
    @Column(name = "username", length = 50)
    private String username;

    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Column(name = "gender")
    private Integer gender = 0;

    /**
     * 生日
     */
    @Column(name = "birthday")
    private LocalDate birthday;

    /**
     * 个人简介
     */
    @Column(name = "bio", length = 200)
    private String bio;

    /**
     * 账号状态：0-禁用，1-正常
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 注册方式：phone-手机号，email-邮箱
     */
    @Column(name = "login_type", length = 20, nullable = false)
    private String loginType = "phone";

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

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
     * 逻辑删除：0-未删除，1-已删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Integer isDeleted = 0;

    /**
     * JPA回调：插入前自动设置创建时间和更新时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.userId == null || this.userId.isEmpty()) {
            this.userId = java.util.UUID.randomUUID().toString();
        }
    }

    /**
     * JPA回调：更新前自动设置更新时间
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
