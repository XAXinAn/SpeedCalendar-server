package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 活动消息实体类
 * 对应数据库表：activity_messages
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Entity
@Table(name = "activity_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityMessage {

    /**
     * 消息唯一ID (UUID)
     */
    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    /**
     * 标题
     */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /**
     * 内容描述
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 图片URL (可选)
     */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /**
     * 标签，如 "新功能"、"活动"
     */
    @Column(name = "tag", length = 50)
    private String tag;

    /**
     * 链接类型: none/internal/webview
     */
    @Column(name = "link_type", length = 20)
    @Builder.Default
    private String linkType = "none";

    /**
     * 跳转链接 (可选)
     */
    @Column(name = "link_url", length = 500)
    private String linkUrl;

    /**
     * 是否有效/上线
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 过期时间 (可选，null表示永不过期)
     */
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    /**
     * 排序权重，越大越靠前
     */
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;
}
