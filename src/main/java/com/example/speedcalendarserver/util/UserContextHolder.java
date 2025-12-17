package com.example.speedcalendarserver.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户上下文持有者
 * 使用 ThreadLocal 和 sessionId 映射双重机制存储用户ID
 *
 * <p>
 * 由于流式 AI 调用在不同线程中执行工具，ThreadLocal 可能失效。
 * 因此增加了基于 sessionId 的映射作为备用方案。
 *
 * @author SpeedCalendar Team
 * @since 2025-11-26
 */
public class UserContextHolder {

    private static final ThreadLocal<String> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 当前线程关联的 sessionId，用于从 SESSION_USER_MAP 回溯 userId
     */
    private static final ThreadLocal<String> SESSION_ID_HOLDER = new ThreadLocal<>();

    /**
     * sessionId -> userId 的映射，用于流式调用时跨线程获取用户ID
     */
    private static final ConcurrentHashMap<String, String> SESSION_USER_MAP = new ConcurrentHashMap<>();

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
     * 设置当前线程的会话ID（用于工具调用时回溯用户ID）
     */
    public static void setSessionId(String sessionId) {
        SESSION_ID_HOLDER.set(sessionId);
    }

    /**
     * 获取当前线程的会话ID
     */
    public static String getSessionId() {
        return SESSION_ID_HOLDER.get();
    }

    /**
     * 先取 ThreadLocal 的 userId，若为空则根据 sessionId 从映射中回溯
     */
    public static String resolveUserId() {
        String userId = USER_ID_HOLDER.get();
        if (userId != null) {
            return userId;
        }

        String sessionId = SESSION_ID_HOLDER.get();
        if (sessionId != null) {
            return SESSION_USER_MAP.get(sessionId);
        }

        // 兜底：当线程上下文缺失且仅存在一个活跃映射时，返回唯一的 userId
        if (SESSION_USER_MAP.size() == 1) {
            return SESSION_USER_MAP.values().stream().findFirst().orElse(null);
        }

        return null;
    }

    /**
     * 绑定 sessionId 和 userId 的关系（用于流式调用）
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     */
    public static void bindSession(String sessionId, String userId) {
        if (sessionId != null && userId != null) {
            SESSION_USER_MAP.put(sessionId, userId);
        }
    }

    /**
     * 根据 sessionId 获取 userId（用于流式调用时跨线程获取）
     *
     * @param sessionId 会话ID
     * @return 用户ID，如果未找到则返回 null
     */
    public static String getUserIdBySession(String sessionId) {
        return sessionId != null ? SESSION_USER_MAP.get(sessionId) : null;
    }

    /**
     * 解绑 sessionId（流式调用完成后调用）
     *
     * @param sessionId 会话ID
     */
    public static void unbindSession(String sessionId) {
        if (sessionId != null) {
            SESSION_USER_MAP.remove(sessionId);
        }
    }

    /**
     * 清除当前线程的用户ID和会话ID
     * 
     * <p>
     * 重要：必须在请求处理完成后调用此方法，避免内存泄漏和线程复用导致的数据污染
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        SESSION_ID_HOLDER.remove();
    }
}
