package com.example.speedcalendarserver.enums;

/**
 * 隐私可见性级别
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
public enum VisibilityLevel {

    /**
     * 公开 - 所有人可见
     */
    PUBLIC("公开"),

    /**
     * 仅好友可见 - 预留给未来好友系统
     * 当前阶段等同于 PRIVATE
     */
    FRIENDS_ONLY("仅好友可见"),

    /**
     * 私密 - 仅自己可见
     */
    PRIVATE("私密");

    private final String description;

    VisibilityLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断当前可见性级别是否允许访问
     *
     * @param isSelf 是否是自己
     * @param isFriend 是否是好友（预留）
     * @return 是否可见
     */
    public boolean isVisible(boolean isSelf, boolean isFriend) {
        if (isSelf) {
            return true; // 自己总是可见
        }

        switch (this) {
            case PUBLIC:
                return true; // 公开对所有人可见
            case FRIENDS_ONLY:
                return isFriend; // 仅好友可见（目前返回false）
            case PRIVATE:
                return false; // 私密只有自己可见
            default:
                return false;
        }
    }
}
