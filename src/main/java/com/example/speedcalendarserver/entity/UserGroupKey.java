package com.example.speedcalendarserver.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * UserGroup 复合主键类
 *
 * @author SpeedCalendar Team
 * @since 2025-12-17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserGroupKey implements Serializable {
    private String userId;
    private String groupId;
}
