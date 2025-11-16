package com.example.speedcalendarserver.repository;

import com.example.speedcalendarserver.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户Token数据访问层
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Repository
public interface UserTokenRepository extends JpaRepository<UserToken, Long> {

    /**
     * 根据AccessToken查找Token记录
     *
     * @param accessToken 访问令牌
     * @return Token Optional
     */
    Optional<UserToken> findByAccessToken(String accessToken);

    /**
     * 根据RefreshToken查找Token记录
     *
     * @param refreshToken 刷新令牌
     * @return Token Optional
     */
    Optional<UserToken> findByRefreshToken(String refreshToken);

    /**
     * 查找用户所有有效Token
     *
     * @param userId 用户ID
     * @param status 状态（1-有效）
     * @return Token列表
     */
    List<UserToken> findByUserIdAndStatus(String userId, Integer status);

    // 注意：update和delete操作需要使用@Modifying和@Query注解
    // 以下方法暂时注释，需要时再添加@Query实现

    // /**
    //  * 失效用户所有Token（登出时使用）
    //  */
    // @Modifying
    // @Query("UPDATE UserToken t SET t.status = :newStatus WHERE t.userId = :userId AND t.status = :oldStatus")
    // int updateStatusByUserIdAndStatus(@Param("userId") String userId, @Param("oldStatus") Integer oldStatus, @Param("newStatus") Integer newStatus);

    /**
     * 删除过期的Token（定时任务使用）
     *
     * @param expiresAt 过期时间
     */
    void deleteByRefreshTokenExpiresAtBefore(LocalDateTime expiresAt);
}
