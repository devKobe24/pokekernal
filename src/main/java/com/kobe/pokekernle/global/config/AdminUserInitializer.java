package com.kobe.pokekernle.global.config;

import com.kobe.pokekernle.domain.user.entity.Role;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 개발 환경에서 관리자 계정을 자동으로 생성하는 초기화 클래스
 * application-dev.yml의 설정값을 읽어서 관리자 계정을 생성합니다.
 */
@Slf4j
@Component
@Profile("dev")
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
        // 이미 관리자 계정이 있는지 확인
        if (userRepository.findByEmail(adminEmail).isPresent()) {
            log.info("[ADMIN INIT] 관리자 계정이 이미 존재합니다: {}", adminEmail);
            return;
        }

        // 관리자 계정 생성
        String encodedPassword = passwordEncoder.encode(adminPassword);
        User admin = User.builder()
                .email(adminEmail)
                .password(encodedPassword)
                .nickname(adminNickname)
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("[ADMIN INIT] 관리자 계정이 생성되었습니다:");
        log.info("[ADMIN INIT]   이메일: {}", adminEmail);
        log.info("[ADMIN INIT]   비밀번호: {} (application-dev.yml에서 변경 가능)", adminPassword);
        log.info("[ADMIN INIT]   닉네임: {}", adminNickname);
    }
}

