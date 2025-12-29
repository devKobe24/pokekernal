package com.kobe.pokekernle.global.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
                        // 3. 이미지 폴더 명시적 허용
                        .requestMatchers("/images/**").permitAll()
                        // 4. favicon 허용
                        .requestMatchers("/favicon.ico").permitAll()
                        // 5. 업로드된 이미지 접근 허용
                        .requestMatchers("/uploads/**").permitAll()
                        // 6. SEO 파일 허용
                        .requestMatchers("/sitemap.xml", "/robots.txt").permitAll()
                        // 4. 로그인 페이지는 모두 접근 가능
                        .requestMatchers("/admin/login").permitAll()
                        // 회원가입 페이지는 인증 없이 접근 가능하도록 허용
                        .requestMatchers("/register").permitAll()
                        // 5. 관리자 페이지는 ADMIN 권한 필요
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cards/**").permitAll() // 카드 목록 페이지 허용
                        .requestMatchers("/collection/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(login -> login
                        .loginPage("/admin/login") // 로그인 페이지 경로
                        .loginProcessingUrl("/admin/login") // 로그인 처리 URL
                        .defaultSuccessUrl("/admin/cards/register", true) // 로그인 성공 시 이동할 경로
                        .failureUrl("/admin/login?error=true") // 로그인 실패 시 이동할 경로
                        .usernameParameter("email") // 이메일을 사용자명으로 사용
                        .passwordParameter("password")
                        .permitAll()
                )
                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                        .logoutSuccessUrl("/cards")
                        .permitAll()
                )
                // H2 Console은 iframe을 사용하므로 X-Frame-Options 설정 필요
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                // H2 Console 사용 시 CSRF 보호를 꺼야 함
                .csrf(csrf -> csrf
                        // H2 Console 허용
                        .ignoringRequestMatchers(PathRequest.toH2Console())
                        // API 요청에 대해서는 CSRF 보호 비활성화 (POST 403 에러 해결)
                        // 개발 환경에서는 /admin/** POST 요청도 CSRF 비활성화 (편의상)
                        .ignoringRequestMatchers("/admin/**")
                );

        return http.build();
    }
}
