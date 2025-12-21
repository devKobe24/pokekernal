package com.kobe.pokekernle.domain.collection.repository;

import com.kobe.pokekernle.domain.collection.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
