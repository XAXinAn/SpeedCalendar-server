package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.ActivityMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 活动消息数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Repository
public interface ActivityMessageRepository extends JpaRepository<ActivityMessage, String> {

    /**
     * 获取所有有效且未过期的消息（分页）
     *
     * @param now      当前时间
     * @param pageable 分页参数
     * @return 消息分页列表
     */
    @Query("SELECT m FROM ActivityMessage m " +
            "WHERE m.isActive = true " +
            "AND (m.expireAt IS NULL OR m.expireAt > :now) " +
            "ORDER BY m.sortOrder DESC, m.createdAt DESC")
    Page<ActivityMessage> findActiveMessages(
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * 获取所有有效且未过期的消息（不分页）
     *
     * @param now 当前时间
     * @return 消息列表
     */
    @Query("SELECT m FROM ActivityMessage m " +
            "WHERE m.isActive = true " +
            "AND (m.expireAt IS NULL OR m.expireAt > :now) " +
            "ORDER BY m.sortOrder DESC, m.createdAt DESC")
    List<ActivityMessage> findAllActiveMessages(@Param("now") LocalDateTime now);

    /**
     * 统计有效且未过期的消息数量
     *
     * @param now 当前时间
     * @return 消息数量
     */
    @Query("SELECT COUNT(m) FROM ActivityMessage m " +
            "WHERE m.isActive = true " +
            "AND (m.expireAt IS NULL OR m.expireAt > :now)")
    long countActiveMessages(@Param("now") LocalDateTime now);
}
