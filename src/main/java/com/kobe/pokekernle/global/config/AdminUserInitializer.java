package com.kobe.pokekernle.global.config;

import com.kobe.pokekernle.domain.user.entity.Role;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 관리자 계정을 자동으로 생성하는 초기화 클래스
 * - 개발 환경: application-dev.yml의 설정값 사용
 * - 운영 환경: AWS Secrets Manager의 설정값 사용 (admin.email, admin.password, admin.nickname)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@pokekernel.com}")
    private String adminEmail;

    @Value("${admin.password:admin123}")
    private String adminPassword;

    @Value("${admin.nickname:Administrator}")
    private String adminNickname;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("[ADMIN INIT] 관리자 계정 초기화 시작");
        log.info("[ADMIN INIT]   이메일: {}", adminEmail);
        log.info("[ADMIN INIT]   비밀번호 길이: {} (값은 로그에 표시하지 않음)", adminPassword != null ? adminPassword.length() : 0);
        log.info("[ADMIN INIT]   닉네임: {}", adminNickname);

        // 이미 관리자 계정이 있는지 확인
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("[ADMIN INIT] 관리자 계정이 이미 존재합니다: {}", adminEmail);
            return;
        }

        // 관리자 계정 생성
        if (adminEmail == null || adminEmail.isEmpty()) {
            log.warn("[ADMIN INIT] 관리자 이메일이 설정되지 않았습니다. 계정 생성을 건너뜁니다.");
            return;
        }

        if (adminPassword == null || adminPassword.isEmpty()) {
            log.warn("[ADMIN INIT] 관리자 비밀번호가 설정되지 않았습니다. 계정 생성을 건너뜁니다.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(adminPassword);
        User admin = User.builder()
                .email(adminEmail)
                .password(encodedPassword)
                .nickname(adminNickname != null ? adminNickname : "Administrator")
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("[ADMIN INIT] 관리자 계정이 생성되었습니다:");
        log.info("[ADMIN INIT]   이메일: {}", adminEmail);
        log.info("[ADMIN INIT]   닉네임: {}", adminNickname);
    }
}

