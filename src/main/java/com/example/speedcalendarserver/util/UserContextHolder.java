package com.example.speedcalendarserver.util;

/**
 * 用户上下文持有者
 * 使用 ThreadLocal 存储当前请求的用户ID，用于在 AI Tool 调用时获取用户信息
 *
 * <p>
 * 注意：此类基于 ThreadLocal 实现，仅在同一线程内有效。
 *
 * <p>
 * TODO: 异步任务（如 @Async 生成会话标题）不能使用此类获取用户ID，
 * 异步方法需要显式传入 userId 参数。
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
public class UserContextHolder {

    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前线程的用户ID
     *
     * @return 用户ID，如果未设置则返回 null
     */
    public static String getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除当前线程的用户ID
     * 
     * <p>
     * 重要：必须在请求处理完成后调用此方法，避免内存泄漏和线程复用导致的数据污染
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}
