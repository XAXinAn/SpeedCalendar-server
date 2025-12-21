package com.example.speedcalendarserver.config;

import com.example.speedcalendarserver.service.CalendarAssistant;
import com.example.speedcalendarserver.service.CalendarTools;
import com.example.speedcalendarserver.service.DatabaseChatMemoryStore;
import com.example.speedcalendarserver.service.StreamingCalendarAssistant;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AI 配置类
 * 用于配置 LangChain4j 和硅基流动 API 相关设置
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@Configuration
public class AiConfig {

    private final DatabaseChatMemoryStore chatMemoryStore;

    public AiConfig(DatabaseChatMemoryStore chatMemoryStore) {
        this.chatMemoryStore = chatMemoryStore;
    }

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String siliconApiKey;

    @Value("${langchain4j.open-ai.chat-model.model-name:}")
    private String modelName;

    @Value("${langchain4j.open-ai.chat-model.base-url:}")
    private String baseUrl;

    /**
     * 创建流式聊天模型 Bean
     * Spring Boot Starter 默认只创建非流式模型，需要手动创建流式模型
     *
     * @return StreamingChatModel 实例
     */
    @Bean
    public StreamingChatModel streamingChatModel() {
        log.info("正在创建 StreamingChatModel，baseUrl: {}, model: {}", baseUrl, modelName);

        return OpenAiStreamingChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(siliconApiKey)
                .modelName(modelName)
                .timeout(Duration.ofSeconds(120))
                .build();
    }

    /**
     * 创建日历智能助手 Bean
     * 使用 AiServices 构建，绑定 ChatLanguageModel 和 CalendarTools
     *
     * @param chatModel     LangChain4j 自动配置的聊天模型
     * @param calendarTools 日历工具类（通过方法参数注入，避免循环依赖）
     * @return CalendarAssistant 实例
     */
    @Bean
    public CalendarAssistant calendarAssistant(ChatModel chatModel, CalendarTools calendarTools) {
        log.info("正在构建 CalendarAssistant，绑定工具和会话记忆");
        log.info("CalendarTools 类型: {}", calendarTools.getClass().getName());

        CalendarAssistant assistant = AiServices.builder(CalendarAssistant.class)
                .chatModel(chatModel)
                .tools(calendarTools)
                // 为每个会话提供独立的记忆，从数据库加载历史消息
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.builder()
                        .id(sessionId)
                        .maxMessages(20) // 保留最近 20 条消息作为上下文
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .build();

        log.info("CalendarAssistant 构建完成，已启用工具调用和会话记忆功能");
        return assistant;
    }

    /**
     * 创建流式日历智能助手 Bean
     * 支持 SSE 流式响应
     *
     * @param streamingChatModel LangChain4j 自动配置的流式聊天模型
     * @param calendarTools      日历工具类（通过方法参数注入，避免循环依赖）
     * @return StreamingCalendarAssistant 实例
     */
    @Bean
    public StreamingCalendarAssistant streamingCalendarAssistant(StreamingChatModel streamingChatModel,
            CalendarTools calendarTools) {
        log.info("正在构建 StreamingCalendarAssistant，绑定工具和会话记忆");
        log.info("CalendarTools 类型: {}", calendarTools.getClass().getName());

        StreamingCalendarAssistant assistant = AiServices.builder(StreamingCalendarAssistant.class)
                .streamingChatModel(streamingChatModel)
                .tools(calendarTools)
                // 为每个会话提供独立的记忆，从数据库加载历史消息
                .chatMemoryProvider(sessionId -> MessageWindowChatMemory.builder()
                        .id(sessionId)
                        .maxMessages(20) // 保留最近 20 条消息作为上下文
                        .chatMemoryStore(chatMemoryStore)
                        .build())
                .build();

        log.info("StreamingCalendarAssistant 构建完成，已启用流式响应、工具调用和会话记忆功能");
        return assistant;
    }

    /**
     * 应用启动时校验 SILICON_API_KEY 环境变量
     * 如果未配置则抛出异常，阻止应用启动
     */
    @PostConstruct
    public void validateApiKey() {
        if (siliconApiKey == null || siliconApiKey.isBlank()) {
            log.error("============================================");
            log.error("错误: SILICON_API_KEY 环境变量未设置!");
            log.error("请设置环境变量 SILICON_API_KEY 后重新启动应用");
            log.error("============================================");
            throw new IllegalStateException("SILICON_API_KEY 环境变量未设置，无法启动 AI 服务");
        }

        log.info("============================================");
        log.info("AI 配置加载成功:");
        log.info("  - API Base URL: {}", baseUrl);
        log.info("  - Model: {}", modelName);
        log.info("  - API Key: {}...(已隐藏)", siliconApiKey.substring(0, Math.min(8, siliconApiKey.length())));
        log.info("  - 工具调用: 已启用 (CalendarTools)");
        log.info("============================================");
    }
}
