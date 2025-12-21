package com.example.speedcalendarserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 快速日程请求 DTO
 * 用于悬浮窗 OCR 截屏场景的快速日程添加
 *
 * @author SpeedCalendar Team
 * @since 2025-12-21
 */
@Data
public class QuickScheduleRequest {

    /**
     * OCR 识别出的原始文本
     */
    @NotBlank(message = "text 不能为空")
    private String text;
}
