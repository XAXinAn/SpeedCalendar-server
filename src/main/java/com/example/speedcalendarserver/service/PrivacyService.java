package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.PrivacySettingDTO;
import com.example.speedcalendarserver.dto.UserInfo;
import com.example.speedcalendarserver.entity.UserPrivacySetting;
import com.example.speedcalendarserver.enums.PrivacyField;
import com.example.speedcalendarserver.enums.VisibilityLevel;
import com.example.speedcalendarserver.repository.UserPrivacySettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 隐私设置服务
 * 性能优化：使用内存缓存，减少数据库查询
 *
 * @author SpeedCalendar Team
 * @since 2025-11-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrivacyService {

    private final UserPrivacySettingRepository privacySettingRepository;

    /**
     * 缓存：用户隐私设置
     * Key: userId, Value: Map<fieldName, VisibilityLevel>
     * 性能优化：内存缓存，避免频繁查询数据库
     */
    private final Map<String, Map<String, VisibilityLevel>> privacyCache = new ConcurrentHashMap<>();

    /**
     * 获取用户的所有隐私设置
     * 如果数据库中没有记录，返回默认设置
     *
     * @param userId 用户ID
     * @return 隐私设置列表
     */
    public List<PrivacySettingDTO> getUserPrivacySettings(String userId) {
        Map<String, VisibilityLevel> settingsMap = getPrivacySettingsMap(userId);

        // 构造返回DTO
        return Arrays.stream(PrivacyField.values())
                .map(field -> new PrivacySettingDTO(
                        field.getFieldName(),
                        field.getDisplayName(),
                        settingsMap.getOrDefault(field.getFieldName(), field.getDefaultLevel())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 批量更新用户的隐私设置
     *
     * @param userId 用户ID
     * @param settings 隐私设置列表
     */
    @Transactional
    public void updatePrivacySettings(String userId, List<PrivacySettingDTO> settings) {
        for (PrivacySettingDTO setting : settings) {
            // 验证字段名是否合法
            PrivacyField field = PrivacyField.fromFieldName(setting.getFieldName());
            if (field == null) {
                log.warn("【隐私设置】无效的字段名: {}", setting.getFieldName());
                continue;
            }

            // 查询是否已存在
            Optional<UserPrivacySetting> existingOpt = privacySettingRepository
                    .findByUserIdAndFieldName(userId, setting.getFieldName());

            if (existingOpt.isPresent()) {
                // 更新现有记录
                UserPrivacySetting existing = existingOpt.get();
                existing.setVisibilityLevel(setting.getVisibilityLevel());
                privacySettingRepository.save(existing);
            } else {
                // 创建新记录
                UserPrivacySetting newSetting = new UserPrivacySetting();
                newSetting.setUserId(userId);
                newSetting.setFieldName(setting.getFieldName());
                newSetting.setVisibilityLevel(setting.getVisibilityLevel());
                privacySettingRepository.save(newSetting);
            }
        }

        // 清除缓存
        privacyCache.remove(userId);

        log.info("【隐私设置】用户 {} 更新隐私设置成功", userId);
    }

    /**
     * 根据隐私设置过滤用户信息
     * 性能优化：使用缓存的隐私设置，避免查询数据库
     *
     * @param userInfo 完整的用户信息
     * @param requesterId 请求者ID（查看者）
     * @return 过滤后的用户信息
     */
    public UserInfo filterUserInfo(UserInfo userInfo, String requesterId) {
        if (userInfo == null) {
            return null;
        }

        String targetUserId = userInfo.getUserId();

        // 如果是自己查看自己，返回完整信息
        if (targetUserId.equals(requesterId)) {
            return userInfo;
        }

        // 获取目标用户的隐私设置
        Map<String, VisibilityLevel> settingsMap = getPrivacySettingsMap(targetUserId);

        // 判断关系（目前没有好友系统，都是陌生人）
        boolean isFriend = false; // 预留给未来好友系统

        // 创建过滤后的UserInfo
        UserInfo filteredInfo = new UserInfo();
        filteredInfo.setUserId(userInfo.getUserId());

        // 始终可见的字段
        filteredInfo.setUsername(userInfo.getUsername());
        filteredInfo.setAvatar(userInfo.getAvatar());
        filteredInfo.setLoginType(userInfo.getLoginType());
        filteredInfo.setCreatedAt(userInfo.getCreatedAt());

        // 根据隐私设置过滤字段
        filteredInfo.setPhone(filterField(userInfo.getPhone(), PrivacyField.PHONE, settingsMap, false, isFriend));
        filteredInfo.setEmail(filterField(userInfo.getEmail(), PrivacyField.EMAIL, settingsMap, false, isFriend));
        filteredInfo.setBirthday(filterLocalDateField(userInfo.getBirthday(), PrivacyField.BIRTHDAY, settingsMap, false, isFriend));
        filteredInfo.setGender(filterIntField(userInfo.getGender(), PrivacyField.GENDER, settingsMap, false, isFriend));
        filteredInfo.setBio(filterField(userInfo.getBio(), PrivacyField.BIO, settingsMap, false, isFriend));

        return filteredInfo;
    }

    /**
     * 获取用户隐私设置Map（带缓存）
     * 性能优化：优先从缓存读取，缓存未命中才查询数据库
     *
     * @param userId 用户ID
     * @return Map<fieldName, VisibilityLevel>
     */
    private Map<String, VisibilityLevel> getPrivacySettingsMap(String userId) {
        // 先从缓存获取
        Map<String, VisibilityLevel> cached = privacyCache.get(userId);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<UserPrivacySetting> settings = privacySettingRepository.findByUserId(userId);

        Map<String, VisibilityLevel> settingsMap = settings.stream()
                .collect(Collectors.toMap(
                        UserPrivacySetting::getFieldName,
                        UserPrivacySetting::getVisibilityLevel
                ));

        // 放入缓存
        privacyCache.put(userId, settingsMap);

        return settingsMap;
    }

    /**
     * 根据隐私设置过滤字段值
     *
     * @param value 字段值
     * @param field 隐私字段枚举
     * @param settingsMap 隐私设置Map
     * @param isSelf 是否是自己
     * @param isFriend 是否是好友
     * @return 过滤后的值，不可见则返回null
     */
    private String filterField(String value, PrivacyField field,
                                Map<String, VisibilityLevel> settingsMap,
                                boolean isSelf, boolean isFriend) {
        if (value == null) {
            return null;
        }

        VisibilityLevel level = settingsMap.getOrDefault(field.getFieldName(), field.getDefaultLevel());

        return level.isVisible(isSelf, isFriend) ? value : null;
    }

    /**
     * 根据隐私设置过滤Integer字段值
     */
    private Integer filterIntField(Integer value, PrivacyField field,
                                    Map<String, VisibilityLevel> settingsMap,
                                    boolean isSelf, boolean isFriend) {
        if (value == null) {
            return null;
        }

        VisibilityLevel level = settingsMap.getOrDefault(field.getFieldName(), field.getDefaultLevel());

        return level.isVisible(isSelf, isFriend) ? value : null;
    }

    /**
     * 根据隐私设置过滤LocalDate字段值
     */
    private java.time.LocalDate filterLocalDateField(java.time.LocalDate value, PrivacyField field,
                                                       Map<String, VisibilityLevel> settingsMap,
                                                       boolean isSelf, boolean isFriend) {
        if (value == null) {
            return null;
        }

        VisibilityLevel level = settingsMap.getOrDefault(field.getFieldName(), field.getDefaultLevel());

        return level.isVisible(isSelf, isFriend) ? value : null;
    }

    /**
     * 清除用户的隐私设置缓存
     * 用于用户更新隐私设置后
     *
     * @param userId 用户ID
     */
    public void clearCache(String userId) {
        privacyCache.remove(userId);
    }

    /**
     * 清除所有缓存
     * 用于系统维护等场景
     */
    public void clearAllCache() {
        privacyCache.clear();
        log.info("【隐私设置】已清除所有隐私设置缓存");
    }
}
