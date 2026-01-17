package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.CreateScheduleRequest;
import com.example.speedcalendarserver.dto.ScheduleDTO;
import com.example.speedcalendarserver.dto.UpdateScheduleRequest;
import com.example.speedcalendarserver.entity.Group;
import com.example.speedcalendarserver.entity.Schedule;
import com.example.speedcalendarserver.entity.UserGroup;
import com.example.speedcalendarserver.repository.GroupRepository;
import com.example.speedcalendarserver.repository.ScheduleRepository;
import com.example.speedcalendarserver.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 日程服务
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserGroupRepository userGroupRepository;
    private final GroupRepository groupRepository;

    /**
     * 按日期获取日程列表 (个人 + 所属群组)
     * V1.2: 包含来源群组名称 (groupName)
     */
    public List<ScheduleDTO> getSchedulesByDate(String userId, String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        List<String> groupIds = getMemberGroupIds(userId);

        List<Schedule> schedules = scheduleRepository.findSchedulesForUserAndGroupsByDateRange(
                userId, groupIds, date, date);

        return enrichScheduleDTOs(schedules);
    }

    /**
     * 按月获取日程列表 (个人 + 所属群组)
     * V1.2: 包含来源群组名称 (groupName)
     */
    public List<ScheduleDTO> getSchedulesByMonth(String userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        return getSchedulesByRange(userId, startDate, endDate);
    }

    /**
     * 按时间范围获取日程列表 (个人 + 所属群组)
     */
    public List<ScheduleDTO> getSchedulesByRange(String userId, String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);
        return getSchedulesByRange(userId, startDate, endDate);
    }

    /**
     * 获取临近日程 (过去3h / 未来24h)
     */
    public List<ScheduleDTO> getNearbySchedules(String userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTimeLimit = now.minusHours(3);
        LocalDateTime endTimeLimit = now.plusHours(24);

        // 获取日期跨度，可能跨2-3天
        LocalDate startDate = startTimeLimit.toLocalDate();
        LocalDate endDate = endTimeLimit.toLocalDate();

        List<ScheduleDTO> allInRange = getSchedulesByRange(userId, startDate, endDate);

        // 内存中根据精确时间过滤
        return allInRange.stream()
                .filter(dto -> {
                    if (dto.getIsAllDay()) {
                        // 全天日程，只要日期在范围内就包含
                        return true;
                    }
                    if (dto.getStartDateTime() == null)
                        return false;

                    LocalDateTime scheduleStart = LocalDateTime.parse(
                            dto.getStartDateTime().substring(0, 19)); // 简单截取处理
                    return !scheduleStart.isBefore(startTimeLimit) && !scheduleStart.isAfter(endTimeLimit);
                })
                .collect(Collectors.toList());
    }

    /**
     * 按时间范围获取日程列表 (内部实现)
     */
    private List<ScheduleDTO> getSchedulesByRange(String userId, LocalDate startDate, LocalDate endDate) {
        List<String> groupIds = getMemberGroupIds(userId);
        List<Schedule> schedules = scheduleRepository.findSchedulesForUserAndGroupsByDateRange(
                userId, groupIds, startDate, endDate);
        return enrichScheduleDTOs(schedules);
    }

    /**
     * 创建日程
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleDTO createSchedule(String userId, CreateScheduleRequest request) {
        // 1. 如果是群组日程，校验成员身份
        if (request.getGroupId() != null && !request.getGroupId().isBlank()) {
            validateGroupMembership(userId, request.getGroupId());
        }

        // 2. 构建实体
        Schedule schedule = Schedule.builder()
                .scheduleId(UUID.randomUUID().toString())
                .userId(userId)
                .groupId(request.getGroupId())
                .title(request.getTitle())
                .scheduleDate(LocalDate.parse(request.getScheduleDate()))
                .startTime(request.getStartTime() != null ? LocalTime.parse(request.getStartTime()) : null)
                .endTime(request.getEndTime() != null ? LocalTime.parse(request.getEndTime()) : null)
                .location(request.getLocation())
                .isAllDay(request.getIsAllDay() != null && request.getIsAllDay() ? 1 : 0)
                .isImportant(request.getIsImportant() != null && request.getIsImportant() ? 1 : 0)
                .color(request.getColor() != null ? request.getColor() : "#4AC4CF")
                .category(request.getCategory() != null ? request.getCategory() : "其他")
                .notes(request.getNotes())
                .reminderMinutes(request.getReminderMinutes())
                .repeatType(request.getRepeatType())
                .repeatEndDate(request.getRepeatEndDate() != null ? LocalDate.parse(request.getRepeatEndDate()) : null)
                .isAiGenerated(request.getIsAiGenerated() != null && request.getIsAiGenerated() ? 1 : 0)
                .isDeleted(0)
                .build();

        Schedule saved = scheduleRepository.save(schedule);
        return convertToDTO(saved);
    }

    /**
     * 更新日程
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleDTO updateSchedule(String userId, String scheduleId, UpdateScheduleRequest request) {
        Schedule schedule = scheduleRepository.findByScheduleIdAndIsDeleted(scheduleId, 0)
                .orElseThrow(() -> new RuntimeException("日程不存在或已被删除"));

        // 1. 权限校验
        checkPermission(userId, schedule, "修改");

        // 2. V1.2: 归属变更校验
        if (request.getGroupId() != null) {
            String newGroupId = request.getGroupId().isBlank() ? null : request.getGroupId();
            if (newGroupId != null) {
                validateGroupMembership(userId, newGroupId);
            }
            schedule.setGroupId(newGroupId);
        }

        // 3. 更新字段
        if (request.getTitle() != null)
            schedule.setTitle(request.getTitle());
        if (request.getScheduleDate() != null)
            schedule.setScheduleDate(LocalDate.parse(request.getScheduleDate()));
        if (request.getStartTime() != null)
            schedule.setStartTime(LocalTime.parse(request.getStartTime()));
        if (request.getEndTime() != null)
            schedule.setEndTime(LocalTime.parse(request.getEndTime()));
        if (request.getLocation() != null)
            schedule.setLocation(request.getLocation());
        if (request.getIsAllDay() != null)
            schedule.setIsAllDay(request.getIsAllDay() ? 1 : 0);
        if (request.getIsImportant() != null)
            schedule.setIsImportant(request.getIsImportant() ? 1 : 0);
        if (request.getColor() != null)
            schedule.setColor(request.getColor());
        if (request.getCategory() != null)
            schedule.setCategory(request.getCategory());
        if (request.getNotes() != null)
            schedule.setNotes(request.getNotes());
        if (request.getReminderMinutes() != null)
            schedule.setReminderMinutes(request.getReminderMinutes());
        if (request.getRepeatType() != null)
            schedule.setRepeatType(request.getRepeatType());
        if (request.getRepeatEndDate() != null)
            schedule.setRepeatEndDate(LocalDate.parse(request.getRepeatEndDate()));

        Schedule updated = scheduleRepository.save(schedule);
        return convertToDTO(updated);
    }

    /**
     * 删除日程
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchedule(String userId, String scheduleId) {
        Schedule schedule = scheduleRepository.findByScheduleIdAndIsDeleted(scheduleId, 0)
                .orElseThrow(() -> new RuntimeException("日程不存在或已被删除"));

        checkPermission(userId, schedule, "删除");

        schedule.setIsDeleted(1);
        scheduleRepository.save(schedule);
    }

    /**
     * 获取用户所属的所有群组ID
     */
    public List<String> getMemberGroupIds(String userId) {
        return userGroupRepository.findByUserIdOrderByJoinedAtDesc(userId)
                .stream()
                .map(UserGroup::getGroupId)
                .collect(Collectors.toList());
    }

    /**
     * 获取用户所属的所有群组
     *
     * @param userId 用户ID
     * @return 群组列表
     */
    public List<Group> getMemberGroups(String userId) {
        List<String> groupIds = getMemberGroupIds(userId);
        if (groupIds.isEmpty()) {
            return List.of();
        }
        return groupRepository.findAllById(groupIds);
    }

    /**
     * 解析群组ID或名称，并验证用户成员权限
     * 
     * 优先按 ID 校验成员资格；
     * 再在用户已加入群组中按名称匹配；
     * 同名群组给出提示。
     *
     * @param userId        用户ID
     * @param groupIdOrName 群组ID或名称
     * @return 解析后的群组ID，如果解析失败返回 null
     * @throws IllegalArgumentException 如果有多个同名群组
     */
    public String resolveGroupIdForUser(String userId, String groupIdOrName) {
        if (groupIdOrName == null || groupIdOrName.isBlank()) {
            return null;
        }

        // 1. 优先按 ID 尝试匹配
        UserGroup directRelation = userGroupRepository.findByUserIdAndGroupId(userId, groupIdOrName);
        if (directRelation != null) {
            return groupIdOrName;
        }

        // 2. 获取用户所有群组，按名称匹配
        List<Group> memberGroups = getMemberGroups(userId);
        if (memberGroups.isEmpty()) {
            return null;
        }

        // 按名称精确匹配
        List<Group> exactMatches = memberGroups.stream()
                .filter(g -> g.getName().equals(groupIdOrName))
                .collect(Collectors.toList());

        if (exactMatches.size() == 1) {
            return exactMatches.get(0).getId();
        }

        if (exactMatches.size() > 1) {
            throw new IllegalArgumentException(
                    String.format("您有 %d 个同名群组「%s」，请使用群组ID指定", exactMatches.size(), groupIdOrName));
        }

        // 3. 按名称子串匹配（包含关系）
        List<Group> containsMatches = memberGroups.stream()
                .filter(g -> g.getName().contains(groupIdOrName) || groupIdOrName.contains(g.getName()))
                .collect(Collectors.toList());

        if (containsMatches.size() == 1) {
            return containsMatches.get(0).getId();
        }

        if (containsMatches.size() > 1) {
            // 多个匹配，返回第一个（或者可以抛出异常让用户明确）
            log.warn("【ScheduleService】群组名称「{}」匹配到多个群组，使用第一个匹配", groupIdOrName);
            return containsMatches.get(0).getId();
        }

        return null;
    }

    /**
     * 校验用户是否为群组成员
     */
    private void validateGroupMembership(String userId, String groupId) {
        UserGroup relation = userGroupRepository.findByUserIdAndGroupId(userId, groupId);
        if (relation == null) {
            throw new SecurityException("您不是该群组成员，无法操作此群组日程");
        }
    }

    /**
     * 权限校验逻辑 (符合 V2.2 规约)
     */
    private void checkPermission(String userId, Schedule schedule, String action) {
        if (schedule.getUserId().equals(userId)) {
            return;
        }
        if (schedule.getGroupId() != null) {
            UserGroup relation = userGroupRepository.findByUserIdAndGroupId(userId, schedule.getGroupId());
            if (relation != null && ("owner".equals(relation.getRole()) || "admin".equals(relation.getRole()))) {
                return;
            }
        }
        throw new SecurityException("权限不足，无法" + action + "此日程");
    }

    /**
     * 批量填充来源名称 (符合 V1.2 规约)
     * 使用 Map 批量缓存群组名称，避免在循环中频繁查库 (类似 JOIN 效果)
     */
    private List<ScheduleDTO> enrichScheduleDTOs(List<Schedule> schedules) {
        // 提取所有不为空的 groupId
        List<String> groupIds = schedules.stream()
                .map(Schedule::getGroupId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询群组名称映射
        Map<String, String> groupNameMap = groupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(Group::getId, Group::getName));

        return schedules.stream()
                .map(s -> {
                    ScheduleDTO dto = convertToDTO(s);
                    if (s.getGroupId() != null) {
                        dto.setGroupName(groupNameMap.getOrDefault(s.getGroupId(), "未知群组"));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private ScheduleDTO convertToDTO(Schedule s) {
        ScheduleDTO dto = ScheduleDTO.fromEntity(s);
        // 单个转换时也填充名称
        if (s.getGroupId() != null) {
            groupRepository.findById(s.getGroupId()).ifPresent(g -> dto.setGroupName(g.getName()));
        }
        return dto;
    }
}
