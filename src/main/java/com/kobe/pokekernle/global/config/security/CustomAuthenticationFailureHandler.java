package com.kobe.pokekernle.global.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AJAX 요청을 감지하여 JSON 응답을 보내는 커스텀 인증 실패 핸들러
 */
@Slf4j
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CustomAuthenticationFailureHandler() {
        // 일반 요청 시 기본 실패 URL 설정
        setDefaultFailureUrl("/admin/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        
        log.info("[AUTH] 로그인 실패 핸들러 호출됨 - Request URI: {}, Method: {}, Error: {}", 
                request.getRequestURI(), request.getMethod(), exception.getMessage());
        
        // AJAX 요청인지 확인
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
        log.info("[AUTH] AJAX 요청 여부: {}", isAjax);

        if (isAjax) {
            // AJAX 요청인 경우 JSON 응답 반환
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "이메일 또는 비밀번호가 올바르지 않습니다.");

            objectMapper.writeValue(response.getWriter(), result);
            log.info("[AUTH] AJAX 로그인 실패: {}", exception.getMessage());
        } else {
            // 일반 요청인 경우 기본 리다이렉트 동작 수행
            super.onAuthenticationFailure(request, response, exception);
            log.info("[AUTH] 일반 로그인 실패: {}", exception.getMessage());
        }
    }
}

