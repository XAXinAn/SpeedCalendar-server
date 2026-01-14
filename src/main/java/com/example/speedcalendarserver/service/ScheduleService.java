package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.CreateScheduleRequest;
import com.example.speedcalendarserver.dto.ScheduleDTO;
import com.example.speedcalendarserver.dto.UpdateScheduleRequest;
import com.example.speedcalendarserver.entity.Schedule;
import com.example.speedcalendarserver.entity.UserGroup;
import com.example.speedcalendarserver.repository.ScheduleRepository;
import com.example.speedcalendarserver.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserGroupRepository userGroupRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 按月份查询日程
     */
    public List<ScheduleDTO> getSchedulesByMonth(String userId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return getSchedulesByDateRange(userId, startDate, endDate);
    }

    /**
     * 按单日查询日程
     */
    public List<ScheduleDTO> getSchedulesByDate(String userId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
        return getSchedulesByDateRange(userId, date, date);
    }

    /**
     * 按日期范围查询日程（内部复用）
     */
    private List<ScheduleDTO> getSchedulesByDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        List<String> groupIds = userGroupRepository.findByUserId(userId).stream()
                .map(UserGroup::getGroupId)
                .collect(Collectors.toList());

        List<Schedule> schedules = scheduleRepository.findSchedulesForUserAndGroupsByDateRange(userId, groupIds,
                startDate, endDate);

        return schedules.stream()
                .map(ScheduleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public ScheduleDTO createSchedule(String userId, CreateScheduleRequest request) {
        log.info("创建日程 - 用户ID: {}, 标题: {}, 群组ID: {}", userId, request.getTitle(), request.getGroupId());

        validateGroupAccess(userId, request.getGroupId());

        // 兼容处理：如果 scheduleDate 为空但 startTime 有值，尝试从 startTime 提取日期
        String dateStr = request.getScheduleDate();
        String startTimeStr = request.getStartTime();
        
        if (!StringUtils.hasText(dateStr) && StringUtils.hasText(startTimeStr) && startTimeStr.contains("T")) {
            try {
                LocalDateTime dt = LocalDateTime.parse(startTimeStr); // 解析 ISO 格式
                dateStr = dt.toLocalDate().format(DATE_FORMATTER);
                // 如果没有单独传 startTime，则使用 ISO 时间中的时间部分
                if (startTimeStr.contains("T")) {
                    startTimeStr = dt.toLocalTime().format(TIME_FORMATTER);
                }
            } catch (Exception e) {
                log.warn("解析 startTime 中的日期失败: {}", startTimeStr);
            }
        }

        // 默认值处理
        if (dateStr == null) {
            throw new IllegalArgumentException("日程日期不能为空");
        }
        
        boolean isAllDay = request.getIsAllDay() != null ? request.getIsAllDay() : false;

        LocalDate scheduleDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        LocalTime startTime = parseTime(startTimeStr);
        LocalTime endTime = parseTime(request.getEndTime());
        LocalDate repeatEndDate = parseDate(request.getRepeatEndDate());

        Schedule schedule = Schedule.builder()
                .userId(userId)
                .groupId(request.getGroupId())
                .title(request.getTitle())
                .scheduleDate(scheduleDate)
                .startTime(isAllDay ? null : startTime)
                .endTime(isAllDay ? null : endTime)
                .location(request.getLocation())
                .isAllDay(isAllDay ? 1 : 0)
                .isImportant(request.getIsImportant() != null && request.getIsImportant() ? 1 : 0)
                .color(request.getColor() != null ? request.getColor() : "#4AC4CF")
                .category(request.getCategory() != null ? request.getCategory() : "其他")
                .isAiGenerated(request.getIsAiGenerated() != null && request.getIsAiGenerated() ? 1 : 0)
                .notes(request.getNotes())
                .reminderMinutes(request.getReminderMinutes())
                .repeatType(request.getRepeatType() != null ? request.getRepeatType() : "none")
                .repeatEndDate(repeatEndDate)
                .isDeleted(0)
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("日程创建成功 - 日程ID: {}", savedSchedule.getScheduleId());
        return ScheduleDTO.fromEntity(savedSchedule);
    }

    @Transactional(rollbackFor = Exception.class)
    public ScheduleDTO updateSchedule(String userId, String scheduleId, UpdateScheduleRequest request) {
        log.info("更新日程 - 用户ID: {}, 日程ID: {}", userId, scheduleId);

        Schedule schedule = scheduleRepository.findByScheduleIdAndIsDeleted(scheduleId, 0)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        validateScheduleOwnership(userId, schedule);
        validateGroupAccess(userId, request.getGroupId());

        // 兼容处理：同 createSchedule
        String dateStr = request.getScheduleDate();
        String startTimeStr = request.getStartTime();
        
        if (!StringUtils.hasText(dateStr) && StringUtils.hasText(startTimeStr) && startTimeStr.contains("T")) {
            try {
                LocalDateTime dt = LocalDateTime.parse(startTimeStr);
                dateStr = dt.toLocalDate().format(DATE_FORMATTER);
                if (startTimeStr.contains("T")) {
                    startTimeStr = dt.toLocalTime().format(TIME_FORMATTER);
                }
            } catch (Exception e) {
                log.warn("解析 startTime 中的日期失败: {}", startTimeStr);
            }
        }
        
        if (dateStr == null) {
             // 如果更新时没传日期，保持原日期？或者报错？这里假设必须传
             // 为了稳健，如果没传，使用原日期
             dateStr = schedule.getScheduleDate().format(DATE_FORMATTER);
        }

        boolean isAllDay = request.getIsAllDay() != null ? request.getIsAllDay() : (schedule.getIsAllDay() == 1);

        LocalDate scheduleDate = LocalDate.parse(dateStr, DATE_FORMATTER);
        LocalTime startTime = parseTime(startTimeStr);
        LocalTime endTime = parseTime(request.getEndTime());
        LocalDate repeatEndDate = parseDate(request.getRepeatEndDate());

        schedule.setTitle(request.getTitle());
        schedule.setScheduleDate(scheduleDate);
        schedule.setGroupId(request.getGroupId());
        schedule.setStartTime(isAllDay ? null : startTime);
        schedule.setEndTime(isAllDay ? null : endTime);
        schedule.setLocation(request.getLocation());
        schedule.setIsAllDay(isAllDay ? 1 : 0);
        
        if (request.getIsImportant() != null) {
            schedule.setIsImportant(request.getIsImportant() ? 1 : 0);
        }
        if (request.getColor() != null) schedule.setColor(request.getColor());
        if (request.getCategory() != null) schedule.setCategory(request.getCategory());
        if (request.getIsAiGenerated() != null) schedule.setIsAiGenerated(request.getIsAiGenerated() ? 1 : 0);
        
        schedule.setNotes(request.getNotes());
        schedule.setReminderMinutes(request.getReminderMinutes());
        if (request.getRepeatType() != null) schedule.setRepeatType(request.getRepeatType());
        schedule.setRepeatEndDate(repeatEndDate);

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("日程更新成功 - 日程ID: {}", scheduleId);
        return ScheduleDTO.fromEntity(updatedSchedule);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteSchedule(String userId, String scheduleId) {
        log.info("删除日程 - 用户ID: {}, 日程ID: {}", userId, scheduleId);

        Schedule schedule = scheduleRepository.findByScheduleIdAndIsDeleted(scheduleId, 0)
                .orElseThrow(() -> new RuntimeException("日程不存在"));

        validateScheduleOwnership(userId, schedule);

        schedule.setIsDeleted(1);
        scheduleRepository.save(schedule);
        log.info("日程删除成功 - 日程ID: {}", scheduleId);
    }

    private LocalTime parseTime(String timeStr) {
        if (StringUtils.hasText(timeStr)) {
            // 如果包含 T (ISO格式)，只取时间部分
            if (timeStr.contains("T")) {
                try {
                    return LocalDateTime.parse(timeStr).toLocalTime();
                } catch (Exception e) {
                    // 忽略，尝试直接解析
                }
            }
            // 尝试解析 HH:mm:ss 或 HH:mm
            try {
                return LocalTime.parse(timeStr);
            } catch (Exception e) {
                 try {
                    return LocalTime.parse(timeStr, TIME_FORMATTER);
                 } catch (Exception ex) {
                     return null;
                 }
            }
        }
        return null;
    }

    private LocalDate parseDate(String dateStr) {
        if (StringUtils.hasText(dateStr)) {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        }
        return null;
    }

    private void validateGroupAccess(String userId, String groupId) {
        if (StringUtils.hasText(groupId)) {
            UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId);
            if (userGroup == null) {
                throw new SecurityException("User is not a member of the specified group.");
            }
        }
    }

    private void validateScheduleOwnership(String userId, Schedule schedule) {
        if (StringUtils.hasText(schedule.getGroupId())) {
            UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, schedule.getGroupId());
            if (userGroup == null) {
                throw new SecurityException("User does not have permission to modify this group schedule.");
            }
        } else {
            if (!schedule.getUserId().equals(userId)) {
                throw new SecurityException("User does not have permission to modify this personal schedule.");
            }
        }
    }
}
