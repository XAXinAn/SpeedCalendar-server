package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 日程数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    /**
     * 查找指定用户在某个日期范围内的所有日程（未删除）
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 日程列表
     */
    @Query("SELECT s FROM Schedule s WHERE s.userId = :userId " +
           "AND s.scheduleDate >= :startDate AND s.scheduleDate < :endDate " +
           "AND s.isDeleted = 0 " +
           "ORDER BY s.scheduleDate ASC, s.startTime ASC")
    List<Schedule> findByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * 根据日程ID和用户ID查找日程（未删除）
     *
     * @param scheduleId 日程ID
     * @param userId 用户ID
     * @return 日程Optional
     */
    Optional<Schedule> findByScheduleIdAndUserIdAndIsDeleted(String scheduleId, String userId, Integer isDeleted);

    /**
     * 根据日程ID查找日程（包含已删除）
     *
     * @param scheduleId 日程ID
     * @return 日程Optional
     */
    Optional<Schedule> findByScheduleId(String scheduleId);
}
