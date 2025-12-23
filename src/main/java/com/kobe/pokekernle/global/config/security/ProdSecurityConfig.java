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
 * fileName       : ProdSecurityConfig
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
@Profile("prod") // "prod" 프로필일 때만 활성화됨
public class ProdSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. 정적 리소스는 운영에서도 열어줘야 화면이 깨지지 않음
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // 2. 메인 페이지 및 로그인 페이지 허용
                        .requestMatchers("/", "/login", "/error").permitAll()

                        // 3. 카드 목록 및 상세 페이지 허용
                        .requestMatchers("/cards/**").permitAll()

                        // 4. 컬렉션 페이지 허용
                        .requestMatchers("/collection/**").permitAll()

                        // 5. API 요청 허용
                        .requestMatchers("/api/**").permitAll()

                        // 6. 업로드된 이미지 접근 허용
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        // 7. 관리자 페이지는 ADMIN 권한만 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 8. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 운영 환경에서는 폼 로그인 기능을 활성화
                .formLogin(login -> login
                        .loginPage("/login") // 커스텀 로그인 페이지 경로 (나중에 만들기)
                        .defaultSuccessUrl("/") // 로그인 성공 시 이동할 경로
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );
        // Prod 환경에서는 H2 Console 관련 설정(CSRF ignore, FrameOptions)을 하지 않음으로써 보안 강화

        return http.build();
    }
}
