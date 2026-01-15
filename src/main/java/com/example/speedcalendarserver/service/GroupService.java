package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.*;
import com.example.speedcalendarserver.entity.Group;
import com.example.speedcalendarserver.entity.User;
import com.example.speedcalendarserver.entity.UserGroup;
import com.example.speedcalendarserver.repository.GroupRepository;
import com.example.speedcalendarserver.repository.UserGroupRepository;
import com.example.speedcalendarserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 群组服务
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserRepository userRepository;

    /**
     * 创建群组
     */
    @Transactional(rollbackFor = Exception.class)
    public GroupDTO createGroup(String userId, CreateGroupRequest request) {
        // 生成唯一邀请码
        String inviteCode;
        do {
            inviteCode = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        } while (groupRepository.findByInvitationCode(inviteCode).isPresent());

        // 创建群组
        Group group = Group.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .ownerId(userId)
                .invitationCode(inviteCode)
                .build();
        groupRepository.save(group);

        // 自动将创建者加入群组 (角色: owner)
        UserGroup userGroup = UserGroup.builder()
                .userId(userId)
                .groupId(group.getId())
                .role("owner")
                .build();
        userGroupRepository.save(userGroup);

        log.info("创建群组成功 - userId: {}, groupId: {}, inviteCode: {}", userId, group.getId(), inviteCode);

        // 构建返回对象
        GroupDTO dto = GroupDTO.fromEntity(group);
        dto.setMemberCount(1);
        dto.setCurrentUserRole("owner"); // 创建者即群主
        return dto;
    }

    /**
     * 加入群组 (通过邀请码)
     */
    @Transactional(rollbackFor = Exception.class)
    public void joinGroup(String userId, JoinGroupRequest request) {
        // 查找群组
        Group group = groupRepository.findByInvitationCode(request.getInviteCode())
                .orElseThrow(() -> new RuntimeException("邀请码无效或群组不存在"));

        // 检查是否已加入
        UserGroup existing = userGroupRepository.findByUserIdAndGroupId(userId, group.getId());
        if (existing != null) {
            throw new RuntimeException("您已是该群组成员");
        }

        // 加入群组 (角色: member)
        UserGroup userGroup = UserGroup.builder()
                .userId(userId)
                .groupId(group.getId())
                .role("member")
                .build();
        userGroupRepository.save(userGroup);

        log.info("加入群组成功 - userId: {}, groupId: {}", userId, group.getId());
    }

    /**
     * 获取我创建的群组 (按创建时间降序)
     */
    public List<GroupDTO> getCreatedGroups(String userId) {
        List<Group> groups = groupRepository.findByOwnerIdOrderByCreatedAtDesc(userId);
        return groups.stream()
                .map(group -> {
                    GroupDTO dto = GroupDTO.fromEntity(group);
                    dto.setMemberCount(userGroupRepository.countByGroupId(group.getId()));
                    dto.setCurrentUserRole("owner"); // 我创建的，角色必然是 owner
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取我加入的群组 (排除我创建的，按加入时间降序)
     */
    public List<GroupDTO> getJoinedGroups(String userId) {
        List<UserGroup> userGroups = userGroupRepository.findByUserIdOrderByJoinedAtDesc(userId);
        
        return userGroups.stream()
                .map(ug -> {
                    Group group = groupRepository.findById(ug.getGroupId()).orElse(null);
                    if (group == null || group.getOwnerId().equals(userId)) {
                        return null; // 排除不存在的或自己创建的
                    }
                    GroupDTO dto = GroupDTO.fromEntity(group);
                    dto.setMemberCount(userGroupRepository.countByGroupId(group.getId()));
                    dto.setJoinedAt(ug.getJoinedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
                    dto.setCurrentUserRole(ug.getRole());

                    // 邀请码可见性控制：仅 owner 或 admin 可见
                    if (!"owner".equals(ug.getRole()) && !"admin".equals(ug.getRole())) {
                        dto.setInviteCode(null);
                    }
                    return dto;
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 解散群组 (群主专用)
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteGroup(String userId, String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群组不存在"));

        if (!group.getOwnerId().equals(userId)) {
            throw new SecurityException("只有群主可以解散群组");
        }

        // 级联删除由数据库外键约束处理，或者手动删除
        // 这里依赖数据库外键级联删除 (ON DELETE CASCADE)
        groupRepository.delete(group);
        log.info("解散群组成功 - userId: {}, groupId: {}", userId, groupId);
    }

    /**
     * 退出群组 (成员专用)
     */
    @Transactional(rollbackFor = Exception.class)
    public void quitGroup(String userId, String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群组不存在"));

        if (group.getOwnerId().equals(userId)) {
            throw new RuntimeException("群主不能退出群组，请选择解散群组");
        }

        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId);
        if (userGroup == null) {
            throw new RuntimeException("您不是该群组成员");
        }

        userGroupRepository.delete(userGroup);
        log.info("退出群组成功 - userId: {}, groupId: {}", userId, groupId);
    }

    /**
     * 获取群组成员列表 (仅 owner/admin 可见)
     */
    public List<GroupMemberDTO> getGroupMembers(String userId, String groupId) {
        // 校验当前用户是否在群内
        UserGroup currentUserGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId);
        if (currentUserGroup == null) {
            throw new SecurityException("您不是该群组成员");
        }

        // 权限校验：仅 owner 或 admin 可查看成员列表
        String role = currentUserGroup.getRole();
        if (!"owner".equals(role) && !"admin".equals(role)) {
            throw new SecurityException("权限不足，仅群主或管理员可查看成员列表");
        }

        List<UserGroup> members = userGroupRepository.findByGroupId(groupId);
        
        return members.stream()
                .map(ug -> {
                    User user = userRepository.findById(ug.getUserId()).orElse(null);
                    if (user == null) return null;

                    return GroupMemberDTO.builder()
                            .userId(user.getUserId())
                            .username(user.getUsername())
                            .avatar(user.getAvatar())
                            .groupRole(ug.getRole())
                            .joinedAt(ug.getJoinedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                // 排序：群主 > 管理员 > 普通成员，同级按加入时间
                .sorted((m1, m2) -> {
                    int roleOrder1 = getRoleOrder(m1.getGroupRole());
                    int roleOrder2 = getRoleOrder(m2.getGroupRole());
                    if (roleOrder1 != roleOrder2) {
                        return roleOrder1 - roleOrder2;
                    }
                    return m1.getJoinedAt().compareTo(m2.getJoinedAt());
                })
                .collect(Collectors.toList());
    }

    /**
     * 设置/取消管理员 (群主专用)
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateMemberRole(String currentUserId, String groupId, String targetUserId, String newRole) {
        // 1. 校验群组是否存在
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("群组不存在"));

        // 2. 校验操作人权限：必须是群主
        if (!group.getOwnerId().equals(currentUserId)) {
            throw new SecurityException("只有群主可以设置管理员");
        }

        // 3. 校验目标用户是否在群内
        UserGroup targetUserGroup = userGroupRepository.findByUserIdAndGroupId(targetUserId, groupId);
        if (targetUserGroup == null) {
            throw new RuntimeException("目标用户不是该群组成员");
        }

        // 4. 校验目标用户是否为群主（群主不能修改自己的角色，也不能被修改）
        if (targetUserId.equals(group.getOwnerId())) {
            throw new RuntimeException("不能修改群主的角色");
        }

        // 5. 更新角色
        targetUserGroup.setRole(newRole);
        userGroupRepository.save(targetUserGroup);
        
        log.info("更新成员角色成功 - groupId: {}, targetUserId: {}, newRole: {}", groupId, targetUserId, newRole);
    }

    private int getRoleOrder(String role) {
        if ("owner".equals(role)) return 1;
        if ("admin".equals(role)) return 2;
        return 3;
    }
}
