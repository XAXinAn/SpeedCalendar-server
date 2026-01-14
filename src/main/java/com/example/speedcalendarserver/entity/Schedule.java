package com.example.speedcalendarserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 日程实体类
 * 对应数据库表：schedules
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Entity
@Table(name = "schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Schedule {

    /**
     * 日程唯一ID (UUID)
     */
    @Id
    @Column(name = "schedule_id", length = 64, nullable = false)
    private String scheduleId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", length = 64, nullable = false)
    private String userId;

    /**
     * 关联的群组ID, NULL表示个人日程
     */
    @Column(name = "group_id", length = 255)
    private String groupId;

    /**
     * 日程标题
     */
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /**
     * 日程日期 (YYYY-MM-DD)
     */
    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    /**
     * 开始时间 (HH:mm)
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    /**
     * 结束时间 (HH:mm)
     */
    @Column(name = "end_time")
    private LocalTime endTime;

    /**
     * 日程地点
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * 是否全天：0-否，1-是
     */
    @Column(name = "is_all_day", nullable = false)
    private Integer isAllDay = 0;

    /**
     * 是否重要：0-否，1-是
     */
    @Column(name = "is_important", nullable = false)
    private Integer isImportant = 0;

    /**
     * 日程颜色 (十六进制颜色值)
     */
    @Column(name = "color", length = 20)
    private String color = "#4AC4CF";

    /**
     * 日程分类：工作, 学习, 个人, 生活, 健康, 运动, 社交, 家庭, 差旅, 其他
     */
    @Column(name = "category", length = 50)
    private String category = "其他";

    /**
     * 是否由AI生成：0-否，1-是
     */
    @Column(name = "is_ai_generated", nullable = false)
    private Integer isAiGenerated = 0;

    /**
     * 日程备注/笔记
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * 提醒时间（分钟）：提前多少分钟提醒，NULL表示不提醒
     */
    @Column(name = "reminder_minutes")
    private Integer reminderMinutes;

    /**
     * 重复类型：none-不重复，daily-每天，weekly-每周，monthly-每月，yearly-每年
     */
    @Column(name = "repeat_type", length = 20)
    private String repeatType = "none";

    /**
     * 重复结束日期
     */
    @Column(name = "repeat_end_date")
    private LocalDate repeatEndDate;

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
        if (this.scheduleId == null || this.scheduleId.isEmpty()) {
            this.scheduleId = java.util.UUID.randomUUID().toString();
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
