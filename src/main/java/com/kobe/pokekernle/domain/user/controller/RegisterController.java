package com.kobe.pokekernle.domain.user.controller;

import com.kobe.pokekernle.domain.user.entity.Role;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/register")
    public String registerRedirect() {
        // Redirect to cards list and instruct it to open the signup modal
        return "redirect:/cards?signup=1";
    }

    @PostMapping(value = "/register")
    public Object register(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String nickname,
            RedirectAttributes ra,
            HttpServletRequest request
    ) {
        boolean isAjax = "XMLHttpRequest".equals(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"));

        if (userRepository.findByEmail(email).isPresent()) {
            if (isAjax) {
                Map<String, String> body = new HashMap<>();
                body.put("message", "이미 존재하는 이메일입니다.");
                return ResponseEntity.status(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(body);
            }
            ra.addFlashAttribute("registerError", "이미 존재하는 이메일입니다.");
            return "redirect:/cards?signup=1";
        }

        String encoded = passwordEncoder.encode(password);
        User user = User.builder()
                .email(email)
                .password(encoded)
                .nickname(nickname)
                .role(Role.USER)
                .build();

        userRepository.save(user);

        if (isAjax) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "회원가입이 완료되었습니다.");
            return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(body);
        }

        return "redirect:/cards?signup=success";
    }
}
