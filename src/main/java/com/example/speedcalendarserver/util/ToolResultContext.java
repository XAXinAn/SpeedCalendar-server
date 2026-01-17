package com.example.speedcalendarserver.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * 工具结果上下文
 * 用于在 AI 工具调用之间传递状态信息
 *
 * @author SpeedCalendar Team
 * @since 2026-01-17
 */
public class ToolResultContext {

    /**
     * 上下文键：最近一次动作类型
     * 值: create | delete | query
     */
    public static final String LAST_ACTION_TYPE = "LAST_ACTION_TYPE";

    /**
     * 上下文键：最近一次动作消息
     */
    public static final String LAST_ACTION_MESSAGE = "LAST_ACTION_MESSAGE";

    /**
     * 上下文键：待确认删除的关键词
     * 当删除操作匹配多个日程时，存储关键词供二次确认使用
     */
    public static final String PENDING_DELETE_KEYWORD = "PENDING_DELETE_KEYWORD";

    /**
     * 上下文键：最近创建的日程ID
     */
    public static final String LAST_CREATED_SCHEDULE_ID = "LAST_CREATED_SCHEDULE_ID";

    /**
     * 上下文键：最近创建的日程日期
     */
    public static final String LAST_CREATED_SCHEDULE_DATE = "LAST_CREATED_SCHEDULE_DATE";

    /**
     * 线程本地上下文存储
     * 使用 ConcurrentHashMap 保证线程安全
     */
    private static final ThreadLocal<Map<String, Object>> CONTEXT = ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * 设置上下文值
     *
     * @param key   上下文键
     * @param value 上下文值
     */
    public static void set(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    /**
     * 获取上下文值
     *
     * @param key 上下文键
     * @return 上下文值，不存在时返回 null
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        return (T) CONTEXT.get().get(key);
    }

    /**
     * 获取上下文值（带默认值）
     *
     * @param key          上下文键
     * @param defaultValue 默认值
     * @return 上下文值，不存在时返回默认值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getOrDefault(String key, T defaultValue) {
        Object value = CONTEXT.get().get(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 移除指定上下文
     *
     * @param key 上下文键
     */
    public static void remove(String key) {
        CONTEXT.get().remove(key);
    }

    /**
     * 清理当前线程的所有上下文
     */
    public static void clear() {
        CONTEXT.get().clear();
        CONTEXT.remove();
    }

    /**
     * 记录创建动作
     *
     * @param message    动作消息
     * @param scheduleId 创建的日程ID
     * @param date       日程日期
     */
    public static void recordCreateAction(String message, String scheduleId, String date) {
        set(LAST_ACTION_TYPE, "create");
        set(LAST_ACTION_MESSAGE, message);
        set(LAST_CREATED_SCHEDULE_ID, scheduleId);
        set(LAST_CREATED_SCHEDULE_DATE, date);
        remove(PENDING_DELETE_KEYWORD);
    }

    /**
     * 记录删除动作
     *
     * @param message 动作消息
     */
    public static void recordDeleteAction(String message) {
        set(LAST_ACTION_TYPE, "delete");
        set(LAST_ACTION_MESSAGE, message);
        remove(PENDING_DELETE_KEYWORD);
    }

    /**
     * 记录待确认删除状态
     *
     * @param keyword 待确认删除的关键词
     * @param message 提示消息
     */
    public static void recordPendingDelete(String keyword, String message) {
        set(LAST_ACTION_TYPE, "delete_pending");
        set(LAST_ACTION_MESSAGE, message);
        set(PENDING_DELETE_KEYWORD, keyword);
    }

    /**
     * 获取最近动作类型
     *
     * @return 动作类型：create | delete | delete_pending | null
     */
    public static String getLastActionType() {
        return get(LAST_ACTION_TYPE);
    }

    /**
     * 获取最近动作消息
     *
     * @return 动作消息
     */
    public static String getLastActionMessage() {
        return get(LAST_ACTION_MESSAGE);
    }

    /**
     * 获取待确认删除的关键词
     *
     * @return 关键词，不存在时返回 null
     */
    public static String getPendingDeleteKeyword() {
        return get(PENDING_DELETE_KEYWORD);
    }
}
