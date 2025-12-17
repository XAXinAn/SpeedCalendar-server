package com.example.speedcalendarserver.dto;

import com.example.speedcalendarserver.entity.Schedule;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.ZoneId;

/**
 * 日程信息DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduleDTO {

    /**
     * 日程ID
     */
    private String scheduleId;

    /**
     * 创建者用户ID
     */
    private String userId;

    /**
     * 关联的群组ID，个人日程则为null
     */
    private String groupId;

    /**
     * 日程标题
     */
    private String title;

    /**
     * 日程日期 (YYYY-MM-DD)
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
     */
    private Boolean isAllDay;

    /**
     * 日程颜色 (十六进制颜色值)
     */
    private String color;

    /**
     * 日程备注/笔记
     */
    private String notes;

    /**
     * 提醒时间（分钟）
     */
    private Integer reminderMinutes;

    /**
     * 重复类型：none, daily, weekly, monthly, yearly
     */
    private String repeatType;

    /**
     * 重复结束日期 (YYYY-MM-DD)
     */
    private String repeatEndDate;

    /**
     * 创建时间（时间戳，毫秒）
     */
    private Long createdAt;

    /**
     * 从Schedule实体转换
     */
    public static ScheduleDTO fromEntity(Schedule schedule) {
        return ScheduleDTO.builder()
                .scheduleId(schedule.getScheduleId())
                .userId(schedule.getUserId())
                .groupId(schedule.getGroupId())
                .title(schedule.getTitle())
                .scheduleDate(schedule.getScheduleDate().toString())
                .startTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null)
                .endTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null)
                .location(schedule.getLocation())
                .isAllDay(schedule.getIsAllDay() == 1)
                .color(schedule.getColor())
                .notes(schedule.getNotes())
                .reminderMinutes(schedule.getReminderMinutes())
                .repeatType(schedule.getRepeatType())
                .repeatEndDate(schedule.getRepeatEndDate() != null ? schedule.getRepeatEndDate().toString() : null)
                .createdAt(schedule.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }
}
