package com.example.speedcalendarserver.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupDetailResponse {
    private String id;
    private String name;
    private String ownerId;
    private String currentUserRole; // 新增字段
    private String invitationCode;
    private List<GroupMemberDTO> members;
}
