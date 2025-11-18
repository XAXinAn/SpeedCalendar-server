package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.UserPrivacySetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户隐私设置Repository
 * 性能优化：使用批量查询，避免N+1问题
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Repository
public interface UserPrivacySettingRepository extends JpaRepository<UserPrivacySetting, Long> {

    /**
     * 查询用户的所有隐私设置
     * 性能优化：一次查询获取所有设置，避免多次查询
     *
     * @param userId 用户ID
     * @return 隐私设置列表
     */
    List<UserPrivacySetting> findByUserId(String userId);

    /**
     * 查询用户的某个字段隐私设置
     *
     * @param userId 用户ID
     * @param fieldName 字段名
     * @return 隐私设置
     */
    Optional<UserPrivacySetting> findByUserIdAndFieldName(String userId, String fieldName);

    /**
     * 删除用户的所有隐私设置
     * 用于用户注销等场景
     *
     * @param userId 用户ID
     */
    @Modifying
    @Query("DELETE FROM UserPrivacySetting ups WHERE ups.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);

    /**
     * 批量查询多个用户的隐私设置
     * 性能优化：支持批量查询，减少数据库交互次数
     *
     * @param userIds 用户ID列表
     * @return 隐私设置列表
     */
    @Query("SELECT ups FROM UserPrivacySetting ups WHERE ups.userId IN :userIds")
    List<UserPrivacySetting> findByUserIdIn(@Param("userIds") List<String> userIds);
}
