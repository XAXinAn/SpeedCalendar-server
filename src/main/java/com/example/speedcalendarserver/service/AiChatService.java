package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.entity.ChatMessage;
import com.example.speedcalendarserver.entity.ChatSession;
import com.example.speedcalendarserver.repository.ChatMessageRepository;
import com.example.speedcalendarserver.repository.ChatSessionRepository;
import com.example.speedcalendarserver.util.UserContextHolder;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final StreamingCalendarAssistant streamingCalendarAssistant;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final DatabaseChatMemoryStore chatMemoryStore;

    /**
     * 创建新的聊天会话
     *
     * @param userId 用户ID
     * @return 新创建的会话
     */
    @Transactional
    public ChatSession createSession(String userId) {
        return createSession(userId, null);
    }

    /**
     * 创建新的聊天会话（可带标题）
     */
    public ChatSession createSession(String userId, String title) {
        ChatSession session = ChatSession.builder()
                .sessionId(UUID.randomUUID().toString())
                .userId(userId)
                // TODO: 异步生成会话标题时需显式传入 userId，不能使用 UserContextHolder
                // 示例：generateSessionTitleAsync(sessionId, userId, firstMessage)
                .title(title)
                .status(1)
                .messageCount(0)
                .isDeleted(0)
                .build();

        if (title != null && !title.isBlank()) {
            log.info("🏷️ 新会话标题: {}", title);
        }

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
    public ChatMessage sendMessage(String sessionId, String userId, String userMessage, String title) {
        // 设置用户上下文，供 CalendarTools 使用
        UserContextHolder.setUserId(userId);

        try {
            ChatSession session;

            // 如果没有提供会话ID，创建新会话
            if (sessionId == null || sessionId.isBlank()) {
                log.info("准备为用户 {} 创建新会话...", userId); // <--- 增加这行日志
                session = createSession(userId, title);
                sessionId = session.getSessionId();
                log.info("为用户 {} 创建新会话成功: {}", userId, sessionId);
            } else {
                // 会话隔离校验
                session = chatSessionRepository
                        .findBySessionIdAndUserIdAndIsDeleted(sessionId, userId, 0)
                        .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权访问"));

                // 如果传入了标题且原会话未命名，补全标题
                if ((session.getTitle() == null || session.getTitle().isBlank())
                        && title != null && !title.isBlank()) {
                    session.setTitle(title);
                    chatSessionRepository.save(session);
                    log.info("🏷️ 更新会话标题: {} -> {}", sessionId, title);
                }
            }

            // 将 sessionId 记录到线程上下文，工具可通过 SESSION_USER_MAP 回溯 userId
            UserContextHolder.setSessionId(sessionId);

            // 获取当前最大序号
            Integer maxSequenceNum = chatMessageRepository.findMaxSequenceNum(sessionId);

            // 调用 CalendarAssistant 获取 AI 回复（支持工具调用和会话记忆）
            // 注意：用户消息在 AI 调用之后保存，避免与 ChatMemoryStore 冲突
            String aiReply;
            try {
                // 生成当前日期字符串，格式：yyyy-MM-dd（星期X）
                String currentDate = getCurrentDateString();
                // 传入 sessionId，LangChain4j 会自动从数据库加载历史消息作为上下文
                aiReply = calendarAssistant.chat(sessionId, sessionId, currentDate, userMessage);
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
     * 流式发送消息并通过 SSE 返回 AI 回复
     *
     * @param sessionId   会话ID（可为null，将自动创建新会话）
     * @param userId      用户ID
     * @param userMessage 用户消息内容
     * @param emitter     SSE 发射器
     * @return 实际使用的会话ID
     */
    public String sendMessageStream(String sessionId, String userId, String userMessage, String title,
            SseEmitter emitter) {
        final long requestStartMs = System.currentTimeMillis();
        final String traceId = UUID.randomUUID().toString().substring(0, 8);
        log.info("[AI_TIMELINE][{}] request_received userId={} sessionId={} ts={}", traceId, userId, sessionId,
                requestStartMs);
        // 设置用户上下文，供 CalendarTools 使用
        UserContextHolder.setUserId(userId);

        try {
            ChatSession session;

            // 如果没有提供会话ID，创建新会话
            if (sessionId == null || sessionId.isBlank()) {
                log.info("准备为用户 {} 创建新会话...", userId);
                session = createSession(userId, title);
                sessionId = session.getSessionId();
                log.info("为用户 {} 创建新会话成功: {}", userId, sessionId);
            } else {
                // 尝试查找现有会话
                var existingSession = chatSessionRepository
                        .findBySessionIdAndUserIdAndIsDeleted(sessionId, userId, 0);

                if (existingSession.isPresent()) {
                    session = existingSession.get();

                    // 如果传入了标题且原会话未命名，补全标题
                    if ((session.getTitle() == null || session.getTitle().isBlank())
                            && title != null && !title.isBlank()) {
                        session.setTitle(title);
                        chatSessionRepository.save(session);
                        log.info("🏷️ 更新会话标题: {} -> {}", sessionId, title);
                    }
                } else {
                    // 会话不存在，自动创建新会话
                    log.info("会话 {} 不存在，为用户 {} 创建新会话...", sessionId, userId);
                    session = createSession(userId, title);
                    sessionId = session.getSessionId();
                    log.info("为用户 {} 创建新会话成功: {}", userId, sessionId);
                }
            }

            long sessionReadyMs = System.currentTimeMillis();
            log.info("[AI_TIMELINE][{}] session_ready userId={} sessionId={} +{}ms", traceId, userId, sessionId,
                    sessionReadyMs - requestStartMs);

            // 绑定 sessionId 和 userId，供 CalendarTools 在跨线程时获取用户ID
            UserContextHolder.bindSession(sessionId, userId);
            UserContextHolder.setSessionId(sessionId);

            // 获取当前最大序号
            Integer maxSequenceNum = chatMessageRepository.findMaxSequenceNum(sessionId);

            // 先保存用户消息
            saveUserMessage(sessionId, userId, userMessage, maxSequenceNum + 1);

            long enqueueMs = System.currentTimeMillis();
            log.info("[AI_TIMELINE][{}] enqueue_model userId={} sessionId={} +{}ms", traceId, userId, sessionId,
                    enqueueMs - requestStartMs);

            // 生成当前日期字符串
            String currentDate = getCurrentDateString();

            // 用于收集完整的 AI 回复
            StringBuilder fullResponse = new StringBuilder();
            AtomicInteger tokensUsed = new AtomicInteger(0);

            // 保存会话相关信息供回调使用
            final String finalSessionId = sessionId;
            final ChatSession finalSession = session;
            final int nextSequenceNum = maxSequenceNum + 2;
            final String finalUserId = userId; // 保存 userId 供回调线程使用
            final AtomicBoolean firstTokenLogged = new AtomicBoolean(false);

            // 调用流式 API
            TokenStream tokenStream = streamingCalendarAssistant.chatStream(sessionId, sessionId, currentDate,
                    userMessage);

            long modelStartMs = System.currentTimeMillis();
            log.info("[AI_TIMELINE][{}] model_start userId={} sessionId={} +{}ms", traceId, userId, sessionId,
                    modelStartMs - requestStartMs);

            tokenStream
                    .onPartialResponse(partialResponse -> {
                        // 在回调线程中重新设置用户上下文（线程池线程不会继承 ThreadLocal）
                        UserContextHolder.setUserId(finalUserId);
                        UserContextHolder.setSessionId(finalSessionId);
                        try {
                            String token = partialResponse;
                            fullResponse.append(token);
                            tokensUsed.addAndGet(token.length());

                            if (firstTokenLogged.compareAndSet(false, true)) {
                                long firstTokenMs = System.currentTimeMillis();
                                log.info("[AI_TIMELINE][{}] first_token userId={} sessionId={} +{}ms", traceId,
                                        finalUserId, finalSessionId, firstTokenMs - requestStartMs);
                            }

                            // 发送 SSE 事件
                            String sseData = String.format("{\"content\": \"%s\", \"done\": false}",
                                    escapeJson(token));
                            log.debug("SSE 发送: {}", sseData);
                            emitter.send(SseEmitter.event().data(sseData));
                        } catch (IOException e) {
                            log.error("发送 SSE 事件失败: {}", e.getMessage());
                        }
                    })
                    .onCompleteResponse(completeResponse -> {
                        // 在回调线程中重新设置用户上下文
                        UserContextHolder.setUserId(finalUserId);
                        UserContextHolder.setSessionId(finalSessionId);
                        try {
                            // 保存 AI 回复到数据库
                            ChatMessage aiMsg = saveAiReplyAndUpdateSession(
                                    finalSession, finalSessionId, finalUserId,
                                    fullResponse.toString(), nextSequenceNum);

                            // 发送完成事件（包含 sessionId，让前端知道实际使用的会话）
                            String doneData = String.format(
                                    "{\"content\": \"\", \"done\": true, \"sessionId\": \"%s\", \"messageId\": \"%s\", \"tokensUsed\": %d}",
                                    finalSessionId, aiMsg.getId(), tokensUsed.get());
                            log.info("SSE 完成: {}", doneData);
                            emitter.send(SseEmitter.event().data(doneData));
                            emitter.complete();

                            long completeMs = System.currentTimeMillis();
                            log.info("[AI_TIMELINE][{}] stream_complete userId={} sessionId={} +{}ms len={}", traceId,
                                    finalUserId, finalSessionId, completeMs - requestStartMs,
                                    fullResponse.length());

                            log.info("会话 {} 流式对话完成，完整回复长度: {}", finalSessionId, fullResponse.length());
                        } catch (IOException e) {
                            log.error("发送完成事件失败: {}", e.getMessage());
                            emitter.completeWithError(e);
                        } finally {
                            UserContextHolder.unbindSession(finalSessionId);
                            UserContextHolder.clear();
                        }
                    })
                    .onError(error -> {
                        log.error("流式 AI 调用失败: {}", error.getMessage(), error);
                        try {
                            String errorData = String.format(
                                    "{\"error\": \"%s\", \"done\": true}",
                                    escapeJson(error.getMessage()));
                            emitter.send(SseEmitter.event().data(errorData));
                        } catch (IOException e) {
                            log.error("发送错误事件失败: {}", e.getMessage());
                        }
                        emitter.completeWithError(error);
                        UserContextHolder.unbindSession(finalSessionId);
                        UserContextHolder.clear();
                    })
                    .start();

            return sessionId;
        } catch (Exception e) {
            UserContextHolder.clear();
            throw e;
        }
    }

    /**
     * 无状态流式对话（不创建会话、不存储消息）
     * 专为悬浮窗 OCR 快速日程场景设计
     *
     * @param userId  用户ID
     * @param prompt  用户消息（已包含"帮我添加日程："前缀）
     * @param emitter SSE 发射器
     */
    public void streamWithoutSession(String userId, String prompt, SseEmitter emitter) {
        final long requestStartMs = System.currentTimeMillis();
        final String traceId = UUID.randomUUID().toString().substring(0, 8);
        // 使用固定前缀的 sessionId，CalendarTools 通过此前缀识别快速日程场景
        final String quickSessionId = "quick-schedule-" + userId;

        log.info("[AI_TIMELINE][{}] quick_schedule_received userId={} ts={}", traceId, userId, requestStartMs);

        // 🔑 关键：清理该用户的快速日程内存缓存，确保每次都是独立的单轮对话
        chatMemoryStore.clearCache(quickSessionId);
        log.debug("[快速日程] 已清理会话 {} 的内存缓存", quickSessionId);

        // 设置用户上下文
        UserContextHolder.setUserId(userId);
        UserContextHolder.bindSession(quickSessionId, userId);
        UserContextHolder.setSessionId(quickSessionId);

        try {
            // 生成当前日期字符串
            String currentDate = getCurrentDateString();

            // 用于收集完整的 AI 回复
            StringBuilder fullResponse = new StringBuilder();
            AtomicInteger tokensUsed = new AtomicInteger(0);
            final AtomicBoolean firstTokenLogged = new AtomicBoolean(false);

            log.info("[AI_TIMELINE][{}] quick_schedule_model_start userId={} +{}ms", traceId, userId,
                    System.currentTimeMillis() - requestStartMs);

            // 调用流式 API（不使用历史消息，每次都是独立的单轮对话）
            TokenStream tokenStream = streamingCalendarAssistant.chatStream(
                    quickSessionId, quickSessionId, currentDate, prompt);

            tokenStream
                    .onPartialResponse(partialResponse -> {
                        // 在回调线程中重新设置用户上下文
                        UserContextHolder.setUserId(userId);
                        UserContextHolder.setSessionId(quickSessionId);
                        try {
                            String token = partialResponse;
                            fullResponse.append(token);
                            tokensUsed.addAndGet(token.length());

                            if (firstTokenLogged.compareAndSet(false, true)) {
                                long firstTokenMs = System.currentTimeMillis();
                                log.info("[AI_TIMELINE][{}] quick_schedule_first_token userId={} +{}ms", traceId,
                                        userId, firstTokenMs - requestStartMs);
                            }

                            // 发送 SSE 事件
                            String sseData = String.format("{\"content\": \"%s\", \"done\": false}",
                                    escapeJson(token));
                            log.debug("SSE 发送: {}", sseData);
                            emitter.send(SseEmitter.event().data(sseData));
                        } catch (IOException e) {
                            log.error("发送 SSE 事件失败: {}", e.getMessage());
                        }
                    })
                    .onCompleteResponse(completeResponse -> {
                        // 在回调线程中重新设置用户上下文
                        UserContextHolder.setUserId(userId);
                        UserContextHolder.setSessionId(quickSessionId);
                        try {
                            // 快速日程不存储消息，直接发送完成事件
                            String doneData = String.format(
                                    "{\"content\": \"\", \"done\": true, \"tokensUsed\": %d}",
                                    tokensUsed.get());
                            log.info("SSE 完成: {}", doneData);
                            emitter.send(SseEmitter.event().data(doneData));
                            emitter.complete();

                            long completeMs = System.currentTimeMillis();
                            log.info("[AI_TIMELINE][{}] quick_schedule_complete userId={} +{}ms len={}", traceId,
                                    userId, completeMs - requestStartMs, fullResponse.length());
                        } catch (IOException e) {
                            log.error("发送完成事件失败: {}", e.getMessage());
                            emitter.completeWithError(e);
                        } finally {
                            // 🔑 关键：调用完成后清理内存缓存，防止累积
                            chatMemoryStore.clearCache(quickSessionId);
                            UserContextHolder.unbindSession(quickSessionId);
                            UserContextHolder.clear();
                        }
                    })
                    .onError(error -> {
                        log.error("快速日程 AI 调用失败: {}", error.getMessage(), error);
                        try {
                            String errorData = String.format(
                                    "{\"error\": \"%s\", \"done\": true}",
                                    escapeJson(error.getMessage()));
                            emitter.send(SseEmitter.event().data(errorData));
                        } catch (IOException e) {
                            log.error("发送错误事件失败: {}", e.getMessage());
                        }
                        emitter.completeWithError(error);
                        // 🔑 关键：出错时也清理内存缓存
                        chatMemoryStore.clearCache(quickSessionId);
                        UserContextHolder.unbindSession(quickSessionId);
                        UserContextHolder.clear();
                    })
                    .start();

        } catch (Exception e) {
            // 🔑 关键：异常时也清理内存缓存
            chatMemoryStore.clearCache(quickSessionId);
            UserContextHolder.unbindSession(quickSessionId);
            UserContextHolder.clear();
            throw e;
        }
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     */
    private String escapeJson(String text) {
        if (text == null)
            return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
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
                // .orElseThrow(() -> new IllegalArgumentException("会话不存在或无权访问"));
                .orElse(null);
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
