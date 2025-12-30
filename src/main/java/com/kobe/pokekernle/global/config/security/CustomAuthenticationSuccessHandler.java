package com.kobe.pokekernle.global.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AJAX 요청을 감지하여 JSON 응답을 보내는 커스텀 인증 성공 핸들러
 */
@Slf4j
@Component
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthenticationSuccessHandler() {
        // 기본 리다이렉트 URL 설정 (역할에 따라 동적으로 변경됨)
        setDefaultTargetUrl("/cards");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        log.info("[AUTH] 로그인 성공 핸들러 호출됨 - Request URI: {}, Method: {}", 
                request.getRequestURI(), request.getMethod());
        
        // 사용자 역할에 따라 리다이렉트 URL 결정
        String redirectUrl = "/cards"; // 기본값: 일반 사용자
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            redirectUrl = "/admin/cards/register"; // 관리자는 관리자 페이지로
        }
        
        // AJAX 요청인지 확인
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        log.info("[AUTH] AJAX 요청 여부: {}, 리다이렉트 URL: {}", isAjax, redirectUrl);

        if (isAjax) {
            // AJAX 요청인 경우 JSON 응답 반환
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "로그인 성공");
            result.put("redirectUrl", redirectUrl);

            objectMapper.writeValue(response.getWriter(), result);
            log.info("[AUTH] AJAX 로그인 성공 - 사용자: {}, 역할: {}, 리다이렉트: {}", 
                    authentication.getName(), 
                    authentication.getAuthorities(), 
                    redirectUrl);
        } else {
            // 일반 요청인 경우 역할에 따라 리다이렉트
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            log.info("[AUTH] 일반 로그인 성공 - 사용자: {}, 역할: {}, 리다이렉트: {}", 
                    authentication.getName(), 
                    authentication.getAuthorities(), 
                    redirectUrl);
        }
    }
}

