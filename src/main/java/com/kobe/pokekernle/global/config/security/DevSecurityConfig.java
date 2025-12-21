package com.kobe.pokekernle.global.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * packageName    : com.kobe.pokekernle.global.config
 * fileName       : DevSecurityConfig
 * author         : kobe
 * date           : 2025. 12. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 20.        kobe       최초 생성
 */
@Configuration
@EnableWebSecurity
@Profile("dev") // "dev" 프로필일 때만 활성화됨
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. H2 Console 자동 허용 (Spring Boot 도구 활용)
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        // 2. 정적 리소스(js, css, images) 자동 허용
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/admin/**").permitAll()
                        .requestMatchers("/cards/**").permitAll() // 카드 목록 페이지 허용
                        // 3. 메인 페이지 및 개발용 테스트 경로 허용
                        .requestMatchers("/", "/api/test/**").permitAll()
                        .anyRequest().authenticated()
                )
                // H2 Console은 iframe을 사용하므로 X-Frame-Options 설정 필요
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                // H2 Console 사용 시 CSRF 보호를 꺼야 함
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(PathRequest.toH2Console())
                );

        return http.build();
    }
}
