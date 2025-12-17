package com.example.speedcalendarserver.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 日历智能助手流式接口
 * 支持 SSE 流式响应，逐字返回 AI 回复
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
public interface StreamingCalendarAssistant {

    /**
     * 与用户进行流式对话
     *
     * @param sessionId   会话ID，用于隔离不同会话的记忆
     * @param currentDate 当前日期，格式：yyyy-MM-dd（星期X）HH:mm
     * @param userMessage 用户消息
     * @return TokenStream 流式响应
     */
    @SystemMessage(CalendarAssistant.SYSTEM_PROMPT)
    TokenStream chatStream(@MemoryId String sessionId,
            @V("currentDate") String currentDate,
            @UserMessage String userMessage);
}
