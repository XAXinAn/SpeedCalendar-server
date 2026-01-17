package com.example.speedcalendarserver.dto;

import com.example.speedcalendarserver.entity.Schedule;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

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
     * 归属群组名称 (V1.2 新增)
     * 强制返回（即使为 null），避免前端缺字段
     */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String groupName;

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
     * ISO8601 格式的开始日期时间 (V1.3 新增，带时区)
     */
    private String startDateTime;

    /**
     * ISO8601 格式的结束日期时间 (V1.3 新增，带时区)
     */
    private String endDateTime;

    /**
     * 日程地点
     */
    private String location;

    /**
     * 是否全天
     */
    private Boolean isAllDay;

    /**
     * 是否重要
     */
    private Boolean isImportant;

    /**
     * 日程颜色 (十六进制颜色值)
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
     * 从Schedule实体转换 (基础转换)
     */
    public static ScheduleDTO fromEntity(Schedule schedule) {
        ZoneId zoneId = ZoneId.systemDefault();

        String startIso = null;
        String endIso = null;

        if (schedule.getScheduleDate() != null) {
            if (schedule.getStartTime() != null) {
                startIso = ZonedDateTime.of(schedule.getScheduleDate(), schedule.getStartTime(), zoneId)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } else {
                // 全天日程默认为 00:00
                startIso = ZonedDateTime.of(schedule.getScheduleDate(), java.time.LocalTime.MIN, zoneId)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }

            if (schedule.getEndTime() != null) {
                endIso = ZonedDateTime.of(schedule.getScheduleDate(), schedule.getEndTime(), zoneId)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            }
        }

        return ScheduleDTO.builder()
                .scheduleId(schedule.getScheduleId())
                .userId(schedule.getUserId())
                .groupId(schedule.getGroupId())
                .title(schedule.getTitle())
                .scheduleDate(schedule.getScheduleDate().toString())
                .startTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null)
                .endTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null)
                .startDateTime(startIso)
                .endDateTime(endIso)
                .location(schedule.getLocation())
                .isAllDay(schedule.getIsAllDay() == 1)
                .isImportant(schedule.getIsImportant() == 1)
                .color(schedule.getColor())
                .category(schedule.getCategory())
                .isAiGenerated(schedule.getIsAiGenerated() == 1)
                .notes(schedule.getNotes())
                .reminderMinutes(schedule.getReminderMinutes())
                .repeatType(schedule.getRepeatType())
                .repeatEndDate(schedule.getRepeatEndDate() != null ? schedule.getRepeatEndDate().toString() : null)
                .createdAt(schedule.getCreatedAt() != null
                        ? schedule.getCreatedAt().atZone(zoneId).toInstant().toEpochMilli()
                        : null)
                .build();
    }
}
