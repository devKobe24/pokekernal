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
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    public DevSecurityConfig(CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                            CustomAuthenticationFailureHandler customAuthenticationFailureHandler) {
        this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
        this.customAuthenticationFailureHandler = customAuthenticationFailureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
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
                        // 4. 로그인 및 회원가입 페이지는 모두 접근 가능
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/admin/login").permitAll()
                        // 회원가입 페이지는 인증 없이 접근 가능하도록 허용
                        .requestMatchers("/register").permitAll()
                        // API 경로는 인증된 사용자만 접근 가능
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/orders/**").authenticated()
                        // 장바구니 및 주문서 페이지는 인증된 사용자만 접근 가능
                        .requestMatchers("/cart").authenticated()
                        .requestMatchers("/checkout").authenticated()
                        // 공지사항 API는 공개 (활성화된 공지사항만)
                        .requestMatchers("/api/notices/active").permitAll()
                        // 공지사항 관리 API는 ADMIN 권한 필요
                        .requestMatchers("/api/notices/**").hasRole("ADMIN")
                        // 5. 관리자 페이지는 ADMIN 권한 필요
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cards/**").permitAll() // 카드 목록 페이지 허용
                        .requestMatchers("/shop/**").permitAll() // SHOP 카테고리 페이지 허용
                        .requestMatchers("/collection/**").permitAll()
                        .requestMatchers("/").permitAll()
                        .anyRequest().authenticated()
                )
                // 폼 로그인 설정
                .formLogin(login -> login
                        .loginPage("/login") // 로그인 페이지 경로
                        .loginProcessingUrl("/login") // 로그인 처리 URL
                        .successHandler(customAuthenticationSuccessHandler) // 커스텀 성공 핸들러 사용 (AJAX 지원, 역할별 리다이렉트)
                        .failureHandler(customAuthenticationFailureHandler) // 커스텀 실패 핸들러 사용 (AJAX 지원)
                        .defaultSuccessUrl("/cards", true) // 일반 요청 시 리다이렉트 경로 (fallback, 역할별로 동적 변경됨)
                        .failureUrl("/login?error=true") // 일반 요청 시 실패 URL (fallback)
                        .usernameParameter("email") // 이메일을 사용자명으로 사용
                        .passwordParameter("password")
                        .permitAll()
                )
                // 로그아웃 설정
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
                        .frameOptions(frame -> frame.sameOrigin()) // H2 Console은 iframe을 사용하므로 X-Frame-Options 설정 필요
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(0) // 개발 환경에서는 비활성화
                        )
                )
                // H2 Console 사용 시 CSRF 보호를 꺼야 함
                .csrf(csrf -> csrf
                        // H2 Console 허용
                        .ignoringRequestMatchers(PathRequest.toH2Console())
                        // API 요청에 대해서는 CSRF 보호 비활성화 (POST 403 에러 해결)
                        // 개발 환경에서는 /admin/** POST 요청도 CSRF 비활성화 (편의상)
                        .ignoringRequestMatchers("/admin/**", "/api/**")
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/login", "POST")) // 로그인 POST 요청도 CSRF 비활성화
                );

        return http.build();
    }
}
