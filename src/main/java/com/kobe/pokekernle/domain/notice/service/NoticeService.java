package com.kobe.pokekernle.domain.notice.service;

import com.kobe.pokekernle.domain.notice.dto.request.CreateNoticeRequest;
import com.kobe.pokekernle.domain.notice.dto.request.UpdateNoticeRequest;
import com.kobe.pokekernle.domain.notice.dto.response.NoticeResponse;
import com.kobe.pokekernle.domain.notice.entity.Notice;
import com.kobe.pokekernle.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 활성화된 공지사항 목록 조회 (사용자용)
     */
    public List<NoticeResponse> getActiveNotices() {
        return noticeRepository.findByIsActiveTrueOrderByPriorityDescCreatedAtDesc().stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 모든 공지사항 목록 조회 (관리자용)
     */
    public List<NoticeResponse> getAllNotices() {
        return noticeRepository.findAllByOrderByPriorityDescCreatedAtDesc().stream()
                .map(NoticeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 공지사항 상세 조회
     */
    public NoticeResponse getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + id));
        return NoticeResponse.from(notice);
    }

    /**
     * 공지사항 생성
     */
    @Transactional
    public NoticeResponse createNotice(CreateNoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .build();

        Notice savedNotice = noticeRepository.save(notice);
        log.info("[NOTICE] 공지사항 생성 - ID: {}, Title: {}", savedNotice.getId(), savedNotice.getTitle());
        return NoticeResponse.from(savedNotice);
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public NoticeResponse updateNotice(Long id, UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + id));

        notice.update(request.getTitle(), request.getContent(), request.getIsActive(), request.getPriority());
        log.info("[NOTICE] 공지사항 수정 - ID: {}, Title: {}", notice.getId(), notice.getTitle());
        return NoticeResponse.from(notice);
    }

    /**
     * 공지사항 삭제
     */
    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다. ID: " + id));

        noticeRepository.delete(notice);
        log.info("[NOTICE] 공지사항 삭제 - ID: {}", id);
    }
}

