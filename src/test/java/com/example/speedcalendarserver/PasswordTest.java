package com.example.speedcalendarserver;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "111111";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("明文: " + rawPassword);
        System.out.println("密文: " + encodedPassword);
    }
}
