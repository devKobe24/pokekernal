package com.kobe.pokekernle.domain.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 관리자 로그인 페이지 컨트롤러
 */
@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login"; // templates/admin/login.html
    }
}

