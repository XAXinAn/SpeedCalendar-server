package com.example.speedcalendarserver.controller;

import com.example.speedcalendarserver.dto.*;
import com.example.speedcalendarserver.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request, @RequestHeader("Authorization") String token) {
        GroupResponse response = groupService.createGroup(request, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join-with-code")
    public ResponseEntity<GroupResponse> joinGroupWithCode(@RequestBody JoinGroupRequest request, @RequestHeader("Authorization") String token) {
        try {
            GroupResponse response = groupService.joinGroupWithCode(request, token);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if ("Group not found".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            } else if ("User is already a member of this group".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            throw e;
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyGroupResponse>> getMyGroups(@RequestHeader("Authorization") String token) {
        List<MyGroupResponse> response = groupService.getMyGroups(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetails(@PathVariable String groupId, @RequestHeader("Authorization") String token) {
        try {
            GroupDetailResponse response = groupService.getGroupDetails(groupId, token);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            if ("Group not found".equals(e.getMessage())) {
                return ResponseEntity.notFound().build();
            }
            throw e;
        }
    }
}
