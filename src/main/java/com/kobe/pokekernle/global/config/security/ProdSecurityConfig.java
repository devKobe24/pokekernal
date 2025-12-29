package com.kobe.pokekernle.global.config.security;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. 정적 리소스는 운영에서도 열어줘야 화면이 깨지지 않음
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()

                        // 2. 메인 페이지 및 로그인 페이지 허용
                        .requestMatchers("/", "/error").permitAll()

                        // 3. 관리자 로그인 페이지는 모두 접근 가능
                        .requestMatchers("/admin/login").permitAll()
                        // 회원가입 페이지는 인증 없이 접근 가능하도록 허용
                        .requestMatchers("/register").permitAll()

                        // 4. 카드 목록 및 상세 페이지 허용
                        .requestMatchers("/cards/**").permitAll()

                        // 5. 컬렉션 페이지 허용
                        .requestMatchers("/collection/**").permitAll()

                        // 6. 업로드된 이미지 접근 허용
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/images/**").permitAll()

                        // 8. SEO 파일 허용
                        .requestMatchers("/sitemap.xml", "/robots.txt").permitAll()

                        // API 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        // 장바구니 및 주문서 페이지는 인증된 사용자만 접근 가능
                        .requestMatchers("/cart").authenticated()
                        .requestMatchers("/checkout").authenticated()
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
                        .logoutRequestMatcher(new AntPathRequestMatcher("/admin/logout"))
                        .logoutSuccessUrl("/cards")
                        .invalidateHttpSession(true) // 세션 무효화
                        .deleteCookies("JSESSIONID") // 세션 쿠키 삭제
                        .permitAll()
                )
                // 세션 관리 설정
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // 필요시 세션 생성
                        .maximumSessions(1) // 동시 세션 1개만 허용
                        .maxSessionsPreventsLogin(false) // 새 로그인 시 기존 세션 만료
                        .expiredUrl("/admin/login?expired=true") // 세션 만료 시 리다이렉트
                        .sessionRegistry(sessionRegistry) // 세션 레지스트리 등록
                )
                // 쿠키 보안 설정
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000) // 1년 (HTTPS 환경)
                        )
                        .contentTypeOptions(contentType -> {}) // MIME 타입 스니핑 방지
                        .xssProtection(xss -> {}) // XSS 보호 활성화
                )
                // API 요청에 대해서는 CSRF 보호 비활성화
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/**")
                );
        // Prod 환경에서는 H2 Console 관련 설정(CSRF ignore, FrameOptions)을 하지 않음으로써 보안 강화

        return http.build();
    }
}
