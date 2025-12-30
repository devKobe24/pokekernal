package com.kobe.pokekernle.domain.notice.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateNoticeRequest {

    private String title;

    private String content;

    private Boolean isActive;

    private Integer priority;
}

