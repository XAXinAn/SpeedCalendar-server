package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {

    @Query("SELECT s FROM Schedule s WHERE " +
           "s.isDeleted = 0 AND s.scheduleDate BETWEEN :startDate AND :endDate AND (" +
           "(s.groupId IS NULL AND s.userId = :userId) OR " +
           "(s.groupId IS NOT NULL AND s.groupId IN :groupIds)) " +
           "ORDER BY s.scheduleDate ASC, s.startTime ASC")
    List<Schedule> findSchedulesForUserAndGroupsByDateRange(
            @Param("userId") String userId,
            @Param("groupIds") List<String> groupIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    Optional<Schedule> findByScheduleIdAndIsDeleted(String scheduleId, Integer isDeleted);
}
