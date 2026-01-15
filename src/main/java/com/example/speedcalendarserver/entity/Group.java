package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 群组实体类
 * 对应数据库表：group (注意是关键字，需转义)
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@Entity
@Table(name = "`group`")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    /**
     * 群组ID (UUID)
     */
    @Id
    @Column(name = "id", length = 64, nullable = false)
    private String id;

    /**
     * 群组名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 群组简介
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 群主ID
     */
    @Column(name = "owner_id", length = 64, nullable = false)
    private String ownerId;

    /**
     * 邀请码 (唯一)
     */
    @Column(name = "invitation_code", length = 20, nullable = false, unique = true)
    private String invitationCode;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * JPA回调：插入前自动设置创建时间
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.id == null || this.id.isEmpty()) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}
