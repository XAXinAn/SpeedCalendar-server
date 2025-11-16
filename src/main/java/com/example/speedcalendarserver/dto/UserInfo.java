package com.example.speedcalendarserver.dto;

import com.example.speedcalendarserver.entity.User;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户名/昵称
     */
    private String username;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 注册方式
     */
    private String loginType;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 从User实体转换
     */
    public static UserInfo fromEntity(User user) {
        return UserInfo.builder()
                .userId(user.getUserId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .birthday(user.getBirthday())
                .bio(user.getBio())
                .loginType(user.getLoginType())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
