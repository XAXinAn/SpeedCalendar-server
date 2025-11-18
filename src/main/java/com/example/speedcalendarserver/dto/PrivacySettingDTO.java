package com.example.speedcalendarserver.dto;

import com.example.speedcalendarserver.enums.VisibilityLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 隐私设置DTO
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivacySettingDTO {

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 字段显示名称
     */
    private String displayName;

    /**
     * 可见性级别
     */
    private VisibilityLevel visibilityLevel;
}
