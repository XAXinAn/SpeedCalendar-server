package com.example.speedcalendarserver.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "111111";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("明文密码: " + rawPassword);
        System.out.println("BCrypt密文: " + encodedPassword);
    }
}
