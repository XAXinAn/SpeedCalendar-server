package com.example.speedcalendarserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 活动消息 DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityMessageDTO {

    /**
     * 消息ID
     */
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容描述
     */
    private String content;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 标签
     */
    private String tag;

    /**
     * 链接类型: none/internal/webview
     */
    private String linkType;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 当前用户是否已读
     */
    private Boolean isRead;

    /**
     * 创建时间（ISO 8601 格式字符串）
     */
    private String createdAt;
}
