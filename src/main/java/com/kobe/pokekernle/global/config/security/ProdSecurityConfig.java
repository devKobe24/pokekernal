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
                        .requestMatchers("/", "/error").permitAll()

                        // 3. 관리자 로그인 페이지는 모두 접근 가능
                        .requestMatchers("/admin/login").permitAll()

                        // 4. 카드 목록 및 상세 페이지 허용
                        .requestMatchers("/cards/**").permitAll()

                        // 5. 컬렉션 페이지 허용
                        .requestMatchers("/collection/**").permitAll()

                        // 6. API 요청 허용
                        .requestMatchers("/api/**").permitAll()

                        // 7. 업로드된 이미지 접근 허용
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        // 8. SEO 파일 허용
                        .requestMatchers("/sitemap.xml", "/robots.txt").permitAll()

                        // 9. 관리자 페이지는 ADMIN 권한만 접근 가능
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // 8. 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 운영 환경에서는 폼 로그인 기능을 활성화
                .formLogin(login -> login
                        .loginPage("/admin/login") // 관리자 로그인 페이지 경로
                        .loginProcessingUrl("/admin/login") // 로그인 처리 URL
                        .defaultSuccessUrl("/admin/cards/register", true) // 로그인 성공 시 이동할 경로
                        .failureUrl("/admin/login?error=true") // 로그인 실패 시 이동할 경로
                        .usernameParameter("email") // 이메일을 사용자명으로 사용
                        .passwordParameter("password")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/admin/logout"))
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .permitAll()
                );
        // Prod 환경에서는 H2 Console 관련 설정(CSRF ignore, FrameOptions)을 하지 않음으로써 보안 강화

        return http.build();
    }
}
