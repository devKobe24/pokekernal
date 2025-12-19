package com.kobe.pokekernle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * packageName    : com.kobe.pokekernle.controller
 * fileName       : MainController
 * author         : kobe
 * date           : 2025. 12. 20.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 20.        kobe       최초 생성
 */
@Controller
public class MainController {

    @GetMapping("/")
    public String index() {
        // src/main/resources/templates/index.html을 찾아감
        return "index";
    }
}
