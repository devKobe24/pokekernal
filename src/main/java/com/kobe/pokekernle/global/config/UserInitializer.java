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
 * packageName    : com.kobe.pokekernle.global
 * fileName       : UserInitializer
 * author         : kobe
 * date           : 2025. 12. 31.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 31.        kobe       최초 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${user.email:user@pokekernel.com}")
    private String userEmail;

    @Value("${user.password:user123}")
    private String userPassword;

    @Value("${user.nickname:User}")
    private String userNickname;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("[USER INIT] 유저 계정 초기화 시작");
        log.info("[USER INIT]      이메일: {}", userEmail);
        log.info("[USER INIT]      비밀번호 길이: {} (값은 로그에 표시하지 않음)", userPassword != null ? userPassword.length() : 0);
        log.info("[USER INIT]      닉네임: {}", userNickname);

        // 이미 유저 계정이 있는지 확인
        if (userRepository.findByEmail(userEmail).isPresent()) {
            log.info("[USER INIT] 유저 계정이 이미 존재합니다: {}", userEmail);
            return;
        }

        // 유저 계정 생성
        if (userEmail == null || userEmail.isEmpty()) {
            log.warn("[USER INIT] 유저 이메일이 설정되지 않았습니다. 계정 생성을 건너뜁니다.");
            return;
        }

        if (userPassword == null || userPassword.isEmpty()) {
            log.warn("[USER INIT] 유저 비밀번호가 설정되지 않았습니다. 계정 생성을 건너뜁니다.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(userPassword);
        User user = User.builder()
                .email(userEmail)
                .password(encodedPassword)
                .nickname(userNickname != null ? userNickname : "User")
                .role(Role.USER)
                .build();

        userRepository.save(user);
        log.info("[USER INIT] 유저 계정이 생성되었습니다:");
        log.info("[USER INIT]      이메일: {}", userEmail);
        log.info("[USER INIT]      닉네임: {}", userNickname);
    }
}
