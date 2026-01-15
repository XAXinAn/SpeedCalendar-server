package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组用户关联实体类
 * 对应数据库表：user_group
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@Entity
@Table(name = "user_group")
@IdClass(UserGroupKey.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserGroup {

    /**
     * 用户ID
     */
    @Id
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 群组ID
     */
    @Id
    @Column(name = "group_id", length = 64, nullable = false)
    private String groupId;

    /**
     * 角色：owner-群主, admin-管理员, member-成员
     */
    @Column(name = "role", length = 20)
    private String role;

    /**
     * 加入时间
     */
    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    /**
     * JPA回调：插入前自动设置加入时间
     */
    @PrePersist
    protected void onCreate() {
        this.joinedAt = LocalDateTime.now();
    }
}
