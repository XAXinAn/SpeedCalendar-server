package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.*;
import com.example.speedcalendarserver.entity.ChatMessage;
import com.example.speedcalendarserver.entity.ChatSession;
import com.example.speedcalendarserver.repository.ChatMessageRepository;
import com.example.speedcalendarserver.service.AiChatService;
import com.example.speedcalendarserver.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;
    private final ChatMessageRepository chatMessageRepository;
    private final JwtUtil jwtUtil;

    /**
     * 获取聊天会话列表
     *
     * GET /api/ai/chat/sessions
     * Headers: Authorization: Bearer {token}
     * 响应: {
     * "code": 200,
     * "message": "获取成功",
     * "data": [
     * {
     * "id": "xxx",
     * "title": "会话标题",
     * "lastMessage": "最后一条消息预览",
     * "timestamp": 1234567890000
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
                    .map(session -> {
                        // 获取最后一条消息
                        ChatMessage lastMsg = chatMessageRepository.findLastMessage(session.getSessionId());
                        String lastMessage = lastMsg != null ? truncateMessage(lastMsg.getContent(), 50) : "";

                        return ChatSessionDTO.builder()
                                .id(session.getSessionId())
                                .title(session.getTitle() != null ? session.getTitle() : "新对话")
                                .lastMessage(lastMessage)
                                .timestamp(session.getLastMessageAt() != null
                                        ? session.getLastMessageAt().atZone(ZoneId.systemDefault()).toInstant()
                                                .toEpochMilli()
                                        : session.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()
                                                .toEpochMilli())
                                .build();
                    })
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
     * POST /api/ai/chat/sessions
     * Headers: Authorization: Bearer {token}
     * Body: { "userId": "xxx" }
     * 响应: {
     * "code": 200,
     * "message": "创建成功",
     * "data": {
     * "id": "xxx",
     * "title": "新对话",
     * "lastMessage": "",
     * "timestamp": 1234567890000
     * }
     * }
     *
     * @param request     创建会话请求
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
                    .id(session.getSessionId())
                    .title(session.getTitle() != null ? session.getTitle() : "新对话")
                    .lastMessage("")
                    .timestamp(session.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    .build();

            return ApiResponse.success("创建成功", sessionDTO);
        } catch (Exception e) {
            log.error("【创建会话失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 发送消息
     *
     * POST /api/ai/chat/message
     * Headers: Authorization: Bearer {token}
     * Body: {
     * "message": "用户消息",
     * "sessionId": "xxx",
     * "userId": "xxx"
     * }
     * 响应: {
     * "code": 200,
     * "message": "发送成功",
     * "data": {
     * "sessionId": "xxx",
     * "message": "AI回复内容",
     * "timestamp": 1234567890000
     * }
     * }
     *
     * @param request     发送消息请求
     * @param httpRequest HTTP请求
     * @return AI回复
     */
    @PostMapping("/message")
    public ApiResponse<ChatMessageResponse> sendMessage(
            @Valid @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【发送消息】userId: {}, sessionId: {}, message: {}",
                    userId, request.getSessionId(), truncateMessage(request.getMessage(), 100));

            ChatMessage aiReply = aiChatService.sendMessage(
                    request.getSessionId(),
                    userId,
                    request.getMessage());

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
     * 获取聊天记录
     *
     * GET /api/ai/chat/history/{sessionId}
     * Headers: Authorization: Bearer {token}
     * 响应: {
     * "code": 200,
     * "message": "获取成功",
     * "data": {
     * "messages": [
     * {
     * "id": "xxx",
     * "content": "消息内容",
     * "role": "user",
     * "timestamp": 1234567890000
     * }
     * ]
     * }
     * }
     *
     * @param sessionId   会话ID
     * @param httpRequest HTTP请求
     * @return 聊天记录
     */
    @GetMapping("/history/{sessionId}")
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
                            .content(msg.getContent())
                            // API文档中使用 user/ai，这里转换 assistant -> ai
                            .role(msg.getRole() == ChatMessage.MessageRole.assistant ? "ai" : msg.getRole().name())
                            .timestamp(msg.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
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
}
