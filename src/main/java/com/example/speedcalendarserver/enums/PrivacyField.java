package com.example.speedcalendarserver.enums;

/**
 * 可配置隐私的字段
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
public enum PrivacyField {

    /**
     * 手机号
     */
    PHONE("phone", "手机号", VisibilityLevel.PRIVATE),

    /**
     * 邮箱
     */
    EMAIL("email", "邮箱", VisibilityLevel.FRIENDS_ONLY),

    /**
     * 生日
     */
    BIRTHDAY("birthday", "生日", VisibilityLevel.FRIENDS_ONLY),

    /**
     * 性别
     */
    GENDER("gender", "性别", VisibilityLevel.PUBLIC),

    /**
     * 个人简介
     */
    BIO("bio", "个人简介", VisibilityLevel.PUBLIC);

    private final String fieldName;
    private final String displayName;
    private final VisibilityLevel defaultLevel;

    PrivacyField(String fieldName, String displayName, VisibilityLevel defaultLevel) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.defaultLevel = defaultLevel;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public VisibilityLevel getDefaultLevel() {
        return defaultLevel;
    }

    /**
     * 根据字段名获取枚举
     *
     * @param fieldName 字段名
     * @return 枚举，不存在则返回null
     */
    public static PrivacyField fromFieldName(String fieldName) {
        if (fieldName == null) {
            return null;
        }

        for (PrivacyField field : values()) {
            if (field.fieldName.equals(fieldName)) {
                return field;
            }
        }

        return null;
    }
}
