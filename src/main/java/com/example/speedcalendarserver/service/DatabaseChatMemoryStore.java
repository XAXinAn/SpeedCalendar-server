package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.entity.ChatMessage;
import com.example.speedcalendarserver.repository.ChatMessageRepository;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库聊天记忆存储
 * 从数据库加载历史消息，实现持久化的会话记忆
 * 
 * <p>
 * 实现 LangChain4j 的 ChatMemoryStore 接口，使 AI 能够记住同一会话中的上下文。
 * 
 * <p>
 * 注意：使用内存缓存避免重复从数据库加载和消息重复问题。
 * 首次加载时从数据库读取，之后由 LangChain4j 管理内存中的消息列表。
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseChatMemoryStore implements ChatMemoryStore {

    private final ChatMessageRepository chatMessageRepository;

    // 内存缓存：sessionId -> 消息列表（首次从数据库加载，之后在内存中管理）
    private final Map<String, List<dev.langchain4j.data.message.ChatMessage>> memoryCache = new ConcurrentHashMap<>();

    /**
     * 获取指定会话的消息列表
     * 首次调用时从数据库加载历史消息，之后从内存缓存读取
     *
     * @param sessionId 会话ID
     * @return LangChain4j 格式的消息列表
     */
    @Override
    public List<dev.langchain4j.data.message.ChatMessage> getMessages(Object sessionId) {
        String sid = sessionId.toString();

        // 如果缓存中没有，从数据库加载
        if (!memoryCache.containsKey(sid)) {
            log.info("【ChatMemoryStore】首次加载会话 {} 的历史消息", sid);
            List<dev.langchain4j.data.message.ChatMessage> messages = loadFromDatabase(sid);
            memoryCache.put(sid, messages);
            log.info("【ChatMemoryStore】会话 {} 加载了 {} 条历史消息", sid, messages.size());
        }

        return memoryCache.get(sid);
    }

    /**
     * 从数据库加载历史消息
     */
    private List<dev.langchain4j.data.message.ChatMessage> loadFromDatabase(String sessionId) {
        List<ChatMessage> dbMessages = chatMessageRepository.findBySessionIdOrderBySequenceNumAsc(sessionId);
        List<dev.langchain4j.data.message.ChatMessage> messages = new ArrayList<>();

        for (ChatMessage msg : dbMessages) {
            if (msg.getRole() == ChatMessage.MessageRole.user) {
                messages.add(UserMessage.from(msg.getContent()));
            } else if (msg.getRole() == ChatMessage.MessageRole.assistant) {
                messages.add(AiMessage.from(msg.getContent()));
            }
        }

        return messages;
    }

    /**
     * 更新会话消息（由 LangChain4j 调用）
     * 直接更新内存缓存，数据库保存由 AiChatService 处理
     *
     * @param sessionId 会话ID
     * @param messages  最新的消息列表
     */
    @Override
    public void updateMessages(Object sessionId, List<dev.langchain4j.data.message.ChatMessage> messages) {
        String sid = sessionId.toString();
        // 更新内存缓存
        memoryCache.put(sid, new ArrayList<>(messages));
        log.debug("【ChatMemoryStore】会话 {} 内存缓存更新，当前 {} 条消息", sid, messages.size());
    }

    /**
     * 删除会话记忆
     *
     * @param sessionId 会话ID
     */
    @Override
    public void deleteMessages(Object sessionId) {
        String sid = sessionId.toString();
        memoryCache.remove(sid);
        log.debug("【ChatMemoryStore】删除会话 {} 的记忆缓存", sid);
    }

    /**
     * 清除指定会话的缓存（供外部调用，比如会话删除时）
     */
    public void clearCache(String sessionId) {
        memoryCache.remove(sessionId);
    }

    /**
     * 清除所有缓存
     */
    public void clearAllCache() {
        memoryCache.clear();
    }
}
