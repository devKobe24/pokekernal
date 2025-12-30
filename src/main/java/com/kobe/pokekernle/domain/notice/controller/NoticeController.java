package com.kobe.pokekernle.domain.notice.controller;

import com.kobe.pokekernle.domain.notice.dto.request.CreateNoticeRequest;
import com.kobe.pokekernle.domain.notice.dto.request.UpdateNoticeRequest;
import com.kobe.pokekernle.domain.notice.dto.response.NoticeResponse;
import com.kobe.pokekernle.domain.notice.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 활성화된 공지사항 목록 조회 (공개 API)
     */
    @GetMapping("/active")
    public ResponseEntity<List<NoticeResponse>> getActiveNotices() {
        List<NoticeResponse> notices = noticeService.getActiveNotices();
        return ResponseEntity.ok(notices);
    }

    /**
     * 모든 공지사항 목록 조회 (관리자용)
     */
    @GetMapping
    public ResponseEntity<List<NoticeResponse>> getAllNotices() {
        List<NoticeResponse> notices = noticeService.getAllNotices();
        return ResponseEntity.ok(notices);
    }

    /**
     * 공지사항 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long id) {
        NoticeResponse notice = noticeService.getNotice(id);
        return ResponseEntity.ok(notice);
    }

    /**
     * 공지사항 생성 (관리자용)
     */
    @PostMapping
    public ResponseEntity<?> createNotice(@Valid @RequestBody CreateNoticeRequest request) {
        try {
            NoticeResponse notice = noticeService.createNotice(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(notice);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[NOTICE] 공지사항 생성 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항 생성 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 공지사항 수정 (관리자용)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotice(@PathVariable Long id, @Valid @RequestBody UpdateNoticeRequest request) {
        try {
            NoticeResponse notice = noticeService.updateNotice(id, request);
            return ResponseEntity.ok(notice);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[NOTICE] 공지사항 수정 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항 수정 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 공지사항 삭제 (관리자용)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotice(@PathVariable Long id) {
        try {
            noticeService.deleteNotice(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("[NOTICE] 공지사항 삭제 실패", e);
            Map<String, String> response = new HashMap<>();
            response.put("message", "공지사항 삭제 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}

