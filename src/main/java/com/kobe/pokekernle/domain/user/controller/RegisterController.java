package com.kobe.pokekernle.domain.user.controller;

import com.kobe.pokekernle.domain.user.dto.request.RegisterRequest;
import com.kobe.pokekernle.domain.user.entity.Role;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import com.kobe.pokekernle.global.config.rate.RateLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RateLimitService rateLimitService;

    @GetMapping("/register")
    public String registerRedirect() {
        // Render a dedicated registration page
        return "register"; // templates/register.html
    }

    @PostMapping(value = "/register")
    public Object register(
            @Valid
            @ModelAttribute
            RegisterRequest request,
            BindingResult bindingResult,
            RedirectAttributes ra,
            HttpServletRequest httpRequest
    ) {
        boolean isAjax = "XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"))
                || (httpRequest.getHeader("Accept") != null && httpRequest.getHeader("Accept").contains("application/json"));

        // Bean Validation 검증 실패 시 처리
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));

            log.warn("[REGISTER] Validation 실패: {}", errorMessage);

            if (isAjax) {
                Map<String, String> body = new HashMap<>();
                body.put("message", errorMessage);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body);
            }
            ra.addFlashAttribute("registerError", errorMessage);
            return "redirect:/cards?signup=1";
        }

        // 이메일 기반 Rate Limit 체크 (Validation 통과 후)
        if (!rateLimitService.tryConsumeByEmail(request.getEmail())) {
            log.warn("[REGISTER] Email Rate Limit 초과: {}", request.getEmail());
            String errorMessage = "요청이 너무 많습니다. 잠시 후 다시 시도해주세요";

            if (isAjax) {
                Map<String, String> body = new HashMap<>();
                body.put("message", errorMessage);
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body);
            }
            ra.addFlashAttribute("registerError", errorMessage);
            return "redirect:/cards?signup=1";
        }

        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("[REGISTER] 이메일 중복 시도: {}", request.getEmail());
            String errorMessage = "이미 존재하는 이메일입니다.";

            if (isAjax) {
                Map<String, String> body = new HashMap<>();
                body.put("message", errorMessage);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body);
            }
            ra.addFlashAttribute("registerError", errorMessage);
            return "redirect:/cards?signup=1";
        }

        // 사용자 생성 및 저장
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.USER)
                .build();

        userRepository.save(user);
        log.info("[REGISTER] 회원가입 성공: Email={}", request.getEmail());

        if (isAjax) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }

        return "redirect:/cards?signup=success";
    }
}
