package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 验证码数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * 查找最新的有效验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @param type  类型
     * @return 验证码Optional
     */
    Optional<VerificationCode> findFirstByPhoneAndCodeAndTypeAndStatusOrderByCreatedAtDesc(
            String phone, String code, String type, Integer status);

    /**
     * 查找手机号最近24小时内发送的验证码数量
     *
     * @param phone     手机号
     * @param startTime 开始时间（24小时前）
     * @return 验证码数量
     */
    long countByPhoneAndCreatedAtAfter(String phone, LocalDateTime startTime);

    /**
     * 查找手机号最近一条验证码
     *
     * @param phone 手机号
     * @return 验证码Optional
     */
    Optional<VerificationCode> findFirstByPhoneOrderByCreatedAtDesc(String phone);

    /**
     * 删除过期的验证码（定时任务使用）
     *
     * @param expiresAt 过期时间
     */
    void deleteByExpiresAtBefore(LocalDateTime expiresAt);

    /**
     * 查找所有过期但未标记的验证码
     *
     * @param now    当前时间
     * @param status 状态（未使用）
     * @return 验证码列表
     */
    List<VerificationCode> findByExpiresAtBeforeAndStatus(LocalDateTime now, Integer status);
}
