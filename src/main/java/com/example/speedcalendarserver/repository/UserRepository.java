package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据手机号查找用户（未删除）
     *
     * @param phone 手机号
     * @return 用户Optional
     */
    Optional<User> findByPhoneAndIsDeleted(String phone, Integer isDeleted);

    /**
     * 根据邮箱查找用户（未删除）
     *
     * @param email 邮箱
     * @return 用户Optional
     */
    Optional<User> findByEmailAndIsDeleted(String email, Integer isDeleted);

    /**
     * 根据手机号查找用户（包含已删除）
     *
     * @param phone 手机号
     * @return 用户Optional
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据邮箱查找用户（包含已删除）
     *
     * @param email 邮箱
     * @return 用户Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查手机号是否存在（未删除）
     *
     * @param phone 手机号
     * @return true-存在，false-不存在
     */
    boolean existsByPhoneAndIsDeleted(String phone, Integer isDeleted);

    /**
     * 检查邮箱是否存在（未删除）
     *
     * @param email 邮箱
     * @return true-存在，false-不存在
     */
    boolean existsByEmailAndIsDeleted(String email, Integer isDeleted);
}
