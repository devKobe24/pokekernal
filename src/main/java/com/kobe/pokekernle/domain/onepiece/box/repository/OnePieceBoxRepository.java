package com.kobe.pokekernle.domain.onepiece.box.repository;

import com.kobe.pokekernle.domain.onepiece.box.entity.OnePieceBox;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * packageName    : com.kobe.pokekernle.domain.onepiece.box.repository
 * fileName       : OnePieceBoxRepository
 * author         : kobe
 * date           : 2026. 1. 9.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026. 1. 9.        kobe       최초 생성
 */
public interface OnePieceBoxRepository extends JpaRepository<OnePieceBox, Long> {
    Optional<OnePieceBox> findBySetName(String setName);
}
