package com.kobe.pokekernle.domain.admin.controller;

import com.kobe.pokekernle.domain.admin.service.ImageUploadService;
import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.Rarity;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.card.repository.MarketPriceRepository;
import com.kobe.pokekernle.domain.card.repository.PriceHistoryRepository;
import com.kobe.pokekernle.domain.card.response.CardListResponse;
import com.kobe.pokekernle.domain.card.service.CardPriceSyncService;
import com.kobe.pokekernle.domain.card.service.CardService;
import com.kobe.pokekernle.domain.collection.repository.UserCardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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
    private final CardService cardService;
    private final CardRepository cardRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final UserCardRepository userCardRepository;

    // 1. 카드 등록 페이지 보여주기
    @GetMapping("/cards/register")
    public String registerPage() {
        return "admin/register"; // templates/admin/register.html
    }

    // 2. 관리자용 카드 목록 페이지
    @GetMapping("/cards/list")
    public String cardList(Model model) {
        List<CardListResponse> cards = cardService.getAllCards();
        model.addAttribute("cards", cards);
        return "admin/card-list"; // templates/admin/card-list.html
    }

    // 2-1. 관리자용 카드 목록 데이터 (JSON, AJAX용)
    @GetMapping("/cards/list-data")
    @ResponseBody
    public ResponseEntity<List<CardListResponse>> cardListData() {
        List<CardListResponse> cards = cardService.getAllCards();
        return ResponseEntity.ok(cards);
    }

    // 3. 입력받은 정보로 API 검색 및 DB 저장 (기존 서비스 재활용)
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

    // 4. 수동 데이터 수집 트리거
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

    // 5. 카드 수정 페이지 보여주기
    @GetMapping("/cards/edit/{id}")
    public String editPage(@PathVariable Long id, Model model) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다. ID=" + id));
        model.addAttribute("card", card);
        model.addAttribute("rarities", Rarity.values());
        return "admin/edit"; // templates/admin/edit.html
    }

    // 6. 카드 수정 처리
    @PostMapping("/cards/edit/{id}")
    @Transactional
    public String updateCard(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String setName,
            @RequestParam(required = false) String number,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) MultipartFile imageFile,
            @RequestParam(required = false) String salePrice,
            RedirectAttributes redirectAttributes
    ) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다. ID=" + id));

        // 이미지 업로드 처리
        String uploadedImageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                uploadedImageUrl = imageUploadService.uploadImage(imageFile);
                log.info("[ADMIN] 이미지 업로드 완료: {}", uploadedImageUrl);
            } catch (Exception e) {
                log.error("[ADMIN] 이미지 업로드 실패", e);
                redirectAttributes.addFlashAttribute("error", "이미지 업로드 실패: " + e.getMessage());
                return "redirect:/admin/cards/edit/" + id;
            }
        }

        // 판매 가격 파싱
        Long salePriceLong = null;
        if (salePrice != null && !salePrice.isBlank()) {
            try {
                salePriceLong = Long.parseLong(salePrice.trim());
            } catch (NumberFormatException e) {
                log.warn("[ADMIN] 판매 가격 파싱 실패: {}", salePrice);
            }
        }

        // Rarity 파싱
        Rarity rarityEnum = null;
        if (rarity != null && !rarity.isBlank()) {
            try {
                rarityEnum = Rarity.valueOf(rarity.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("[ADMIN] Rarity 파싱 실패: {}", rarity);
            }
        }

        // 카드 정보 업데이트
        card.updateCard(
                name,
                setName,
                number,
                rarityEnum,
                imageUrl,
                uploadedImageUrl != null ? uploadedImageUrl : card.getUploadedImageUrl(),
                salePriceLong
        );

        cardRepository.save(card);
        log.info("[ADMIN] 카드 수정 완료 - Card ID: {}", id);

        redirectAttributes.addFlashAttribute("message", "카드 정보가 수정되었습니다.");
        return "redirect:/admin/cards/list";
    }

    // 7. 카드 삭제 처리
    @PostMapping("/cards/delete/{id}")
    @Transactional
    public String deleteCard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Card card = cardRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카드입니다. ID=" + id));

            // 연관된 데이터 먼저 삭제 (외래키 제약조건 해결)
            // 1. MarketPrice 삭제
            marketPriceRepository.findByCard(card).ifPresent(marketPriceRepository::delete);
            log.info("[ADMIN] MarketPrice 삭제 완료 - Card ID: {}", id);

            // 2. PriceHistory 삭제
            priceHistoryRepository.deleteAll(priceHistoryRepository.findAllByCardOrderByRecordedAtAsc(card));
            log.info("[ADMIN] PriceHistory 삭제 완료 - Card ID: {}", id);

            // 3. UserCard 삭제
            userCardRepository.deleteAll(userCardRepository.findByCard(card));
            log.info("[ADMIN] UserCard 삭제 완료 - Card ID: {}", id);

            // 4. Card 삭제
            cardRepository.delete(card);
            log.info("[ADMIN] 카드 삭제 완료 - Card ID: {}", id);

            redirectAttributes.addFlashAttribute("message", "카드가 삭제되었습니다.");
        } catch (Exception e) {
            log.error("[ADMIN] 카드 삭제 실패 - Card ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "카드 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }

        return "redirect:/admin/cards/list";
    }
}
