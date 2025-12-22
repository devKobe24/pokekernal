package com.kobe.pokekernle.domain.collection.controller;

import com.kobe.pokekernle.domain.collection.entity.CollectionStatus;
import com.kobe.pokekernle.domain.collection.request.AddCollectionRequest;
import com.kobe.pokekernle.domain.collection.service.CollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.controller
 * fileName       : CollectionApiController
 * author         : kobe
 * date           : 2025. 12. 22.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 22.        kobe       최초 생성
 */
@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
public class CollectionApiController {

    private final CollectionService collectionService;

    @PostMapping
    public ResponseEntity<String> addCard(@RequestBody AddCollectionRequest request) {
        collectionService.addCardToCollection(request);
        return ResponseEntity.ok("성공적으로 컬렉션에 추가되었습니다!");
    }
}
