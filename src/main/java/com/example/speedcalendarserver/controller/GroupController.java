package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.CreateGroupRequest;
import com.example.speedcalendarserver.dto.GroupResponse;
import com.example.speedcalendarserver.dto.MyGroupResponse;
import com.example.speedcalendarserver.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups") // 修正：移除 "/api"，因为 context-path 中已包含
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request, @RequestHeader("Authorization") String token) {
        GroupResponse response = groupService.createGroup(request, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<GroupResponse> joinGroup(@PathVariable String groupId, @RequestHeader("Authorization") String token) {
        try {
            GroupResponse response = groupService.joinGroup(groupId, token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if ("Group not found".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            } else if ("User is already a member of this group".equals(e.getMessage())) {
                return ResponseEntity.status(409).build();
            }
            throw e;
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyGroupResponse>> getMyGroups(@RequestHeader("Authorization") String token) {
        List<MyGroupResponse> response = groupService.getMyGroups(token);
        return ResponseEntity.ok(response);
    }
}
