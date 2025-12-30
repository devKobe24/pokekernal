package com.kobe.pokekernle.domain.notice.controller;

import com.kobe.pokekernle.domain.notice.dto.response.NoticeResponse;
import com.kobe.pokekernle.domain.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/notices")
@RequiredArgsConstructor
public class NoticeViewController {

    private final NoticeService noticeService;

    @GetMapping
    public String noticeManagementPage(Model model) {
        List<NoticeResponse> notices = noticeService.getAllNotices();
        model.addAttribute("notices", notices);
        return "admin/notices";
    }
}

