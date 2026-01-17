package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.CreateScheduleRequest;
import com.example.speedcalendarserver.dto.ScheduleDTO;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 悬浮窗专用工具类（仅保留创建日程）
 * 不依赖会话/历史记忆
 *
 * @author SpeedCalendar Team
 * @since 2026-01-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QuickScheduleTools {

    private final ScheduleService scheduleService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 判断字符串是否为空或无效值
     */
    private boolean isBlankOrNull(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value) || "none".equalsIgnoreCase(value);
    }

    /**
     * 创建新日程（悬浮窗专用）
     */
    @Tool(name = "createSchedule", value = "创建一个新的日程安排。当用户表达添加日程意图时调用此工具。")
    public String createSchedule(
            @P("用户ID，必须传入当前用户ID") String userId,
            @P("日程标题，必填") String title,
            @P("日程日期，必填，格式yyyy-MM-dd") String date,
            @P("开始时间，可选，格式HH:mm如14:00，没有则传空字符串") String startTime,
            @P("结束时间，可选，格式HH:mm，没有则传空字符串") String endTime,
            @P("地点，可选，没有则传空字符串") String location,
            @P("是否全天，有具体时间传false，没有具体时间传true") Boolean isAllDay,
            @P("备注信息，可选，没有则传空字符串") String notes,
            @P("提前提醒分钟数，可选，如10表示提前10分钟提醒，不需要提醒则传0") Integer reminderMinutes,
            @P("重复类型，可选，值为：none(不重复)/daily(每天)/weekly(每周)/monthly(每月)/yearly(每年)，默认none") String repeatType,
            @P("日程颜色，可选，十六进制如#FF5722，没有则传空字符串") String color,
            @P("是否重要，可选，默认false") Boolean isImportant,
            @P("群组ID或群组名称，可选，个人日程传空字符串") String groupId,
            @P("重复结束日期，可选，格式yyyy-MM-dd，没有则传空字符串") String repeatEndDate,
            @P("日程分类，可选，值为：工作/学习/运动/健康/生活/社交/家庭/差旅/个人/其他，默认根据内容自动识别") String category) {

        if (isBlankOrNull(userId)) {
            log.error("【QuickScheduleTools】createSchedule 失败：userId 为空");
            return "抱歉，无法获取用户信息，请重新登录或重试。";
        }

        log.info(
                "【QuickScheduleTools】createSchedule - userId: {}, title: {}, date: {}, startTime: {}, endTime: {}, location: {}, isAllDay: {}, notes: {}, reminder: {}, repeat: {}, color: {}, isImportant: {}, groupId: {}, repeatEndDate: {}, category: {}",
                userId, title, date, startTime, endTime, location, isAllDay, notes, reminderMinutes, repeatType, color,
                isImportant, groupId, repeatEndDate, category);

        try {
            if (isBlankOrNull(title)) {
                return "请提供日程标题。";
            }
            if (isBlankOrNull(date)) {
                return "请提供日程日期（格式：yyyy-MM-dd）。";
            }

            LocalDate.parse(date, DATE_FORMATTER);

            String actualStartTime = isBlankOrNull(startTime) ? null : startTime;
            String actualEndTime = isBlankOrNull(endTime) ? null : endTime;
            String actualLocation = isBlankOrNull(location) ? null : location;
            String actualNotes = isBlankOrNull(notes) ? null : notes;
            String actualRepeatType = isBlankOrNull(repeatType) ? "none" : repeatType;
            String actualColor = isBlankOrNull(color) ? null : color;
            Integer actualReminderMinutes = (reminderMinutes != null && reminderMinutes > 0) ? reminderMinutes : null;
            String actualRepeatEndDate = isBlankOrNull(repeatEndDate) ? null : repeatEndDate;
            String actualCategory = isBlankOrNull(category) ? "其他" : category;
            boolean actualIsAllDay = isAllDay != null ? isAllDay
                    : (actualStartTime == null && actualEndTime == null);
            boolean actualIsImportant = isImportant != null && isImportant;

            String actualGroupId = null;
            if (!isBlankOrNull(groupId)) {
                try {
                    actualGroupId = scheduleService.resolveGroupIdForUser(userId, groupId);
                } catch (Exception e) {
                    log.warn("【QuickScheduleTools】解析群组失败: {}", e.getMessage());
                }
            }

            CreateScheduleRequest request = new CreateScheduleRequest();
            request.setTitle(title);
            request.setScheduleDate(date);
            request.setStartTime(actualStartTime);
            request.setEndTime(actualEndTime);
            request.setLocation(actualLocation);
            request.setIsAllDay(actualIsAllDay);
            request.setNotes(actualNotes);
            request.setReminderMinutes(actualReminderMinutes);
            request.setRepeatType(actualRepeatType);
            request.setColor(actualColor);
            request.setIsImportant(actualIsImportant);
            request.setGroupId(actualGroupId);
            request.setRepeatEndDate(actualRepeatEndDate);
            request.setCategory(actualCategory);
            request.setIsAiGenerated(true);

            ScheduleDTO result = scheduleService.createSchedule(userId, request);

            String timeInfo = actualIsAllDay ? "全天"
                    : String.format("%s - %s",
                            actualStartTime != null ? actualStartTime : "未设置",
                            actualEndTime != null ? actualEndTime : "未设置");
            String locationInfo = (actualLocation != null) ? "，地点：" + actualLocation : "";
            String reminderInfo = (actualReminderMinutes != null) ? "，提前" + actualReminderMinutes + "分钟提醒" : "";
            String repeatInfo = !"none".equals(actualRepeatType) ? "，" + getRepeatTypeText(actualRepeatType) : "";
            String groupInfo = (actualGroupId != null && result.getGroupName() != null) ? "，群组：" + result.getGroupName()
                    : "";

            return String.format("✅ 日程创建成功！\n📅 标题：%s\n📆 日期：%s\n⏰ 时间：%s%s%s%s%s",
                    result.getTitle(),
                    result.getScheduleDate(),
                    timeInfo,
                    locationInfo,
                    reminderInfo,
                    repeatInfo,
                    groupInfo);

        } catch (DateTimeParseException e) {
            log.error("【QuickScheduleTools】日期格式错误", e);
            return "日期格式不正确，请使用 yyyy-MM-dd 格式，例如 2025-11-26。";
        } catch (Exception e) {
            log.error("【QuickScheduleTools】创建日程失败", e);
            return "抱歉，创建日程时出现错误：" + e.getMessage();
        }
    }

    /**
     * 获取重复类型的中文描述
     */
    private String getRepeatTypeText(String repeatType) {
        return switch (repeatType) {
            case "daily" -> "每天重复";
            case "weekly" -> "每周重复";
            case "monthly" -> "每月重复";
            case "yearly" -> "每年重复";
            default -> "";
        };
    }
}
