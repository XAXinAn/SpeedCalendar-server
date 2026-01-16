package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.entity.Schedule;
import com.example.speedcalendarserver.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleService scheduleService;

    /**
     * 获取分类占比统计 (饼状图数据)
     * 返回示例: [{"category": "工作", "count": 12, "color": "#2196F3"}, ...]
     */
    public List<Map<String, Object>> getCategoryStats(String userId, String monthStr) {
        YearMonth yearMonth = YearMonth.parse(monthStr);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<String> groupIds = scheduleService.getMemberGroupIds(userId);
        List<Schedule> schedules = scheduleRepository.findSchedulesForUserAndGroupsByDateRange(
                userId, groupIds, startDate, endDate);

        // 按分类分组统计
        Map<String, List<Schedule>> groups = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getCategory() == null ? "其他" : s.getCategory()));

        return groups.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("category", entry.getKey());
                    map.put("count", (long) entry.getValue().size());
                    // 取该分类下的第一个颜色作为代表色
                    String color = entry.getValue().get(0).getColor();
                    map.put("color", color != null ? color : "#9E9E9E");
                    return map;
                })
                .sorted((a, b) -> Long.compare((long) b.get("count"), (long) a.get("count")))
                .collect(Collectors.toList());
    }

    /**
     * 获取周度活跃趋势 (趋势图数据)
     * 范围: currentDate 所在周的前 4 周 + 当前周 + 后 3 周 = 共 8 周
     */
    public List<Map<String, Object>> getWeeklyTrends(String userId, String currentDateStr) {
        LocalDate currentDate = LocalDate.parse(currentDateStr);
        
        // 1. 定位当前周的周一
        LocalDate currentMonday = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        // 2. 计算 8 周的起点和终点
        LocalDate startDate = currentMonday.minusWeeks(4);
        LocalDate endDate = currentMonday.plusWeeks(4).minusDays(1); // 加上当前周和后3周，共8周的周日

        List<String> groupIds = scheduleService.getMemberGroupIds(userId);
        List<Schedule> schedules = scheduleRepository.findSchedulesForUserAndGroupsByDateRange(
                userId, groupIds, startDate, endDate);

        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM/dd");
        DateTimeFormatter rangeFormatter = DateTimeFormatter.ofPattern("MM.dd");

        // 3. 循环 8 次生成每周统计
        for (int i = 0; i < 8; i++) {
            LocalDate weekStart = startDate.plusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);
            
            long count = schedules.stream()
                    .filter(s -> !s.getScheduleDate().isBefore(weekStart) && !s.getScheduleDate().isAfter(weekEnd))
                    .count();

            Map<String, Object> weekData = new LinkedHashMap<>();
            weekData.put("weekLabel", weekStart.format(labelFormatter));
            weekData.put("weekRange", String.format("%s-%s", weekStart.format(rangeFormatter), weekEnd.format(rangeFormatter)));
            weekData.put("count", count);
            result.add(weekData);
        }

        return result;
    }
}
