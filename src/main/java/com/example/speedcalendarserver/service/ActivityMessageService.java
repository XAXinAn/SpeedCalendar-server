package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.ActivityMessageDTO;
import com.example.speedcalendarserver.dto.ActivityMessageListResponse;
import com.example.speedcalendarserver.dto.ReadAllResponse;
import com.example.speedcalendarserver.dto.UnreadCountResponse;
import com.example.speedcalendarserver.entity.ActivityMessage;
import com.example.speedcalendarserver.entity.UserReadMessage;
import com.example.speedcalendarserver.repository.ActivityMessageRepository;
import com.example.speedcalendarserver.repository.UserReadMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 活动消息服务层
 *
 * @author SpeedCalendar Team
 * @since 2025-12-03
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityMessageService {

    private final ActivityMessageRepository activityMessageRepository;
    private final UserReadMessageRepository userReadMessageRepository;
    
    /**
     * ISO 8601 日期时间格式化器
     */
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    /**
     * 获取活动消息列表（带已读状态）
     *
     * @param userId   用户ID
     * @param page     页码（从1开始）
     * @param pageSize 每页数量
     * @return 消息列表响应
     */
    public ActivityMessageListResponse getMessages(String userId, int page, int pageSize) {
        // 限制每页最大数量
        pageSize = Math.min(pageSize, 50);
        page = Math.max(page, 1);

        LocalDateTime now = LocalDateTime.now();

        // 获取分页消息
        Page<ActivityMessage> messagePage = activityMessageRepository.findActiveMessages(
                now, PageRequest.of(page - 1, pageSize));

        // 获取用户已读的消息ID集合
        Set<String> readMessageIds = userReadMessageRepository.findReadMessageIdsByUserId(userId);

        // 转换为 DTO
        List<ActivityMessageDTO> messageDTOs = messagePage.getContent().stream()
                .map(msg -> convertToDTO(msg, readMessageIds.contains(msg.getId())))
                .collect(Collectors.toList());

        // 获取未读数量
        long unreadCount = userReadMessageRepository.countUnreadMessages(userId, now);

        return ActivityMessageListResponse.builder()
                .unreadCount(unreadCount)
                .messages(messageDTOs)
                .total(messagePage.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 未读数量响应
     */
    public UnreadCountResponse getUnreadCount(String userId) {
        LocalDateTime now = LocalDateTime.now();
        long unreadCount = userReadMessageRepository.countUnreadMessages(userId, now);

        return UnreadCountResponse.builder()
                .unreadCount(unreadCount)
                .build();
    }

    /**
     * 标记单条消息已读
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @throws IllegalArgumentException 如果消息不存在
     */
    @Transactional
    public void markAsRead(String userId, String messageId) {
        // 检查消息是否存在
        if (!activityMessageRepository.existsById(messageId)) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 检查是否已读
        if (userReadMessageRepository.existsByUserIdAndMessageId(userId, messageId)) {
            log.debug("【标记已读】消息已读，跳过: userId={}, messageId={}", userId, messageId);
            return;
        }

        // 创建已读记录
        UserReadMessage readRecord = UserReadMessage.builder()
                .userId(userId)
                .messageId(messageId)
                .readAt(LocalDateTime.now())
                .build();

        userReadMessageRepository.save(readRecord);
        log.info("【标记已读】成功: userId={}, messageId={}", userId, messageId);
    }

    /**
     * 标记全部消息已读
     *
     * @param userId 用户ID
     * @return 标记已读的消息数量
     */
    @Transactional
    public ReadAllResponse markAllAsRead(String userId) {
        LocalDateTime now = LocalDateTime.now();

        // 获取所有未读消息ID
        List<String> unreadMessageIds = userReadMessageRepository.findUnreadMessageIds(userId, now);

        if (unreadMessageIds.isEmpty()) {
            log.debug("【全部已读】无未读消息: userId={}", userId);
            return ReadAllResponse.builder().readCount(0).build();
        }

        // 批量创建已读记录
        List<UserReadMessage> readRecords = unreadMessageIds.stream()
                .map(messageId -> UserReadMessage.builder()
                        .userId(userId)
                        .messageId(messageId)
                        .readAt(now)
                        .build())
                .collect(Collectors.toList());

        userReadMessageRepository.saveAll(readRecords);
        log.info("【全部已读】成功: userId={}, readCount={}", userId, readRecords.size());

        return ReadAllResponse.builder()
                .readCount(readRecords.size())
                .build();
    }

    /**
     * 获取消息详情
     *
     * @param userId    用户ID
     * @param messageId 消息ID
     * @return 消息详情
     * @throws IllegalArgumentException 如果消息不存在
     */
    public ActivityMessageDTO getMessageDetail(String userId, String messageId) {
        ActivityMessage message = activityMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("消息不存在"));

        // 检查消息是否有效
        LocalDateTime now = LocalDateTime.now();
        if (!message.getIsActive() || (message.getExpireAt() != null && message.getExpireAt().isBefore(now))) {
            throw new IllegalArgumentException("消息不存在");
        }

        // 检查是否已读
        boolean isRead = userReadMessageRepository.existsByUserIdAndMessageId(userId, messageId);

        return convertToDTO(message, isRead);
    }

    /**
     * 将实体转换为 DTO
     *
     * @param message 消息实体
     * @param isRead  是否已读
     * @return 消息 DTO
     */
    private ActivityMessageDTO convertToDTO(ActivityMessage message, boolean isRead) {
        return ActivityMessageDTO.builder()
                .id(message.getId())
                .title(message.getTitle())
                .content(message.getContent())
                .imageUrl(message.getImageUrl())
                .tag(message.getTag())
                .linkType(message.getLinkType())
                .linkUrl(message.getLinkUrl())
                .isRead(isRead)
                .createdAt(message.getCreatedAt() != null
                        ? message.getCreatedAt().atZone(ZoneId.of("Asia/Shanghai")).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                        : null)
                .build();
    }
}
