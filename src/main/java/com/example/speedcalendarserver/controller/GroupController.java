package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.*;
import com.example.speedcalendarserver.service.GroupService;
import com.example.speedcalendarserver.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群组控制器
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Slf4j
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final JwtUtil jwtUtil;

    /**
     * 创建群组
     */
    @PostMapping
    public ApiResponse<GroupDTO> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            log.info("【创建群组】userId: {}, name: {}", userId, request.getName());
            GroupDTO group = groupService.createGroup(userId, request);
            return ApiResponse.success("创建成功", group);
        } catch (Exception e) {
            log.error("【创建群组失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 加入群组
     */
    @PostMapping("/join")
    public ApiResponse<Void> joinGroup(
            @Valid @RequestBody JoinGroupRequest request,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            log.info("【加入群组】userId: {}, code: {}", userId, request.getInviteCode());
            groupService.joinGroup(userId, request);
            return ApiResponse.success("加入成功", null);
        } catch (RuntimeException e) {
            log.warn("【加入群组失败】{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("【加入群组失败】{}", e.getMessage(), e);
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统繁忙");
        }
    }

    /**
     * 获取我创建的群组
     */
    @GetMapping("/created")
    public ApiResponse<List<GroupDTO>> getCreatedGroups(HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            List<GroupDTO> groups = groupService.getCreatedGroups(userId);
            return ApiResponse.success("获取成功", groups);
        } catch (Exception e) {
            log.error("【获取创建群组失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取我加入的群组
     */
    @GetMapping("/joined")
    public ApiResponse<List<GroupDTO>> getJoinedGroups(HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            List<GroupDTO> groups = groupService.getJoinedGroups(userId);
            return ApiResponse.success("获取成功", groups);
        } catch (Exception e) {
            log.error("【获取加入群组失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 解散群组 (群主专用)
     */
    @DeleteMapping("/{groupId}")
    public ApiResponse<Void> deleteGroup(
            @PathVariable String groupId,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            log.info("【解散群组】userId: {}, groupId: {}", userId, groupId);
            groupService.deleteGroup(userId, groupId);
            return ApiResponse.success("解散成功", null);
        } catch (SecurityException e) {
            return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【解散群组失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 退出群组 (管理员及成员可用)
     */
    @PostMapping("/{groupId}/quit")
    public ApiResponse<Void> quitGroup(
            @PathVariable String groupId,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            log.info("【退出群组】userId: {}, groupId: {}", userId, groupId);
            groupService.quitGroup(userId, groupId);
            return ApiResponse.success("退出成功", null);
        } catch (Exception e) {
            log.error("【退出群组失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 获取群组成员列表
     */
    @GetMapping("/{groupId}/members")
    public ApiResponse<List<GroupMemberDTO>> getGroupMembers(
            @PathVariable String groupId,
            HttpServletRequest httpRequest) {
        try {
            String userId = getUserIdFromRequest(httpRequest);
            if (userId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            List<GroupMemberDTO> members = groupService.getGroupMembers(userId, groupId);
            return ApiResponse.success("获取成功", members);
        } catch (SecurityException e) {
            return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【获取群组成员失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 设置/解除管理员 (群主特权)
     * URL: PUT /api/groups/{groupId}/members/{userId}/role
     */
    @PutMapping("/{groupId}/members/{targetUserId}/role")
    public ApiResponse<Void> updateMemberRole(
            @PathVariable String groupId,
            @PathVariable String targetUserId,
            @Valid @RequestBody UpdateGroupRoleRequest request,
            HttpServletRequest httpRequest) {
        try {
            String currentUserId = getUserIdFromRequest(httpRequest);
            if (currentUserId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }
            
            log.info("【角色变更】operator: {}, target: {}, role: {}", currentUserId, targetUserId, request.getRole());
            groupService.updateMemberRole(currentUserId, groupId, targetUserId, request.getRole());
            
            return ApiResponse.success("操作成功", null);
        } catch (SecurityException e) {
            return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【角色变更失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 批量移除群成员 (群主/管理员特权)
     * URL: DELETE /api/groups/{groupId}/members
     */
    @DeleteMapping("/{groupId}/members")
    public ApiResponse<Void> removeMembers(
            @PathVariable String groupId,
            @Valid @RequestBody RemoveMembersRequest request,
            HttpServletRequest httpRequest) {
        try {
            String currentUserId = getUserIdFromRequest(httpRequest);
            if (currentUserId == null) {
                return ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), "未授权，请先登录");
            }

            log.info("【批量移除成员】operator: {}, groupId: {}, userIds: {}", 
                    currentUserId, groupId, request.getUserIds());
            
            groupService.removeMembers(currentUserId, groupId, request.getUserIds());
            
            return ApiResponse.success("移除成功", null);
        } catch (SecurityException e) {
            return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
        } catch (Exception e) {
            log.error("【批量移除成员失败】{}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }

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
}
