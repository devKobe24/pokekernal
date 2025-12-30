package com.kobe.pokekernle.domain.notice.entity;

import com.kobe.pokekernle.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공지사항 엔티티
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notices")
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title; // 공지사항 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 공지사항 내용

    @Column(nullable = false)
    private Boolean isActive = true; // 활성화 여부

    @Column(nullable = false)
    private Integer priority = 0; // 우선순위 (높을수록 먼저 표시)

    @Builder
    public Notice(String title, String content, Boolean isActive, Integer priority) {
        this.title = title;
        this.content = content;
        this.isActive = isActive != null ? isActive : true;
        this.priority = priority != null ? priority : 0;
    }

    /**
     * 공지사항 수정
     */
    public void update(String title, String content, Boolean isActive, Integer priority) {
        if (title != null && !title.isBlank()) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (isActive != null) {
            this.isActive = isActive;
        }
        if (priority != null) {
            this.priority = priority;
        }
    }
}

