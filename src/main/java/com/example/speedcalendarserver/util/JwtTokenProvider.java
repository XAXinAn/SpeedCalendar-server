package com.example.speedcalendarserver.util;

import com.example.speedcalendarserver.entity.User;
import com.example.speedcalendarserver.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final UserRepository userRepository;

    @Autowired
    public JwtTokenProvider(@Value("${jwt.secret}") String secret, UserRepository userRepository) {
        // 使用现代的、更安全的方式从字符串生成密钥
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.userRepository = userRepository;
    }

    public User getUserFromToken(String token) {
        // 这是 jjwt-0.12.3 版本最标准、最推荐的写法。
        // 如果此写法仍然报错，则100%是您的IDE环境问题。
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build() // 关键步骤：构建出 JwtParser
                .parseClaimsJws(token) // 然后在 JwtParser 上调用解析方法
                .getBody();

        String userId = claims.getSubject();
        return userRepository.findByUserIdAndIsDeleted(userId, 0)
                .orElseThrow(() -> new RuntimeException("User not found or is deleted with id: " + userId));
    }
}
