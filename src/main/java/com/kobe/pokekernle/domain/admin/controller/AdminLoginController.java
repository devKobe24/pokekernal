package com.kobe.pokekernle.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 로그인 페이지 컨트롤러
 * 일반 로그인 페이지로 리다이렉트 (역할에 따라 자동으로 관리자 페이지로 이동)
 */
@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/login"; // 일반 로그인 페이지로 리다이렉트
    }
}

