package com.kobe.pokekernle.global.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * packageName    : com.kobe.pokekernle.global.config
 * fileName       : GlobalSecurityConfig
 * author         : kobe
 * date           : 2025. 12. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 20.        kobe       최초 생성
 */
@Configuration
public class GlobalSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt는 강력한 해시 함수로, 비밀번호 암호화의 표준입니다.
        return new BCryptPasswordEncoder();
    }
}
