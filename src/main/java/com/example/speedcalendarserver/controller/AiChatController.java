package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.*;
import com.example.speedcalendarserver.entity.ChatMessage;
import com.example.speedcalendarserver.entity.ChatSession;
import com.example.speedcalendarserver.service.AiChatService;
import com.example.speedcalendarserver.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 聊天控制器
 * 处理 AI 聊天相关的请求，包括会话管理和消息发送
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;
    private final JwtUtil jwtUtil;

    /**
     * 获取聊天会话列表
     *
     * GET /api/ai/sessions
     * Headers: Authorization: Bearer {token}
     * 响应: {
     * "code": 200,
     * "message": "获取成功",
     * "data": [
     * {
     * "sessionId": "xxx",
     * "userId": "xxx",
     * "title": "会话标题",
     * "status": 1,
     * "messageCount": 5,
     * "createdAt": "2025-12-17T10:00:00",
     * "updatedAt": "2025-12-17T10:30:00",
     * "lastMessageAt": "2025-12-17T10:30:00"
     * }
     * ]
     * }
     *
     * @param httpRequest HTTP请求
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionDTO>> getSessions(HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【获取会话列表】userId: {}", userId);

            List<ChatSession> sessions = aiChatService.getUserSessions(userId);

            List<ChatSessionDTO> sessionDTOs = sessions.stream()
                    .map(session -> ChatSessionDTO.builder()
                            .sessionId(session.getSessionId())
                            .userId(session.getUserId())
                            .title(session.getTitle() != null ? session.getTitle() : "新对话")
                            .status(session.getStatus())
                            .messageCount(session.getMessageCount())
                            .createdAt(session.getCreatedAt())
                            .updatedAt(session.getUpdatedAt())
                            .lastMessageAt(session.getLastMessageAt())
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.success("获取成功", sessionDTOs);
        } catch (Exception e) {
            log.error("【获取会话列表失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 创建新会话
     *
     * POST /api/ai/sessions
     * Headers: Authorization: Bearer {token}
     * Body: 空（无需请求体）
     * 响应: {
     * "code": 200,
     * "message": "创建成功",
     * "data": {
     * "sessionId": "xxx",
     * "userId": "xxx",
     * "title": "新对话",
     * "status": 1,
     * "messageCount": 0,
     * "createdAt": "2025-12-17T12:00:00",
     * "updatedAt": "2025-12-17T12:00:00",
     * "lastMessageAt": null
     * }
     * }
     *
     * @param request     创建会话请求（可选）
     * @param httpRequest HTTP请求
     * @return 新创建的会话
     */
    @PostMapping("/sessions")
    public ApiResponse<ChatSessionDTO> createSession(
            @RequestBody(required = false) CreateSessionRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【创建新会话】userId: {}", userId);

            ChatSession session = aiChatService.createSession(userId);

            ChatSessionDTO sessionDTO = ChatSessionDTO.builder()
                    .sessionId(session.getSessionId())
                    .userId(session.getUserId())
                    .title(session.getTitle() != null ? session.getTitle() : "新对话")
                    .status(session.getStatus())
                    .messageCount(session.getMessageCount())
                    .createdAt(session.getCreatedAt())
                    .updatedAt(session.getUpdatedAt())
                    .lastMessageAt(session.getLastMessageAt())
                    .build();

            return ApiResponse.success("创建成功", sessionDTO);
        } catch (Exception e) {
            log.error("【创建会话失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送消息（SSE流式响应）
     *
     * POST /api/ai/sessions/{sessionId}/messages
     * Headers: Authorization: Bearer {token}, Accept: text/event-stream
     * Body: { "content": "用户消息" }
     * 响应: SSE 流式事件
     *
     * @param sessionId   会话ID
     * @param request     发送消息请求
     * @param httpRequest HTTP请求
     * @return SSE 流式响应
     */
    @PostMapping(value = "/sessions/{sessionId}/messages", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sendMessageStream(
            @PathVariable String sessionId,
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest) {

        // 创建 SSE 发射器，设置超时时间为 5 分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        String userId = getUserIdFromRequest(httpRequest);
        if (userId == null) {
            try {
                emitter.send(SseEmitter.event().data("{\"error\": \"未授权，请先登录\", \"done\": true}"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        // 支持从 path 或 body 获取 sessionId
        String effectiveSessionId = sessionId != null && !sessionId.equals("new") ? sessionId : request.getSessionId();
        String title = request.getTitle();

        // 兼容旧的 message 字段和新的 content 字段
        String messageContent = request.getContent() != null ? request.getContent() : request.getMessage();

        if (messageContent == null || messageContent.isBlank()) {
            try {
                emitter.send(SseEmitter.event().data("{\"error\": \"消息内容不能为空\", \"done\": true}"));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            }
            return emitter;
        }

        log.info("【流式发送消息】userId: {}, sessionId: {}, title: {}, message: {}",
                userId, effectiveSessionId, title, truncateMessage(messageContent, 100));

        try {
            // 调用流式服务
            aiChatService.sendMessageStream(effectiveSessionId, userId, messageContent, title, emitter);
        } catch (IllegalArgumentException e) {
            log.warn("【流式发送消息失败】{}", e.getMessage());
            try {
                emitter.send(SseEmitter.event().data(
                        String.format("{\"error\": \"%s\", \"done\": true}", escapeJson(e.getMessage()))));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        } catch (Exception e) {
            log.error("【流式发送消息失败】{}", e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event().data("{\"error\": \"AI服务暂时不可用，请稍后重试\", \"done\": true}"));
                emitter.complete();
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }

        return emitter;
    }

    /**
     * 发送消息（非流式，兼容旧接口）
     *
     * POST /api/ai/chat/message
     */
    @PostMapping("/chat/message")
    public ApiResponse<ChatMessageResponse> sendMessageLegacy(
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            String sessionId = request.getSessionId();
            String title = request.getTitle();
            String messageContent = request.getContent() != null ? request.getContent() : request.getMessage();

            log.info("【发送消息】userId: {}, sessionId: {}, title: {}, message: {}",
                    userId, sessionId, title, truncateMessage(messageContent, 100));

            ChatMessage aiReply = aiChatService.sendMessage(sessionId, userId, messageContent, title);

            ChatMessageResponse response = ChatMessageResponse.builder()
                    .sessionId(aiReply.getSessionId())
                    .message(aiReply.getContent())
                    .timestamp(aiReply.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

            return ApiResponse.success("发送成功", response);
        } catch (IllegalArgumentException e) {
            log.warn("【发送消息失败】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("【AI服务异常】{}", e.getMessage(), e);
            return ApiResponse.error(HttpStatus.SERVICE_UNAVAILABLE.value(), "AI服务暂时不可用，请稍后重试");
        } catch (Exception e) {
            log.error("【发送消息失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取会话历史消息
     *
     * GET /api/ai/sessions/{sessionId}/messages
     * Headers: Authorization: Bearer {token}
     * 响应: {
     * "code": 200,
     * "message": "获取成功",
     * "data": {
     * "messages": [
     * {
     * "id": "xxx",
     * "sessionId": "xxx",
     * "content": "消息内容",
     * "role": "user",
     * "tokensUsed": 10,
     * "sequenceNum": 1,
     * "createdAt": "2025-12-17T10:00:00"
     * }
     * ]
     * }
     * }
     *
     * @param sessionId   会话ID
     * @param httpRequest HTTP请求
     * @return 聊天记录
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<ChatHistoryResponse> getChatHistory(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【获取聊天记录】userId: {}, sessionId: {}", userId, sessionId);

            List<ChatMessage> messages = aiChatService.getChatHistory(sessionId, userId);

            List<ChatHistoryMessageDTO> messageDTOs = messages.stream()
                    .map(msg -> ChatHistoryMessageDTO.builder()
                            .id(String.valueOf(msg.getId()))
                            .sessionId(msg.getSessionId())
                            .content(msg.getContent())
                            .role(msg.getRole().name())
                            .tokensUsed(msg.getTokensUsed())
                            .sequenceNum(msg.getSequenceNum())
                            .createdAt(msg.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());

            ChatHistoryResponse response = ChatHistoryResponse.builder()
                    .messages(messageDTOs)
                    .build();

            return ApiResponse.success("获取成功", response);
        } catch (IllegalArgumentException e) {
            log.warn("【获取聊天记录失败】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【获取聊天记录失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取聊天记录（兼容旧接口）
     */
    @GetMapping("/chat/history/{sessionId}")
    public ApiResponse<ChatHistoryResponse> getChatHistoryLegacy(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        return getChatHistory(sessionId, httpRequest);
    }

    /**
     * 删除会话
     *
     * DELETE /api/ai/chat/sessions/{sessionId}
     * Headers: Authorization: Bearer {token}
     * 响应: {
     * "code": 200,
     * "message": "删除成功",
     * "data": null
     * }
     *
     * @param sessionId   会话ID
     * @param httpRequest HTTP请求
     * @return 操作结果
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Void> deleteSession(
            @PathVariable String sessionId,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【删除会话】userId: {}, sessionId: {}", userId, sessionId);

            aiChatService.deleteSession(sessionId, userId);

            return ApiResponse.success("删除成功", null);
        } catch (IllegalArgumentException e) {
            log.warn("【删除会话失败】{}", e.getMessage());
            return ApiResponse.error(HttpStatus.NOT_FOUND.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【删除会话失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 从请求中获取用户ID
     *
     * @param request HTTP请求
     * @return 用户ID，如果token无效返回null
     */
    private String getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUserIdFromToken(token);
            }
        }
        return null;
    }

    /**
     * 截断消息内容用于日志或预览
     *
     * @param message   原始消息
     * @param maxLength 最大长度
     * @return 截断后的消息
     */
    private String truncateMessage(String message, int maxLength) {
        if (message == null) {
            return "";
        }
        if (message.length() <= maxLength) {
            return message;
        }
        return message.substring(0, maxLength) + "...";
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
}
