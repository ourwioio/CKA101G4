package com.webond.venue;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123";
        String hashedPassword = encoder.encode(rawPassword);
        
        System.out.println("=====================================");
        System.out.println("你的原始密碼: " + rawPassword);
        System.out.println("產生的加密 Hash: " + hashedPassword);
        System.out.println("=====================================");
    }
}