package com.example.speedcalendarserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 群组成员DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDTO {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户头像
     */
    private String avatar;

    /**
     * 群内角色: owner, admin, member
     */
    private String groupRole;

    /**
     * 加入时间 (时间戳)
     */
    private Long joinedAt;
}
