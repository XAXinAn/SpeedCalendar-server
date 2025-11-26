package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.CreateScheduleRequest;
import com.example.speedcalendarserver.dto.ScheduleDTO;
import com.example.speedcalendarserver.dto.UpdateScheduleRequest;
import com.example.speedcalendarserver.entity.Schedule;
import com.example.speedcalendarserver.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日程服务
 * 处理日程的创建、查询、更新、删除等业务逻辑
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 获取指定用户在某年某月的所有日程
     *
     * @param userId 用户ID
     * @param year   年份
     * @param month  月份
     * @return 日程列表
     */
    public List<ScheduleDTO> getSchedulesByMonth(String userId, Integer year, Integer month) {
        log.info("获取日程列表 - 用户ID: {}, 年: {}, 月: {}", userId, year, month);

        // 计算月份的开始和结束日期
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth().plusDays(1); // 下个月的第一天

        // 查询日程
        List<Schedule> schedules = scheduleRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        // 转换为DTO
        return schedules.stream()
                .map(ScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 创建新日程
     *
     * @param userId  用户ID
     * @param request 创建日程请求
     * @return 创建的日程DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleDTO createSchedule(String userId, CreateScheduleRequest request) {
        log.info("创建日程 - 用户ID: {}, 标题: {}", userId, request.getTitle());

        // 验证日期格式
        LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate(), DATE_FORMATTER);

        // 验证时间格式
        LocalTime startTime = null;
        LocalTime endTime = null;
        if (!request.getIsAllDay()) {
            if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
                startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
            }
            if (request.getEndTime() != null && !request.getEndTime().isEmpty()) {
                endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);
            }
        }

        // 创建日程实体
        Schedule schedule = Schedule.builder()
                .userId(userId)
                .title(request.getTitle())
                .scheduleDate(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .location(request.getLocation())
                .isAllDay(request.getIsAllDay() ? 1 : 0)
                .isDeleted(0)
                .build();

        // 保存到数据库
        Schedule savedSchedule = scheduleRepository.save(schedule);

        log.info("日程创建成功 - 日程ID: {}", savedSchedule.getScheduleId());

        return ScheduleDTO.fromEntity(savedSchedule);
    }

    /**
     * 更新日程
     *
     * @param userId     用户ID
     * @param scheduleId 日程ID
     * @param request    更新日程请求
     * @return 更新后的日程DTO
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleDTO updateSchedule(String userId, String scheduleId, UpdateScheduleRequest request) {
        log.info("更新日程 - 用户ID: {}, 日程ID: {}", userId, scheduleId);

        // 查找日程
        Schedule schedule = scheduleRepository.findByScheduleIdAndUserIdAndIsDeleted(scheduleId, userId, 0)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        // 验证日期格式
        LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate(), DATE_FORMATTER);

        // 验证时间格式
        LocalTime startTime = null;
        LocalTime endTime = null;
        if (!request.getIsAllDay()) {
            if (request.getStartTime() != null && !request.getStartTime().isEmpty()) {
                startTime = LocalTime.parse(request.getStartTime(), TIME_FORMATTER);
            }
            if (request.getEndTime() != null && !request.getEndTime().isEmpty()) {
                endTime = LocalTime.parse(request.getEndTime(), TIME_FORMATTER);
            }
        }

        // 更新日程信息
        schedule.setTitle(request.getTitle());
        schedule.setScheduleDate(scheduleDate);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setLocation(request.getLocation());
        schedule.setIsAllDay(request.getIsAllDay() ? 1 : 0);

        // 保存更新
        Schedule updatedSchedule = scheduleRepository.save(schedule);

        log.info("日程更新成功 - 日程ID: {}", scheduleId);

        return ScheduleDTO.fromEntity(updatedSchedule);
    }

    /**
     * 删除日程（逻辑删除）
     *
     * @param userId     用户ID
     * @param scheduleId 日程ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchedule(String userId, String scheduleId) {
        log.info("删除日程 - 用户ID: {}, 日程ID: {}", userId, scheduleId);

        // 查找日程
        Schedule schedule = scheduleRepository.findByScheduleIdAndUserIdAndIsDeleted(scheduleId, userId, 0)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        // 逻辑删除
        schedule.setIsDeleted(1);
        scheduleRepository.save(schedule);

        log.info("日程删除成功 - 日程ID: {}", scheduleId);
    }
}
