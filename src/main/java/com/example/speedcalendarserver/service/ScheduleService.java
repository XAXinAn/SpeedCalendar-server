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

    public List<ScheduleDTO> getSchedulesByMonth(String userId, Integer year, Integer month) {
        log.info("获取日程列表 - 用户ID: {}, 年: {}, 月: {}", userId, year, month);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

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

        LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate(), DATE_FORMATTER);
        LocalTime startTime = parseTime(request.getStartTime());
        LocalTime endTime = parseTime(request.getEndTime());
        LocalDate repeatEndDate = parseDate(request.getRepeatEndDate());

        Schedule schedule = Schedule.builder()
                .userId(userId)
                .groupId(request.getGroupId())
                .title(request.getTitle())
                .scheduleDate(scheduleDate)
                .startTime(request.getIsAllDay() ? null : startTime)
                .endTime(request.getIsAllDay() ? null : endTime)
                .location(request.getLocation())
                .isAllDay(request.getIsAllDay() ? 1 : 0)
                // 新增字段
                .color(request.getColor() != null ? request.getColor() : "#4AC4CF")
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

        LocalDate scheduleDate = LocalDate.parse(request.getScheduleDate(), DATE_FORMATTER);
        LocalTime startTime = parseTime(request.getStartTime());
        LocalTime endTime = parseTime(request.getEndTime());
        LocalDate repeatEndDate = parseDate(request.getRepeatEndDate());

        schedule.setTitle(request.getTitle());
        schedule.setScheduleDate(scheduleDate);
        schedule.setGroupId(request.getGroupId());
        schedule.setStartTime(request.getIsAllDay() ? null : startTime);
        schedule.setEndTime(request.getIsAllDay() ? null : endTime);
        schedule.setLocation(request.getLocation());
        schedule.setIsAllDay(request.getIsAllDay() ? 1 : 0);
        // 新增字段更新
        if (request.getColor() != null) {
            schedule.setColor(request.getColor());
        }
        schedule.setNotes(request.getNotes());
        schedule.setReminderMinutes(request.getReminderMinutes());
        if (request.getRepeatType() != null) {
            schedule.setRepeatType(request.getRepeatType());
        }
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
            return LocalTime.parse(timeStr, TIME_FORMATTER);
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
