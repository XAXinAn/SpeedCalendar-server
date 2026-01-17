package com.example.speedcalendarserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 快速日程动作响应 DTO
 * 支持创建/删除/待确认等多种动作类型
 *
 * @author SpeedCalendar Team
 * @since 2026-01-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuickScheduleActionResponse {

    /**
     * 动作类型：create | delete | delete_pending
     * - create: 创建日程成功
     * - delete: 删除日程成功
     * - delete_pending: 待确认删除（多个匹配）
     */
    private String action;

    /**
     * 创建或删除的日程信息
     * 当 action=create 或 action=delete 时返回
     */
    private ScheduleDTO schedule;

    /**
     * 操作结果或提示消息
     */
    private String message;

    /**
     * 便捷跳转字段：日程日期（YYYY-MM-DD）
     * 前端可用于跳转到对应日期视图
     */
    private String scheduleDate;

    /**
     * 待确认删除的日程列表（当有多个匹配时）
     */
    private java.util.List<ScheduleDTO> pendingSchedules;

    /**
     * 待确认删除的关键词（用于二次确认）
     */
    private String pendingKeyword;
}
