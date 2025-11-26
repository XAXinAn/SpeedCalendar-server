package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.CreateScheduleRequest;
import com.example.speedcalendarserver.dto.ScheduleDTO;
import com.example.speedcalendarserver.util.UserContextHolder;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 日历工具类
 * 提供 AI 可调用的日程管理工具方法，使用 LangChain4j @Tool 注解
 *
 * <p>
 * 工具方法通过 UserContextHolder 获取当前用户ID，确保操作的是当前用户的日程数据。
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalendarTools {

    private final ScheduleService scheduleService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 判断字符串是否为空或无效值
     */
    private boolean isBlankOrNull(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value) || "none".equalsIgnoreCase(value);
    }

    /**
     * 创建新日程
     * 使用独立参数，可选参数如果AI不传则使用空字符串，代码中判断处理
     *
     * @param title     日程标题（必填）
     * @param date      日程日期（必填）
     * @param startTime 开始时间（可选，不传则传空字符串）
     * @param endTime   结束时间（可选，不传则传空字符串）
     * @param location  地点（可选，不传则传空字符串）
     * @param isAllDay  是否全天
     * @return 创建结果消息
     */
    @Tool(name = "createSchedule", value = "创建一个新的日程安排。当用户说'帮我添加日程'、'创建日程'、'新建日程'或表达想要添加日程的意图时调用此工具。")
    public String createSchedule(
            @P("日程标题，必填") String title,
            @P("日程日期，必填，格式yyyy-MM-dd") String date,
            @P("开始时间，可选，格式HH:mm如14:00，没有则传空字符串") String startTime,
            @P("结束时间，可选，格式HH:mm，没有则传空字符串") String endTime,
            @P("地点，可选，没有则传空字符串") String location,
            @P("是否全天，有具体时间传false，没有具体时间传true") boolean isAllDay) {

        String userId = UserContextHolder.getUserId();
        if (userId == null) {
            log.error("【CalendarTools】createSchedule 失败：用户上下文为空");
            return "抱歉，无法获取用户信息，请重新登录后再试。";
        }

        log.info(
                "【CalendarTools】createSchedule 被调用 - userId: {}, title: {}, date: {}, startTime: {}, endTime: {}, location: {}, isAllDay: {}",
                userId, title, date, startTime, endTime, location, isAllDay);

        try {
            // 验证必填字段
            if (isBlankOrNull(title)) {
                return "请提供日程标题。";
            }
            if (isBlankOrNull(date)) {
                return "请提供日程日期（格式：yyyy-MM-dd）。";
            }

            // 验证日期格式
            LocalDate.parse(date, DATE_FORMATTER);

            // 处理可选字段：空字符串、null、"null"都视为无效
            String actualStartTime = isBlankOrNull(startTime) ? null : startTime;
            String actualEndTime = isBlankOrNull(endTime) ? null : endTime;
            String actualLocation = isBlankOrNull(location) ? null : location;

            // 构建请求
            CreateScheduleRequest request = new CreateScheduleRequest();
            request.setTitle(title);
            request.setScheduleDate(date);
            request.setStartTime(actualStartTime);
            request.setEndTime(actualEndTime);
            request.setLocation(actualLocation);
            request.setIsAllDay(isAllDay);

            // 调用服务创建日程
            ScheduleDTO result = scheduleService.createSchedule(userId, request);

            String timeInfo = isAllDay ? "全天"
                    : String.format("%s - %s",
                            actualStartTime != null ? actualStartTime : "未设置",
                            actualEndTime != null ? actualEndTime : "未设置");
            String locationInfo = (actualLocation != null) ? "，地点：" + actualLocation : "";

            return String.format("✅ 日程创建成功！\n📅 标题：%s\n📆 日期：%s\n⏰ 时间：%s%s",
                    result.getTitle(),
                    result.getScheduleDate(),
                    timeInfo,
                    locationInfo);

        } catch (DateTimeParseException e) {
            log.error("【CalendarTools】日期格式错误", e);
            return "日期格式不正确，请使用 yyyy-MM-dd 格式，例如 2025-11-26。";
        } catch (Exception e) {
            log.error("【CalendarTools】创建日程失败", e);
            return "抱歉，创建日程时出现错误：" + e.getMessage();
        }
    }

    /**
     * 查询指定月份的日程列表
     *
     * @param year  年份（必填）
     * @param month 月份，1-12（必填）
     * @return 日程列表摘要
     */
    @Tool(name = "querySchedulesByDate", value = "查询指定月份的日程列表。当用户说'查看日程'、'我有什么安排'、'这个月的日程'或表达想要查看日程的意图时调用此工具。")
    public String querySchedulesByDate(
            @P("年份，例如 2025") int year,
            @P("月份，1-12，例如 11 表示十一月") int month) {
        String userId = UserContextHolder.getUserId();
        if (userId == null) {
            log.error("【CalendarTools】querySchedulesByDate 失败：用户上下文为空");
            return "抱歉，无法获取用户信息，请重新登录后再试。";
        }

        log.info("【CalendarTools】querySchedulesByDate 被调用 - userId: {}, year: {}, month: {}", userId, year, month);

        try {
            // 验证月份范围
            if (month < 1 || month > 12) {
                return "月份必须在 1-12 之间。";
            }

            List<ScheduleDTO> schedules = scheduleService.getSchedulesByMonth(userId, year, month);

            if (schedules.isEmpty()) {
                return String.format("📅 %d年%d月暂无日程安排。", year, month);
            }

            // 构建日程摘要
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("📅 %d年%d月共有 %d 个日程：\n\n", year, month, schedules.size()));

            for (int i = 0; i < schedules.size(); i++) {
                ScheduleDTO schedule = schedules.get(i);
                String timeInfo = schedule.getIsAllDay()
                        ? "全天"
                        : String.format("%s - %s",
                                schedule.getStartTime() != null ? schedule.getStartTime() : "?",
                                schedule.getEndTime() != null ? schedule.getEndTime() : "?");

                sb.append(String.format("%d. 【%s】%s %s",
                        i + 1,
                        schedule.getScheduleDate(),
                        schedule.getTitle(),
                        timeInfo));

                if (schedule.getLocation() != null && !schedule.getLocation().isBlank()) {
                    sb.append(" @ ").append(schedule.getLocation());
                }
                sb.append("\n");
            }

            return sb.toString().trim();

        } catch (Exception e) {
            log.error("【CalendarTools】查询日程失败", e);
            return "抱歉，查询日程时出现错误：" + e.getMessage();
        }
    }

    /**
     * 删除指定日程
     * 根据标题关键词自动查询匹配的日程并删除
     *
     * @param titleKeyword 日程标题关键词
     * @return 删除结果消息
     */
    @Tool(name = "deleteSchedule", value = "删除日程。用户说删除/取消/删掉某个日程时调用。")
    public String deleteSchedule(
            @P("要删除的日程标题关键词，如健身、开会") String titleKeyword) {
        String userId = UserContextHolder.getUserId();
        if (userId == null) {
            log.error("【CalendarTools】deleteSchedule 失败：用户上下文为空");
            return "抱歉，无法获取用户信息，请重新登录后再试。";
        }

        log.info("【CalendarTools】deleteSchedule - userId: {}, keyword: {}", userId, titleKeyword);

        try {
            if (titleKeyword == null || titleKeyword.isBlank()) {
                return "请告诉我要删除哪个日程，例如'删除健身房的日程'。";
            }

            // 查询当前月和下个月的日程
            LocalDate now = LocalDate.now();
            int year = now.getYear();
            int month = now.getMonthValue();

            List<ScheduleDTO> schedules = scheduleService.getSchedulesByMonth(userId, year, month);

            // 如果当前月没找到，尝试下个月
            if (schedules.isEmpty()) {
                int nextMonth = month == 12 ? 1 : month + 1;
                int nextYear = month == 12 ? year + 1 : year;
                schedules = scheduleService.getSchedulesByMonth(userId, nextYear, nextMonth);
            }

            // 根据关键词筛选匹配的日程
            List<ScheduleDTO> matchedSchedules = schedules.stream()
                    .filter(s -> s.getTitle().contains(titleKeyword))
                    .toList();

            if (matchedSchedules.isEmpty()) {
                return String.format("找不到标题包含「%s」的日程。", titleKeyword);
            }

            if (matchedSchedules.size() == 1) {
                // 只有一个匹配，直接删除
                ScheduleDTO toDelete = matchedSchedules.get(0);
                scheduleService.deleteSchedule(userId, toDelete.getScheduleId());

                return String.format("✅ 已删除日程：【%s】%s %s",
                        toDelete.getScheduleDate(),
                        toDelete.getTitle(),
                        toDelete.getIsAllDay() ? "全天" : toDelete.getStartTime());
            } else {
                // 多个匹配，列出让用户确认
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("找到 %d 个包含「%s」的日程：\n\n", matchedSchedules.size(), titleKeyword));

                for (int i = 0; i < matchedSchedules.size(); i++) {
                    ScheduleDTO s = matchedSchedules.get(i);
                    sb.append(String.format("%d. 【%s】%s %s\n",
                            i + 1,
                            s.getScheduleDate(),
                            s.getTitle(),
                            s.getIsAllDay() ? "全天" : s.getStartTime()));
                }
                sb.append("\n请告诉我要删除哪一个，例如'删除第1个'或提供更精确的日期。");

                return sb.toString();
            }

        } catch (Exception e) {
            log.error("【CalendarTools】删除日程失败", e);
            return "抱歉，删除日程时出现错误：" + e.getMessage();
        }
    }

    /**
     * 根据序号删除日程（用于多个匹配时的二次确认）
     *
     * @param titleKeyword 日程标题关键词
     * @param index        序号（从1开始）
     * @return 删除结果消息
     */
    @Tool(name = "deleteScheduleByIndex", value = "当用户说'删除第X个'时调用此工具，用于在多个匹配日程中按序号删除。")
    public String deleteScheduleByIndex(
            @P("日程标题关键词，与之前查询时相同") String titleKeyword,
            @P("要删除的日程序号，从1开始") int index) {
        String userId = UserContextHolder.getUserId();
        if (userId == null) {
            log.error("【CalendarTools】deleteScheduleByIndex 失败：用户上下文为空");
            return "抱歉，无法获取用户信息，请重新登录后再试。";
        }

        log.info("【CalendarTools】deleteScheduleByIndex 被调用 - userId: {}, titleKeyword: {}, index: {}",
                userId, titleKeyword, index);

        try {
            // 查询当前月和下个月的日程
            LocalDate now = LocalDate.now();
            List<ScheduleDTO> schedules = scheduleService.getSchedulesByMonth(userId, now.getYear(),
                    now.getMonthValue());

            // 下个月
            int nextMonth = now.getMonthValue() == 12 ? 1 : now.getMonthValue() + 1;
            int nextYear = now.getMonthValue() == 12 ? now.getYear() + 1 : now.getYear();
            schedules.addAll(scheduleService.getSchedulesByMonth(userId, nextYear, nextMonth));

            // 根据关键词筛选
            List<ScheduleDTO> matchedSchedules = schedules.stream()
                    .filter(s -> s.getTitle().contains(titleKeyword))
                    .toList();

            if (index < 1 || index > matchedSchedules.size()) {
                return String.format("序号无效，请输入 1 到 %d 之间的数字。", matchedSchedules.size());
            }

            ScheduleDTO toDelete = matchedSchedules.get(index - 1);
            scheduleService.deleteSchedule(userId, toDelete.getScheduleId());

            return String.format("✅ 已删除日程：【%s】%s %s",
                    toDelete.getScheduleDate(),
                    toDelete.getTitle(),
                    toDelete.getIsAllDay() ? "全天" : toDelete.getStartTime());

        } catch (Exception e) {
            log.error("【CalendarTools】按序号删除日程失败", e);
            return "抱歉，删除日程时出现错误：" + e.getMessage();
        }
    }
}
