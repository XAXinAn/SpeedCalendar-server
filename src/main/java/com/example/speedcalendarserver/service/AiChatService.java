package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.entity.ChatMessage;
import com.example.speedcalendarserver.entity.ChatSession;
import com.example.speedcalendarserver.repository.ChatMessageRepository;
import com.example.speedcalendarserver.repository.ChatSessionRepository;
import com.example.speedcalendarserver.util.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * AI 聊天服务
 * 提供与 AI 模型交互的核心功能，支持会话管理和上下文维护
 * 
 * <p>
 * 使用 CalendarAssistant（AiServices）进行 AI 对话，支持工具调用（Tool Calling）。
 * 通过 UserContextHolder 传递用户上下文给 CalendarTools。
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final CalendarAssistant calendarAssistant;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * 创建新的聊天会话
     *
     * @param userId 用户ID
     * @return 新创建的会话
     */
    @Transactional
    public ChatSession createSession(String userId) {
        ChatSession session = ChatSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(userId)
                // TODO: 异步生成会话标题时需显式传入 userId，不能使用 UserContextHolder
                // 示例：generateSessionTitleAsync(sessionId, userId, firstMessage)
                .title(null)
                .status(1)
                .messageCount(0)
                .isDeleted(0)
                .build();

        return chatSessionRepository.save(session);
    }

    /**
     * 获取用户的所有会话列表
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    public List<ChatSession> getUserSessions(String userId) {
        return chatSessionRepository.findByUserIdAndIsDeletedOrderByLastMessageAtDesc(userId, 0);
    }

    /**
     * 获取会话的聊天历史记录
     *
     * @param sessionId 会话ID
     * @param userId    用户ID（用于会话隔离校验）
     * @return 消息列表
     * @throws IllegalArgumentException 如果会话不存在或不属于该用户
     */
    public List<ChatMessage> getChatHistory(String sessionId, String userId) {
        // 会话隔离校验
        validateSessionAccess(sessionId, userId);

        return chatMessageRepository.findBySessionIdAndUserIdOrderBySequenceNumAsc(sessionId, userId);
    }

    /**
     * 校验会话访问权限
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @throws IllegalArgumentException 如果会话不存在或不属于该用户
     */
    private void validateSessionAccess(String sessionId, String userId) {
        chatSessionRepository
                .findBySessionIdAndUserIdAndIsDeleted(sessionId, userId, 0)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权访问"));
    }

    /**
     * 发送消息并获取 AI 回复
     * 
     * <p>
     * 此方法使用 UserContextHolder 传递用户ID给 CalendarTools，
     * 确保在 try-finally 中正确设置和清理用户上下文。
     * 
     * <p>
     * 注意：此方法不使用 @Transactional，因为：
     * 1. AI 调用是长时间操作，不应该持有事务
     * 2. CalendarTools 中的工具方法需要独立事务（如 createSchedule）
     * 3. 避免嵌套事务导致的 rollback-only 问题
     *
     * @param sessionId   会话ID（可为null，将自动创建新会话）
     * @param userId      用户ID
     * @param userMessage 用户消息内容
     * @return AI 回复的消息
     * @throws IllegalArgumentException 如果会话不存在或不属于该用户
     */
    public ChatMessage sendMessage(String sessionId, String userId, String userMessage) {
        // 设置用户上下文，供 CalendarTools 使用
        UserContextHolder.setUserId(userId);

        try {
            ChatSession session;

            // 如果没有提供会话ID，创建新会话
            if (sessionId == null || sessionId.isBlank()) {
                session = createSession(userId);
                sessionId = session.getSessionId();
                log.info("为用户 {} 创建新会话: {}", userId, sessionId);
            } else {
                // 会话隔离校验
                session = chatSessionRepository
                        .findBySessionIdAndUserIdAndIsDeleted(sessionId, userId, 0)
                        .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权访问"));
            }

            // 获取当前最大序号
            Integer maxSequenceNum = chatMessageRepository.findMaxSequenceNum(sessionId);

            // 调用 CalendarAssistant 获取 AI 回复（支持工具调用和会话记忆）
            // 注意：用户消息在 AI 调用之后保存，避免与 ChatMemoryStore 冲突
            String aiReply;
            try {
                // 生成当前日期字符串，格式：yyyy-MM-dd（星期X）
                String currentDate = getCurrentDateString();
                // 传入 sessionId，LangChain4j 会自动从数据库加载历史消息作为上下文
                aiReply = calendarAssistant.chat(sessionId, currentDate, userMessage);
            } catch (Exception e) {
                log.error("调用 AI 模型失败: {}", e.getMessage(), e);
                throw new RuntimeException("AI 服务暂时不可用，请稍后重试", e);
            }

            // AI 调用成功后，保存用户消息和 AI 回复到数据库
            saveUserMessage(sessionId, userId, userMessage, maxSequenceNum + 1);
            ChatMessage aiMsg = saveAiReplyAndUpdateSession(session, sessionId, userId, aiReply, maxSequenceNum + 2);

            log.info("会话 {} 完成一轮对话，当前消息数: {}", sessionId, session.getMessageCount() + 2);

            return aiMsg;
        } finally {
            // 清理用户上下文，防止线程复用导致的数据污染
            UserContextHolder.clear();
        }
    }

    /**
     * 保存用户消息（独立事务）
     */
    @Transactional
    public void saveUserMessage(String sessionId, String userId, String content, int sequenceNum) {
        ChatMessage userMsg = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(ChatMessage.MessageRole.user)
                .content(content)
                .sequenceNum(sequenceNum)
                .build();
        chatMessageRepository.save(userMsg);
    }

    /**
     * 保存 AI 回复并更新会话信息（独立事务）
     */
    @Transactional
    public ChatMessage saveAiReplyAndUpdateSession(ChatSession session, String sessionId, String userId,
            String aiReply, int sequenceNum) {
        // 保存 AI 回复
        ChatMessage aiMsg = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(userId)
                .role(ChatMessage.MessageRole.assistant)
                .content(aiReply)
                .sequenceNum(sequenceNum)
                .build();
        chatMessageRepository.save(aiMsg);

        // 更新会话信息
        session.setMessageCount(session.getMessageCount() + 2);
        session.setLastMessageAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        return aiMsg;
    }

    /**
     * 删除会话（逻辑删除）
     *
     * @param sessionId 会话ID
     * @param userId    用户ID（用于会话隔离校验）
     * @throws IllegalArgumentException 如果会话不存在或不属于该用户
     */
    @Transactional
    public void deleteSession(String sessionId, String userId) {
        ChatSession session = chatSessionRepository
                .findBySessionIdAndUserIdAndIsDeleted(sessionId, userId, 0)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权访问"));

        session.setIsDeleted(1);
        chatSessionRepository.save(session);
        log.info("用户 {} 删除会话: {}", userId, sessionId);
    }

    /**
     * 获取会话详情
     *
     * @param sessionId 会话ID
     * @param userId    用户ID（用于会话隔离校验）
     * @return 会话信息
     * @throws IllegalArgumentException 如果会话不存在或不属于该用户
     */
    public ChatSession getSession(String sessionId, String userId) {
        return chatSessionRepository
                .findBySessionIdAndUserIdAndIsDeleted(sessionId, userId, 0)
                .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权访问"));
    }

    /**
     * 获取当前日期字符串，包含星期信息
     * 格式：yyyy-MM-dd（星期X）HH:mm
     *
     * @return 格式化的日期时间字符串
     */
    private String getCurrentDateString() {
        LocalDateTime now = LocalDateTime.now();
        String dateStr = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String timeStr = now.format(DateTimeFormatter.ofPattern("HH:mm"));
        String weekDay = getChineseWeekDay(now.getDayOfWeek());
        return dateStr + "（" + weekDay + "）" + timeStr;
    }

    /**
     * 将 DayOfWeek 转换为中文星期
     */
    private String getChineseWeekDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "星期一";
            case TUESDAY -> "星期二";
            case WEDNESDAY -> "星期三";
            case THURSDAY -> "星期四";
            case FRIDAY -> "星期五";
            case SATURDAY -> "星期六";
            case SUNDAY -> "星期日";
        };
    }
}
