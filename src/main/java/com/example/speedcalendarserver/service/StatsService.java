package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.entity.Schedule;
import com.example.speedcalendarserver.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;

    /**
     * 获取分类占比统计
     * @param userId 用户ID
     * @param monthStr 格式 YYYY-MM
     */
    public Map<String, Long> getCategoryStats(String userId, String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<String> groupIds = scheduleService.getMemberGroupIds(userId);
        List<Schedule> schedules = scheduleRepository.findSchedulesForUserAndGroupsByDateRange(
                userId, groupIds, startDate, endDate);

        return schedules.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getCategory() == null ? "其他" : s.getCategory(),
                        Collectors.counting()
                ));
    }

    /**
     * 获取周活跃度统计
     * @param userId 用户ID
     * @param startDateStr 开始日期 YYYY-MM-DD
     * @param endDateStr 结束日期 YYYY-MM-DD
     */
    public List<Map<String, Object>> getWeeklyActivityStats(String userId, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        List<String> groupIds = scheduleService.getMemberGroupIds(userId);
        List<Schedule> schedules = scheduleRepository.findSchedulesForUserAndGroupsByDateRange(
                userId, groupIds, startDate, endDate);

        // 使用 ISO 周字段
        WeekFields weekFields = WeekFields.of(Locale.getDefault());

        // 按周分组
        Map<String, Long> weeklyCounts = schedules.stream()
                .collect(Collectors.groupingBy(s -> {
                    LocalDate date = s.getScheduleDate();
                    int month = date.getMonthValue();
                    int weekOfMonth = date.get(weekFields.weekOfMonth());
                    return String.format("%02d-W%d", month, weekOfMonth);
                }, TreeMap::new, Collectors.counting()));

        // 转换为前端要求的格式
        return weeklyCounts.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("week", entry.getKey());
                    item.put("count", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}
