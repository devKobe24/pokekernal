package com.kobe.pokekernle.domain.user.repository;

import com.kobe.pokekernle.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * packageName    : com.kobe.pokekernle.domain.user.repository
 * fileName       : UserRepository
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); // 로그인 시 필요
}
