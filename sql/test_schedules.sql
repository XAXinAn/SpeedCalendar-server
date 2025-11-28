-- =============================================
-- 日程功能测试数据脚本
-- 用途：快速创建各种极限条件的测试数据
-- =============================================

USE speed_calendar;

-- =============================================
-- 1. 清理测试数据
-- =============================================
-- 删除测试用户的所有日程
-- DELETE FROM schedules WHERE user_id = 'YOUR_USER_ID_HERE';

-- =============================================
-- 2. 基础边界测试数据
-- =============================================

-- 注意：将 'YOUR_USER_ID_HERE' 替换为你的实际用户ID

-- 2.1 月初日期
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅月初测试-1号', '2025-12-01', '09:00', '10:00', '测试地点', 0, 0);

-- 2.2 月末日期
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES
  (UUID(), 'YOUR_USER_ID_HERE', '✅月末测试-30号', '2025-11-30', '18:00', '19:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅月末测试-31号', '2025-12-31', '20:00', '21:00', NULL, 0, 0);

-- 2.3 闰年2月29日
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅闰年测试-2024/2/29', '2024-02-29', '12:00', '13:00', '闰年特别日', 0, 0);

-- 2.4 跨年日期
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES
  (UUID(), 'YOUR_USER_ID_HERE', '✅跨年夜-2025/12/31', '2025-12-31', '23:00', '23:59', '跨年派对', 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅新年-2026/1/1', '2026-01-01', '00:00', '01:00', '新年庆祝', 0, 0);

-- =============================================
-- 3. 时间边界测试
-- =============================================

-- 3.1 午夜时间 00:00
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅午夜测试-00:00', '2025-11-27', '00:00', '01:00', NULL, 0, 0);

-- 3.2 接近午夜 23:59
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅深夜测试-23:59', '2025-11-27', '23:59', NULL, NULL, 0, 0);

-- 3.3 全天日程
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅全天日程测试', '2025-11-28', NULL, NULL, '全天活动', 1, 0);

-- =============================================
-- 4. 字段边界测试
-- =============================================

-- 4.1 极短标题（1字符）
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', 'A', '2025-11-27', '10:00', NULL, NULL, 0, 0);

-- 4.2 长标题（100字符）
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE',
  '✅这是一个很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长很长的标题测试一二三四五六七八九十',
  '2025-11-27', '11:00', NULL, NULL, 0, 0);

-- 4.3 超长标题（200字符边界）
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE',
  '测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试测试',
  '2025-11-27', '12:00', NULL, NULL, 0, 0);

-- 4.4 特殊字符测试
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE',
  '特殊字符!@#$%^&*()_+-=[]{}|;:''",.<>?/~`',
  '2025-11-27', '13:00', NULL, '地址包含特殊字符：123-456号楼#2层', 0, 0);

-- 4.5 Emoji表情测试
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES
  (UUID(), 'YOUR_USER_ID_HERE', '🎉生日派对🎂', '2025-11-28', '14:00', '16:00', '🏠家里', 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '📅重要会议💼', '2025-11-28', '10:00', '11:00', '🏢办公室', 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '🏃‍♂️晨跑⏰', '2025-11-29', '06:00', '07:00', '🌳公园', 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '❤️💛💚💙💜', '2025-11-29', '15:00', NULL, NULL, 0, 0);

-- 4.6 中英文混合
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE',
  'Meeting会议 with客户 about关于 Project项目',
  '2025-11-27', '14:00', '15:00', 'Room会议室 301', 0, 0);

-- =============================================
-- 5. 单日多个日程测试
-- =============================================

-- 在同一天创建10个日程
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-1 早晨会议', '2025-11-30', '08:00', '09:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-2 晨会', '2025-11-30', '09:00', '09:30', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-3 部门例会', '2025-11-30', '10:00', '11:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-4 午餐', '2025-11-30', '12:00', '13:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-5 客户会议', '2025-11-30', '14:00', '15:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-6 项目评审', '2025-11-30', '15:00', '16:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-7 代码审查', '2025-11-30', '16:00', '17:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-8 技术分享', '2025-11-30', '17:00', '18:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-9 晚餐聚会', '2025-11-30', '19:00', '20:00', NULL, 0, 0),
  (UUID(), 'YOUR_USER_ID_HERE', '✅多日程-10 健身房', '2025-11-30', '20:00', '21:00', NULL, 0, 0);

-- =============================================
-- 6. 极远日期测试
-- =============================================

