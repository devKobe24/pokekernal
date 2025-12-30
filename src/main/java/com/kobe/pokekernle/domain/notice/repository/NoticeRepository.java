package com.kobe.pokekernle.domain.notice.repository;

import com.kobe.pokekernle.domain.notice.entity.Notice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    /**
     * 활성화된 공지사항 목록 조회 (우선순위 내림차순)
     */
    List<Notice> findByIsActiveTrueOrderByPriorityDescCreatedAtDesc();

    /**
     * 모든 공지사항 목록 조회 (우선순위 내림차순)
     */
    List<Notice> findAllByOrderByPriorityDescCreatedAtDesc();
}

