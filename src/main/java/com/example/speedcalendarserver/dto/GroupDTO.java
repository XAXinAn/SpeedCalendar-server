package com.example.speedcalendarserver.dto;

import com.example.speedcalendarserver.entity.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;

/**
 * 群组信息DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDTO {

    /**
     * 群组ID
     */
    private String groupId;

    /**
     * 群组名称
     */
    private String name;

    /**
     * 群组简介
     */
    private String description;

    /**
     * 群主ID
     */
    private String ownerId;

    /**
     * 当前用户在该群的角色 (owner/admin/member)
     */
    private String currentUserRole;

    /**
     * 邀请码 (仅群主或管理员可见)
     */
    private String inviteCode;

    /**
     * 成员数量 (可选)
     */
    private Integer memberCount;

    /**
     * 创建时间 (时间戳)
     */
    private Long createdAt;

    /**
     * 加入时间 (时间戳) - 仅在"我加入的群组"列表中有值
     */
    private Long joinedAt;

    /**
     * 从实体转换
     */
    public static GroupDTO fromEntity(Group group) {
        return GroupDTO.builder()
                .groupId(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .inviteCode(group.getInvitationCode())
                .createdAt(group.getCreatedAt() != null ? 
                        group.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() : null)
                .build();
    }
}