-- 未来很远的日期
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅未来测试-2099年', '2099-12-31', '23:59', NULL, '未来世界', 0, 0);

-- 过去很远的日期
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅历史测试-1900年', '1900-01-01', '00:00', NULL, '历史事件', 0, 0);

-- =============================================
-- 7. SQL注入安全测试（应该被安全处理）
-- =============================================

-- 这些恶意输入应该被当作普通文本处理
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE',
  '''; DROP TABLE schedules; --',
  '2025-11-27', '16:00', NULL, NULL, 0, 0);

-- =============================================
-- 8. 逻辑删除测试
-- =============================================

-- 创建一个已删除的日程
INSERT INTO schedules (schedule_id, user_id, title, schedule_date, start_time, end_time, location, is_all_day, is_deleted)
VALUES (UUID(), 'YOUR_USER_ID_HERE', '✅已删除日程测试', '2025-11-27', '17:00', NULL, NULL, 0, 1);

-- =============================================
-- 9. 验证查询
-- =============================================

-- 查看所有测试数据
-- SELECT schedule_id, title, schedule_date, start_time, is_all_day, is_deleted
-- FROM schedules
-- WHERE user_id = 'YOUR_USER_ID_HERE'
-- ORDER BY schedule_date, start_time;

-- 统计每天的日程数量
-- SELECT schedule_date, COUNT(*) as count
-- FROM schedules
-- WHERE user_id = 'YOUR_USER_ID_HERE' AND is_deleted = 0
-- GROUP BY schedule_date
-- ORDER BY schedule_date;

-- 查找包含emoji的日程
-- SELECT title FROM schedules
-- WHERE title LIKE '%🎉%' OR title LIKE '%💼%';

-- 查找超长标题的日程
-- SELECT title, LENGTH(title) as length
-- FROM schedules
-- WHERE LENGTH(title) > 50
-- ORDER BY length DESC;

-- =============================================
-- 10. 性能测试 - 批量创建数据
-- =============================================

-- 创建存储过程用于批量插入测试数据
DELIMITER $$
DROP PROCEDURE IF EXISTS create_bulk_test_schedules$$
CREATE PROCEDURE create_bulk_test_schedules(
    IN p_user_id VARCHAR(64),
    IN p_num_schedules INT
)
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE random_day INT;
    DECLARE random_hour INT;
    DECLARE random_minute INT;

    WHILE i < p_num_schedules DO
        SET random_day = FLOOR(1 + RAND() * 30);
        SET random_hour = FLOOR(RAND() * 24);
        SET random_minute = FLOOR(RAND() * 60);

        INSERT INTO schedules (
            schedule_id,
            user_id,
            title,
            schedule_date,
            start_time,
            end_time,
            location,
            is_all_day,
            is_deleted
        )
        VALUES (
            UUID(),
            p_user_id,
            CONCAT('批量测试日程-', i + 1),
            DATE_ADD('2025-12-01', INTERVAL random_day DAY),
            CONCAT(LPAD(random_hour, 2, '0'), ':', LPAD(random_minute, 2, '0')),
            NULL,
            CONCAT('地点-', FLOOR(RAND() * 10)),
            0,
            0
        );

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 使用方法：
-- CALL create_bulk_test_schedules('YOUR_USER_ID_HERE', 50);  -- 创建50个测试日程
-- CALL create_bulk_test_schedules('YOUR_USER_ID_HERE', 100); -- 创建100个测试日程

-- =============================================
-- 11. 清理测试数据的脚本
-- =============================================

-- 删除所有包含"测试"字样的日程
-- DELETE FROM schedules WHERE title LIKE '%测试%';

-- 删除所有带✅标记的日程
-- DELETE FROM schedules WHERE title LIKE '✅%';

-- 删除特定日期范围的日程
-- DELETE FROM schedules
-- WHERE user_id = 'YOUR_USER_ID_HERE'
-- AND schedule_date BETWEEN '2025-11-27' AND '2025-11-30';

-- =============================================
-- 使用说明
-- =============================================

/*
1. 获取你的用户ID：
   SELECT user_id FROM users WHERE phone = 'YOUR_PHONE_NUMBER';

2. 替换所有 'YOUR_USER_ID_HERE' 为你的实际用户ID

3. 执行所需的测试数据插入语句

4. 在前端App中验证：
   - 切换到对应的月份
   - 查看日程是否正确显示
   - 尝试编辑和删除

5. 测试完成后，运行清理脚本删除测试数据
*/
