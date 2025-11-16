package com.example.speedcalendarserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * BCrypt密码加密配置
 *
 * @author SpeedCalendar Team
 * @since 2025-11-16
 */
@Configuration
public class BcryptConfig {

    /**
     * 注册PasswordEncoder Bean
     * 使用BCrypt算法进行密码加密
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
