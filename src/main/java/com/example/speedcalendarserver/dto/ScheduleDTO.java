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
                .groupId(schedule.getGroupId()) // 添加groupId的映射
                .title(schedule.getTitle())
                .scheduleDate(schedule.getScheduleDate().toString())
                .startTime(schedule.getStartTime() != null ? schedule.getStartTime().toString() : null)
                .endTime(schedule.getEndTime() != null ? schedule.getEndTime().toString() : null)
                .location(schedule.getLocation())
                .isAllDay(schedule.getIsAllDay() == 1)
                .createdAt(schedule.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .build();
    }
}
