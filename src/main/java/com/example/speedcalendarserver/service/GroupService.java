package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.*;
import com.example.speedcalendarserver.entity.Group;
import com.example.speedcalendarserver.entity.User;
import com.example.speedcalendarserver.entity.UserGroup;
import com.example.speedcalendarserver.repository.GroupRepository;
import com.example.speedcalendarserver.repository.UserGroupRepository;
import com.example.speedcalendarserver.repository.UserRepository;
import com.example.speedcalendarserver.util.JwtUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User getUserByToken(String token) {
        String userId = jwtUtil.getUserIdFromToken(token.substring(7));
        return userRepository.findByUserIdAndIsDeleted(userId, 0)
                .orElseThrow(() -> new RuntimeException("User not found or is deleted with id: " + userId));
    }

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, String token) {
        User user = getUserByToken(token);
        Group group = new Group();
        group.setId("grp_" + UUID.randomUUID().toString());
        group.setName(request.getName());
        group.setOwnerId(user.getUserId());
        group.setInvitationCode(RandomStringUtils.randomAlphanumeric(8));
        groupRepository.save(group);

        UserGroup userGroup = new UserGroup();
        userGroup.setUserId(user.getUserId());
        userGroup.setGroupId(group.getId());
        userGroup.setRole("admin");
        userGroupRepository.save(userGroup);

        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setOwnerId(group.getOwnerId());
        response.setInvitationCode(group.getInvitationCode());
        return response;
    }

    @Transactional
    public GroupResponse joinGroupWithCode(JoinGroupRequest request, String token) {
        User user = getUserByToken(token);
        Group group = groupRepository.findByInvitationCode(request.getInvitationCode())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        UserGroup existingUserGroup = userGroupRepository.findByUserIdAndGroupId(user.getUserId(), group.getId());
        if (existingUserGroup != null) {
            throw new RuntimeException("User is already a member of this group");
        }

        UserGroup userGroup = new UserGroup();
        userGroup.setUserId(user.getUserId());
        userGroup.setGroupId(group.getId());
        userGroup.setRole("member");
        userGroupRepository.save(userGroup);

        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setOwnerId(group.getOwnerId());
        response.setInvitationCode(group.getInvitationCode());
        return response;
    }

    public List<MyGroupResponse> getMyGroups(String token) {
        User user = getUserByToken(token);
        List<UserGroup> userGroups = userGroupRepository.findByUserId(user.getUserId());
        return userGroups.stream().map(userGroup -> {
            Group group = groupRepository.findById(userGroup.getGroupId()).orElse(null);
            if (group == null) {
                return null;
            }
            MyGroupResponse response = new MyGroupResponse();
            response.setGroupId(group.getId());
            response.setGroupName(group.getName());
            response.setRole(userGroup.getRole());
            return response;
        }).filter(response -> response != null).collect(Collectors.toList());
    }

    public GroupDetailResponse getGroupDetails(String groupId, String token) {
        User user = getUserByToken(token);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        UserGroup currentUserGroup = userGroupRepository.findByUserIdAndGroupId(user.getUserId(), groupId);

        if (currentUserGroup == null) {
            throw new SecurityException("User is not a member of this group");
        }

        GroupDetailResponse response = new GroupDetailResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setOwnerId(group.getOwnerId());
        response.setCurrentUserRole(currentUserGroup.getRole()); // 设置当前用户的角色

        if ("admin".equals(currentUserGroup.getRole())) {
            response.setInvitationCode(group.getInvitationCode());
        }

        List<UserGroup> allUserGroups = userGroupRepository.findByGroupId(groupId);
        List<GroupMemberDTO> members = allUserGroups.stream().map(ug -> {
            User memberUser = userRepository.findById(ug.getUserId()).orElse(null);
            if (memberUser == null) {
                return null;
            }
            GroupMemberDTO memberDTO = new GroupMemberDTO();
            memberDTO.setUserId(memberUser.getUserId());
            memberDTO.setUsername(memberUser.getUsername());
            memberDTO.setRole(ug.getRole());
            return memberDTO;
        }).filter(dto -> dto != null).collect(Collectors.toList());

        response.setMembers(members);
        return response;
    }
}
