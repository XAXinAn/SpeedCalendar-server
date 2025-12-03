package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.UserReadMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户已读消息数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Repository
public interface UserReadMessageRepository extends JpaRepository<UserReadMessage, UserReadMessage.UserReadMessageId> {

    /**
     * 获取用户已读的消息ID列表
     *
     * @param userId 用户ID
     * @return 已读消息ID集合
     */
    @Query("SELECT r.messageId FROM UserReadMessage r WHERE r.userId = :userId")
    Set<String> findReadMessageIdsByUserId(@Param("userId") String userId);

    /**
     * 检查用户是否已读某条消息
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @return 是否存在记录
     */
    boolean existsByUserIdAndMessageId(String userId, String messageId);

    /**
     * 统计用户未读消息数量
     *
     * @param userId 用户ID
     * @param now    当前时间
     * @return 未读消息数量
     */
    @Query("SELECT COUNT(m) FROM ActivityMessage m " +
            "WHERE m.isActive = true " +
            "AND (m.expireAt IS NULL OR m.expireAt > :now) " +
            "AND m.id NOT IN (SELECT r.messageId FROM UserReadMessage r WHERE r.userId = :userId)")
    long countUnreadMessages(@Param("userId") String userId, @Param("now") LocalDateTime now);

    /**
     * 获取用户未读的消息ID列表
     *
     * @param userId 用户ID
     * @param now    当前时间
     * @return 未读消息ID列表
     */
    @Query("SELECT m.id FROM ActivityMessage m " +
            "WHERE m.isActive = true " +
            "AND (m.expireAt IS NULL OR m.expireAt > :now) " +
            "AND m.id NOT IN (SELECT r.messageId FROM UserReadMessage r WHERE r.userId = :userId)")
    List<String> findUnreadMessageIds(@Param("userId") String userId, @Param("now") LocalDateTime now);
}
