package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新日程请求DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Data
public class UpdateScheduleRequest {

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
}
