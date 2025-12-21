package com.kobe.pokekernle.domain.user.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * packageName    : com.kobe.pokekernle.domain.user.entity
 * fileName       : Role
 * author         : kobe
 * date           : 2025. 12. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 20.        kobe       최초 생성
 */
@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;
    private final String title;
}
