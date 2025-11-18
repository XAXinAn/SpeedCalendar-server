package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量更新隐私设置请求
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Data
public class UpdatePrivacySettingsRequest {

    /**
     * 隐私设置列表
     */
    @NotEmpty(message = "隐私设置列表不能为空")
    private List<PrivacySettingDTO> settings;
}
