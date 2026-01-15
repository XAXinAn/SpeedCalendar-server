package com.example.speedcalendarserver.service;

import com.example.speedcalendarserver.dto.LoginResponse;
import com.example.speedcalendarserver.dto.PasswordLoginRequest;
import com.example.speedcalendarserver.dto.PhoneLoginRequest;
import com.example.speedcalendarserver.dto.RegisterRequest;
import com.example.speedcalendarserver.dto.SendCodeRequest;
import com.example.speedcalendarserver.dto.UpdateProfileRequest;
import com.example.speedcalendarserver.dto.UpdateUserInfoRequest;
import com.example.speedcalendarserver.dto.UserInfo;
import com.example.speedcalendarserver.entity.User;
import com.example.speedcalendarserver.entity.UserToken;
import com.example.speedcalendarserver.entity.VerificationCode;
import com.example.speedcalendarserver.repository.UserRepository;
import com.example.speedcalendarserver.repository.UserTokenRepository;
import com.example.speedcalendarserver.repository.VerificationCodeRepository;
import com.example.speedcalendarserver.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * 认证服务
 * 处理用户登录、注册、验证码等业务逻辑
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtUtil jwtUtil;
    private final PrivacyService privacyService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 验证码长度
     */
    @Value("${verification.code.length}")
    private Integer codeLength;

    /**
     * 验证码有效期（秒）
     */
    @Value("${verification.code.expiration}")
    private Integer codeExpiration;

    /**
     * 每日发送次数限制
     */
    @Value("${verification.code.daily-limit}")
    private Integer dailyLimit;

    /**
     * 发送间隔（秒）
     */
    @Value("${verification.code.interval}")
    private Integer sendInterval;

    /**
     * 本地调试模式开关
     */
    @Value("${verification.code.debug-mode:true}")
    private Boolean debugMode;

    /**
     * 发送验证码
     *
     * @param request   发送验证码请求
     * @param ipAddress 客户端IP地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void sendVerificationCode(SendCodeRequest request, String ipAddress) {
        String phone = request.getPhone();

        // 1/2. 原频率与每日次数限制临时关闭（调试用）
        // checkSendFrequency(phone);
        // checkDailyLimit(phone);

        // 3. 生成验证码
        String code = generateCode();

        // 4. 标记该手机号之前所有未使用的验证码为过期
        expireOldCodes(phone);

        // 5. 保存新验证码
        VerificationCode verificationCode = VerificationCode.builder()
                .phone(phone)
                .code(code)
                .type("login")
                .status(0) // 未使用
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusSeconds(codeExpiration))
                .build();
        verificationCodeRepository.save(verificationCode);

        // 6. 发送验证码（调试模式打印到控制台，生产模式发送短信）
        if (debugMode) {
            // 本地调试模式：控制台打印验证码
            log.warn("\n" +
                    "========================================\n" +
                    "【本地调试 - 验证码】\n" +
                    "手机号: {}\n" +
                    "验证码: {}\n" +
                    "有效期: {}秒\n" +
                    "========================================",
                    phone, code, codeExpiration);
            System.out.println("\n========================================");
            System.out.println("【本地调试 - 验证码】");
            System.out.println("手机号: " + phone);
            System.out.println("验证码: " + code);
            System.out.println("有效期: " + codeExpiration + "秒");
            System.out.println("========================================\n");
        } else {
            // 生产环境：实际发送短信
            log.info("【发送短信】手机号: {}, 验证码已通过短信服务发送", phone);
            // TODO: 集成真实的短信服务（阿里云、腾讯云等）
            // smsService.sendCode(phone, code);
        }
    }

    /**
     * 手机号登录
     * 如果用户不存在则自动注册
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse phoneLogin(PhoneLoginRequest request, HttpServletRequest httpRequest) {
        String phone = request.getPhone();
        String code = request.getCode();

        // 1. 验证验证码
        validateVerificationCode(phone, code);

        // 2. 查找或创建用户
        User user = userRepository.findByPhoneAndIsDeleted(phone, 0)
                .orElseGet(() -> createNewUser(phone));

        // 3. 更新登录信息
        updateUserLoginInfo(user, httpRequest);

        // 4. 生成Token
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 5. 保存Token记录
        saveUserToken(user.getUserId(), accessToken, refreshToken, httpRequest);

        // 6. 构建响应
        return buildLoginResponse(user, accessToken, refreshToken);
    }

    /**
     * 手机号密码注册（注册后自动登录）
     *
     * @param request     注册请求
     * @param httpRequest HTTP请求
     * @return 登录响应（包含token和用户信息）
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse register(RegisterRequest request, HttpServletRequest httpRequest) {
        String phone = request.getPhone();

        // 检查手机号是否已注册
        if (userRepository.findByPhoneAndIsDeleted(phone, 0).isPresent()) {
            throw new RuntimeException("该手机号已注册");
        }

        // 创建用户
        User user = User.builder()
                .userId(UUID.randomUUID().toString())
                .phone(phone)
                .password(passwordEncoder.encode(request.getPassword()))
                .username("用户" + phone.substring(phone.length() - 4))
                .avatar("https://api.dicebear.com/7.x/initials/svg?seed=" + phone.substring(phone.length() - 4))
                .loginType("phone")
                .status(1)
                .isDeleted(0)
                .build();

        userRepository.save(user);
        log.info("【注册成功】手机号: {}, userId: {}", phone, user.getUserId());

        // 注册成功后自动登录：更新登录信息
        updateUserLoginInfo(user, httpRequest);

        // 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 保存 Token
        saveUserToken(user.getUserId(), accessToken, refreshToken, httpRequest);

        log.info("【注册并登录成功】手机号: {}, userId: {}", phone, user.getUserId());

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    /**
     * 手机号密码登录
     *
     * @param request     登录请求
     * @param httpRequest HTTP请求
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse passwordLogin(PasswordLoginRequest request, HttpServletRequest httpRequest) {
        String phone = request.getPhone();

        // 查找用户
        User user = userRepository.findByPhoneAndIsDeleted(phone, 0)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密码
        if (user.getPassword() == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 更新登录信息
        updateUserLoginInfo(user, httpRequest);

        // 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getUserId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // 保存 Token
        saveUserToken(user.getUserId(), accessToken, refreshToken, httpRequest);

        log.info("【密码登录成功】手机号: {}, userId: {}", phone, user.getUserId());

        return buildLoginResponse(user, accessToken, refreshToken);
    }

    /**
     * 刷新Token
     * 使用refreshToken换取新的accessToken和refreshToken
     *
     * @param refreshToken 刷新令牌
     * @param httpRequest  HTTP请求
     * @return 刷新Token响应
     */
    @Transactional(rollbackFor = Exception.class)
    public com.example.speedcalendarserver.dto.RefreshTokenResponse refreshToken(String refreshToken,
            HttpServletRequest httpRequest) {
        // 1. 验证refreshToken格式和签名
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("refreshToken无效或已过期");
        }

        // 2. 从refreshToken中获取用户ID
        String userId = jwtUtil.getUserIdFromToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("无法解析refreshToken");
        }

        // 3. 查找数据库中的Token记录
        UserToken userToken = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("refreshToken不存在或已被使用"));

        // 4. 验证Token记录是否有效
        if (userToken.getStatus() != 1) {
            throw new RuntimeException("Token记录已失效");
        }

        // 5. 验证refreshToken是否过期（数据库中的过期时间）
        if (userToken.getRefreshTokenExpiresAt().isBefore(LocalDateTime.now())) {
            // 将旧Token标记为失效
            userToken.setStatus(0);
            userTokenRepository.save(userToken);
            throw new RuntimeException("refreshToken已过期，请重新登录");
        }

        // 6. 验证用户是否存在且有效
        User user = userRepository.findByUserIdAndIsDeleted(userId, 0)
                .orElseThrow(() -> new RuntimeException("用户不存在或已被禁用"));

        // 7. 生成新的Token
        String newAccessToken = jwtUtil.generateAccessToken(userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId);

        // 8. 将旧Token记录标记为失效
        userToken.setStatus(0);
        userTokenRepository.save(userToken);

        // 9. 保存新Token记录
        saveUserToken(userId, newAccessToken, newRefreshToken, httpRequest);

        log.info("【刷新Token成功】userId: {}", userId);

        // 10. 返回新Token
        return com.example.speedcalendarserver.dto.RefreshTokenResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .build();
    }

    /**
     * 检查发送频率限制
     */
    private void checkSendFrequency(String phone) {
        Optional<VerificationCode> latestCodeOpt = verificationCodeRepository
                .findFirstByPhoneOrderByCreatedAtDesc(phone);

        if (latestCodeOpt.isPresent()) {
            VerificationCode latestCode = latestCodeOpt.get();
            LocalDateTime latestTime = latestCode.getCreatedAt();
            LocalDateTime now = LocalDateTime.now();

            long secondsSinceLastSend = java.time.Duration.between(latestTime, now).getSeconds();

            if (secondsSinceLastSend < sendInterval) {
                long remainingSeconds = sendInterval - secondsSinceLastSend;
                throw new RuntimeException("发送过于频繁，请" + remainingSeconds + "秒后再试");
            }
        }
    }

    /**
     * 检查每日发送次数限制
     */
    private void checkDailyLimit(String phone) {
        LocalDateTime dayStart = LocalDateTime.now().minusDays(1);
        long todayCount = verificationCodeRepository.countByPhoneAndCreatedAtAfter(phone, dayStart);

        if (todayCount >= dailyLimit) {
            throw new RuntimeException("今日发送次数已达上限");
        }
    }

    /**
     * 生成验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 标记旧验证码为过期
     */
    private void expireOldCodes(String phone) {
        verificationCodeRepository.findByExpiresAtBeforeAndStatus(LocalDateTime.now(), 0)
                .forEach(VerificationCode::markAsExpired);
    }

    /**
     * 验证验证码
     */
    private void validateVerificationCode(String phone, String code) {
        Optional<VerificationCode> codeOpt = verificationCodeRepository
                .findFirstByPhoneAndCodeAndTypeAndStatusOrderByCreatedAtDesc(
                        phone, code, "login", 0);

        if (codeOpt.isEmpty()) {
            throw new RuntimeException("验证码错误或已失效");
        }

        VerificationCode verificationCode = codeOpt.get();

        // 检查是否过期
        if (!verificationCode.isValid()) {
            throw new RuntimeException("验证码已过期");
        }

        // 标记为已使用
        verificationCode.markAsUsed();
        verificationCodeRepository.save(verificationCode);
    }

    /**
     * 创建新用户
     */
    private User createNewUser(String phone) {
        // 生成默认昵称
        String defaultNickname = "用户" + phone.substring(phone.length() - 4);

        // 生成默认头像
        String defaultAvatar = "https://api.dicebear.com/7.x/initials/svg?seed=" + defaultNickname;

        User user = User.builder()
                .userId(java.util.UUID.randomUUID().toString())
                .phone(phone)
                .username(defaultNickname)
                .avatar(defaultAvatar)
                .loginType("phone")
                .status(1)
                .isDeleted(0)
                .build();

        userRepository.save(user);
        log.info("【注册】新用户注册成功, userId: {}, phone: {}", user.getUserId(), phone);

        return user;
    }

    /**
     * 更新用户登录信息
     */
    private void updateUserLoginInfo(User user, HttpServletRequest httpRequest) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp(httpRequest));
        userRepository.save(user);
    }

    /**
     * 保存Token记录
     */
    private void saveUserToken(String userId, String accessToken, String refreshToken, HttpServletRequest httpRequest) {
        LocalDateTime now = LocalDateTime.now();

        UserToken userToken = UserToken.builder()
                .userId(userId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(now.plusSeconds(jwtUtil.getAccessTokenExpiration()))
                .refreshTokenExpiresAt(now.plusSeconds(jwtUtil.getRefreshTokenExpiration()))
                .deviceType("android") // 可以从请求头获取
                .ipAddress(getClientIp(httpRequest))
                .userAgent(httpRequest.getHeader("User-Agent"))
                .status(1)
                .build();

        userTokenRepository.save(userToken);
    }

    /**
     * 构建登录响应
     */
    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        UserInfo userInfo = UserInfo.fromEntity(user);

        return LoginResponse.builder()
                .userId(user.getUserId())
                .token(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration())
                .userInfo(userInfo)
                .build();
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 更新用户信息 (旧方法)
     */
    @Transactional(rollbackFor = Exception.class)
    public UserInfo updateUserInfo(String userId, UpdateUserInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user.setUsername(request.getUsername().trim());
        }
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar().trim());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getBirthday() != null && !request.getBirthday().trim().isEmpty()) {
            try {
                user.setBirthday(LocalDate.parse(request.getBirthday(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } catch (Exception e) {
                throw new IllegalArgumentException("生日格式错误，应为 yyyy-MM-dd");
            }
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("【更新用户信息】userId: {}, username: {}", userId, user.getUsername());
        return UserInfo.fromEntity(user);
    }

    /**
     * 更新个人资料 (V2.0 新方法)
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 更新昵称
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            user.setUsername(request.getUsername().trim());
        }

        // 更新性别 (中文转数字)
        if (request.getGender() != null) {
            int genderCode = 0; // 默认未知
            switch (request.getGender()) {
                case "男": genderCode = 1; break;
                case "女": genderCode = 2; break;
                case "保密": genderCode = 0; break;
                default: break; // 保持默认
            }
            user.setGender(genderCode);
        }

        // 更新头像
        if (request.getAvatar() != null && !request.getAvatar().trim().isEmpty()) {
            user.setAvatar(request.getAvatar().trim());
        }

        // 手机号和角色通常不允许在此接口修改，故忽略

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        log.info("【更新个人资料】userId: {}, username: {}, gender: {}", userId, user.getUsername(), user.getGender());
    }

    /**
     * 根据用户ID获取用户信息（带隐私过滤）
     * 性能优化：使用 PrivacyService 的缓存机制
     *
     * @param userId      用户ID（被查看者）
     * @param requesterId 请求者ID（查看者），如果为null则返回完整信息
     * @return 用户信息（根据隐私设置过滤）
     */
    public UserInfo getUserInfo(String userId, String requesterId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        UserInfo userInfo = UserInfo.fromEntity(user);

        // 如果没有提供请求者ID，返回完整信息（兼容旧版本）
        if (requesterId == null || requesterId.isEmpty()) {
            return userInfo;
        }

        // 使用隐私服务过滤字段
        return privacyService.filterUserInfo(userInfo, requesterId);
    }

    /**
     * 根据用户ID获取用户信息（完整版本，不过滤）
     * 仅用于内部调用
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserInfo getUserInfo(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return UserInfo.fromEntity(user);
    }
}
