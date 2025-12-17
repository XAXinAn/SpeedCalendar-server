package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
     */
    @NotBlank(message = "日程日期不能为空")
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
     */
    @NotNull(message = "是否全天不能为空")
    private Boolean isAllDay;

    /**
     * 关联的群组ID，个人日程则为null
     */
    private String groupId;

    /**
     * 日程颜色 (十六进制颜色值，如 #F44336)
     */
    private String color;

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
