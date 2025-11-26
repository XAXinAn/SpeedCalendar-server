package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 聊天会话数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, String> {

    /**
     * 根据用户ID查找所有活跃的会话（未删除），按最后消息时间倒序
     *
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @return 会话列表
     */
    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.isDeleted = :isDeleted " +
            "ORDER BY s.lastMessageAt DESC NULLS LAST, s.createdAt DESC")
    List<ChatSession> findByUserIdAndIsDeletedOrderByLastMessageAtDesc(
            @Param("userId") String userId,
            @Param("isDeleted") Integer isDeleted);

    /**
     * 根据会话ID和用户ID查找会话（会话隔离校验）
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @return 会话Optional
     */
    Optional<ChatSession> findBySessionIdAndUserIdAndIsDeleted(
            String sessionId,
            String userId,
            Integer isDeleted);

    /**
     * 根据会话ID查找会话
     *
     * @param sessionId 会话ID
     * @return 会话Optional
     */
    Optional<ChatSession> findBySessionId(String sessionId);

    /**
     * 统计用户的会话数量（未删除）
     *
     * @param userId    用户ID
     * @param isDeleted 是否删除
     * @return 会话数量
     */
    long countByUserIdAndIsDeleted(String userId, Integer isDeleted);
}
