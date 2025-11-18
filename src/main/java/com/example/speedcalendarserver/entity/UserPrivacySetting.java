package com.example.speedcalendarserver.entity;

import com.example.speedcalendarserver.enums.VisibilityLevel;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户隐私设置实体
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Data
@Entity
@Table(
    name = "user_privacy_settings",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_field", columnNames = {"user_id", "field_name"})
    },
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
    }
)
public class UserPrivacySetting {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /**
     * 字段名
     */
    @Column(name = "field_name", nullable = false, length = 50)
    private String fieldName;

    /**
     * 可见性级别
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "visibility_level", nullable = false, columnDefinition = "ENUM('PUBLIC', 'FRIENDS_ONLY', 'PRIVATE')")
    private VisibilityLevel visibilityLevel;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
