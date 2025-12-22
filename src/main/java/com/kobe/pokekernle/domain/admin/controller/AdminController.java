package com.kobe.pokekernle.domain.admin.controller;

import com.kobe.pokekernle.domain.admin.service.ImageUploadService;
import com.kobe.pokekernle.domain.card.service.CardPriceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.CompletableFuture;

/**
 * packageName    : com.kobe.pokekernle.domain.admin.controller
 * fileName       : AdminController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CardPriceSyncService cardPriceSyncService;
    private final ImageUploadService imageUploadService;

    // 1. 카드 등록 페이지 보여주기
    @GetMapping("/cards/register")
    public String registerPage() {
        return "admin/register"; // templates/admin/register.html
    }

    // 2. 입력받은 정보로 API 검색 및 DB 저장 (기존 서비스 재활용)
    @PostMapping("/cards/register")
    public String registerCard(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String setId,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) String salePrice,
            RedirectAttributes redirectAttributes
    ) {
        // 이미지 업로드 처리
        String uploadedImageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                uploadedImageUrl = imageUploadService.uploadImage(imageFile);
                log.info("[ADMIN] 이미지 업로드 완료: {}", uploadedImageUrl);
            } catch (Exception e) {
                log.error("[ADMIN] 이미지 업로드 실패", e);
                redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
                return "redirect:/admin/cards/register";
            }
        }

        // 검색 쿼리 조합 (예: "name:pikachu number:025")
        StringBuilder queryBuilder = new StringBuilder();

        if (name != null && !name.isBlank()) {
            queryBuilder.append("name:\"").append(name).append("\" ");
        }
        if (number != null && !number.isBlank()) {
            queryBuilder.append("number:").append(number).append(" ");
        }
        if (setId != null && !setId.isBlank()) {
            queryBuilder.append("set.id:").append(setId);
        }

        String query = queryBuilder.toString().trim();

        // 이미지만 업로드하고 쿼리가 없는 경우
        if (query.isEmpty() && uploadedImageUrl == null) {
            redirectAttributes.addFlashAttribute("error", "최소한 하나의 정보는 입력해야 합니다.");
            return "redirect:/admin/cards/register";
        }

        // 이미지만 업로드한 경우
        if (query.isEmpty() && uploadedImageUrl != null) {
            redirectAttributes.addFlashAttribute("message", "이미지가 업로드되었습니다: " + uploadedImageUrl + " (카드 정보를 입력하면 자동으로 연결됩니다)");
            return "redirect:/admin/cards/register";
        }

        try {
            // 판매 가격 파싱 (원화)
            Long salePriceLong = null;
            if (salePrice != null && !salePrice.isBlank()) {
                try {
                    salePriceLong = Long.parseLong(salePrice.trim());
                } catch (NumberFormatException e) {
                    log.warn("[ADMIN] 판매 가격 파싱 실패: {}", salePrice);
                }
            }
            
            // 기존에 만든 배치 서비스를 여기서 호출해서 즉시 저장!
            // 업로드된 이미지 URL과 판매 가격 전달
            cardPriceSyncService.syncLatestPrices(query, uploadedImageUrl, salePriceLong);
            
            String successMessage = "카드 검색 및 등록이 완료되었습니다! (Query: " + query + ")";
            if (uploadedImageUrl != null) {
                successMessage += " 이미지도 업로드되어 적용되었습니다: " + uploadedImageUrl;
            }
            if (salePriceLong != null) {
                successMessage += " 희망 판매 가격: ₩" + String.format("%,d", salePriceLong);
            }
            redirectAttributes.addFlashAttribute("message", successMessage);
        } catch (Exception e) {
            String errorMessage = "등록 중 오류 발생: " + e.getMessage();
            if (uploadedImageUrl != null) {
                errorMessage += " (이미지는 업로드되었습니다: " + uploadedImageUrl + ")";
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
        }

        return "redirect:/admin/cards/register";
    }

    /**
     * 수동 데이터 수집 트리거
     * 사용법: 
     *   - 특정 세트: http://localhost:8080/admin/collect?query=set.id:sv3pt5
     *   - 특정 카드 (세트 제한 권장): http://localhost:8080/admin/collect?query=name:pikachu set.id:sv3pt5
     * 비동기 처리로 변경하여 타임아웃 방지
     */
    @GetMapping("/collect")
    @ResponseBody
    public String triggerCardCollection(@RequestParam String query) {
        log.info("[ADMIN] 수동 데이터 수집 요청 - Query: {}", query);

        // 광범위한 쿼리 경고
        String warningMessage = "";
        if (query.contains("name:") && !query.contains("set.id:")) {
            warningMessage = "<br><strong style='color: orange;'>⚠ 경고: 광범위한 쿼리입니다. 특정 세트로 제한하는 것을 권장합니다. (예: name:pikachu set.id:sv3pt5)</strong>";
        }

        // 비동기로 처리하여 즉시 응답 반환 (백그라운드에서 수집 진행)
        CompletableFuture.runAsync(() -> {
            try {
                cardPriceSyncService.syncLatestPrices(query);
                log.info("[ADMIN] 데이터 수집 완료 - Query: {}", query);
            } catch (Exception e) {
                log.error("[ADMIN] 데이터 수집 중 오류 발생 - Query: {}", query, e);
            }
        });

        return String.format(
            "<html><body>" +
            "<h3>수집 작업이 시작되었습니다!</h3>" +
            "<p><strong>Query:</strong> %s</p>" +
            "<p>백그라운드에서 처리 중입니다. 로그와 DB를 확인하세요.</p>" +
            "%s" +
            "<p><small>참고: 광범위한 쿼리는 타임아웃이 발생할 수 있습니다. 특정 세트로 제한하는 것을 권장합니다.</small></p>" +
            "</body></html>",
            query, warningMessage
        );
    }
}
