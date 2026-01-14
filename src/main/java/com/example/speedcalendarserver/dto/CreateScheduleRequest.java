package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建日程请求DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
public class CreateScheduleRequest {

    /**
     * 日程标题
     */
    @NotBlank(message = "日程标题不能为空")
    private String title;

    /**
     * 日程日期 (YYYY-MM-DD)
     * 可选，如果为空则尝试从 startTime 提取
     */
    private String scheduleDate;

    /**
     * 开始时间 (HH:mm)
     */
    private String startTime;

    /**
     * 结束时间 (HH:mm)
     */
    private String endTime;

    /**
     * 日程地点
     */
    private String location;

    /**
     * 是否全天
     * 可选，默认为 false
     */
    private Boolean isAllDay;

    /**
     * 是否重要
     * 可选，默认为 false
     */
    private Boolean isImportant;

    /**
     * 关联的群组ID，个人日程则为null
     */
    private String groupId;

    /**
     * 日程颜色 (十六进制颜色值，如 #F44336)
     */
    private String color;

    /**
     * 日程分类
     */
    private String category;

    /**
     * 是否由AI生成
     */
    private Boolean isAiGenerated;

    /**
     * 日程备注/笔记
     */
    private String notes;

    /**
     * 提醒时间（分钟）：提前多少分钟提醒
     */
    private Integer reminderMinutes;

    /**
     * 重复类型：none-不重复，daily-每天，weekly-每周，monthly-每月，yearly-每年
     */
    private String repeatType;

    /**
     * 重复结束日期 (YYYY-MM-DD)
     */
    private String repeatEndDate;
}
