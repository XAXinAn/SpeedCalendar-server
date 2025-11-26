package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天消息数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 获取会话的所有消息（按序号升序排列）
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessage> findBySessionIdOrderBySequenceNumAsc(String sessionId);

    /**
     * 获取会话的最近N条消息（用于构建上下文窗口）
     * 按序号倒序查询，调用方需要反转结果
     *
     * @param sessionId 会话ID
     * @param pageable  分页参数（限制数量）
     * @return 消息列表（倒序）
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId " +
            "ORDER BY m.sequenceNum DESC")
    List<ChatMessage> findRecentMessages(
            @Param("sessionId") String sessionId,
            Pageable pageable);

    /**
     * 获取会话的最大消息序号
     *
     * @param sessionId 会话ID
     * @return 最大序号，如果没有消息则返回0
     */
    @Query("SELECT COALESCE(MAX(m.sequenceNum), 0) FROM ChatMessage m " +
            "WHERE m.sessionId = :sessionId")
    Integer findMaxSequenceNum(@Param("sessionId") String sessionId);

    /**
     * 根据会话ID和用户ID获取消息（会话隔离校验）
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @return 消息列表
     */
    List<ChatMessage> findBySessionIdAndUserIdOrderBySequenceNumAsc(
            String sessionId,
            String userId);

    /**
     * 统计会话的消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    long countBySessionId(String sessionId);

    /**
     * 获取会话的最后一条消息
     *
     * @param sessionId 会话ID
     * @return 最后一条消息
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId " +
            "ORDER BY m.sequenceNum DESC LIMIT 1")
    ChatMessage findLastMessage(@Param("sessionId") String sessionId);
}
