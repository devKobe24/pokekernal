package com.kobe.pokekernle.domain.collection.repository;

import com.kobe.pokekernle.domain.collection.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.repository
 * fileName       : UserCardRepository
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    // 특정 유저의 보유 카드 목록 조회 (최신순)
    List<UserCard> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
