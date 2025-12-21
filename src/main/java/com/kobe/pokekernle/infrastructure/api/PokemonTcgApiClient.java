package com.kobe.pokekernle.infrastructure.api;

import com.kobe.pokekernle.domain.card.dto.external.PokemonTcgApiResponse;
import com.kobe.pokekernle.domain.card.service.CardDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * packageName    : com.kobe.pokekernle.infrastructure.api
 * fileName       : PokemonTcgApiClient
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */

@Slf4j
@Component
public class PokemonTcgApiClient implements CardDataProvider {

    // 환경 변수를 활용하여 bootRun하면서 API Key 값 입력, 없으면 기본값
    @Value("${pokemontcg.api-key}")
    private String apiKey;

    private final RestClient restClient;

    /**
     * [생성자]
     * RestClient.Builder를 주입받아 타임아웃 설정을 추가한 뒤 RestClient를 생성합니다.
     * @RequiredArgsConstructor를 뺏으므로, 이 생성자가 유일한 생성자가 되어 Spring이 자동으로 사용합니다.
     *
     * @param builder
     */
    @Autowired
    public PokemonTcgApiClient(RestClient.Builder builder) {
        // 1. 타임아웃 설정(연결 10초, 읽기 60초)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(100000);

        // 2. Base URL 설정
        this.restClient = builder
                .baseUrl("https://api.pokemontcg.io/v2") // 기본 주소
                .requestFactory(factory)
                .build();
    }

    @Override
    public PokemonTcgApiResponse fetchCardsBySet(String query) {
        log.info("[API CLIENT] PokemonTCG.io 요청 시작. Query: {}", query);

        // 상대 경로(/cards)를 사용하여 URL 충돌 및 인코딩 문제 원천 차단
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cards")                     // baseUrl 뒤에 붙음 -> https://api.pokemontcg.io/v2/cards
                        .queryParam("q", query)
                        .queryParam("pageSize", 5)
                        .build())
                .header("X-Api-Key", apiKey)
                .retrieve()
                .body(PokemonTcgApiResponse.class);
    }
}
