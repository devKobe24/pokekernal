package com.kobe.pokekernle.infrastructure.api;

import com.kobe.pokekernle.domain.card.dto.external.PokemonTcgApiResponse;
import com.kobe.pokekernle.domain.card.service.CardDataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
        // 1. 타임아웃 설정(연결 10초, 읽기 90초) - 복잡한 쿼리 처리 시 충분한 시간 확보
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000);
        factory.setReadTimeout(90000); // 타임아웃 방지를 위해 90초로 증가

        // 2. Base URL 설정
        this.restClient = builder
                .baseUrl("https://api.pokemontcg.io/v2") // 기본 주소
                .requestFactory(factory)
                .build();
    }

    /**
     * 포켓몬 이름, 세트 ID, 카드 번호를 조합하여 카드 데이터를 가져옵니다.
     * 재시도 로직이 포함되어 있습니다.
     * @param name 포켓몬 이름 (예: "Pikachu", null 가능)
     * @param setId 세트 ID (예: "sv3pt5", null 가능)
     * @param number 카드 번호 (예: "175", null 가능)
     * @param page 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 항목 수 (최대 250)
     * @return API 응답 DTO
     */
    public PokemonTcgApiResponse fetchCardsBySet(String name, String setId, String number, int page, int pageSize) {
        String query = buildQuery(name, setId, number);
        return fetchCardsBySetWithRetry(query, page, pageSize, 5);
    }

    /**
     * 포켓몬 이름과 세트 ID를 조합하여 카드 데이터를 가져옵니다.
     * 재시도 로직이 포함되어 있습니다.
     * @param name 포켓몬 이름 (예: "Pikachu", null 가능)
     * @param setId 세트 ID (예: "sv3pt5", null 가능)
     * @param page 페이지 번호 (1부터 시작)
     * @param pageSize 페이지당 항목 수 (최대 250)
     * @return API 응답 DTO
     */
    public PokemonTcgApiResponse fetchCardsBySet(String name, String setId, int page, int pageSize) {
        return fetchCardsBySet(name, setId, null, page, pageSize);
    }

    /**
     * 인터페이스 구현을 위한 메서드 (내부적으로 이름, 세트 ID, 번호를 받는 메서드로 변환)
     * @param query 검색 쿼리 (예: "name:\"Pikachu\" number:175 set.id:sv3pt5")
     * @return API 응답 DTO
     */
    @Override
    public PokemonTcgApiResponse fetchCardsBySet(String query) {
        // 쿼리 문자열을 파싱하여 이름, 세트 ID, 번호 추출
        String name = extractNameFromQuery(query);
        String setId = extractSetIdFromQuery(query);
        String number = extractNumberFromQuery(query);
        return fetchCardsBySet(name, setId, number, 1, 250);
    }

    /**
     * 포켓몬 이름, 세트 ID, 카드 번호를 조합하여 검색 쿼리를 생성합니다.
     * Pokemon TCG API 문서 참고: https://docs.pokemontcg.io/api-reference/cards/search-cards
     * 
     * 쿼리 문법:
     * - name:charizard (단순 키워드)
     * - name:"venusaur v" (공백이 있으면 따옴표 필요)
     * - number:175 (카드 번호)
     * - set.id:sv3pt5 (중첩 필드 검색)
     * - 여러 조건은 공백으로 AND 연결
     * 
     * @param name 포켓몬 이름 (null 가능)
     * @param setId 세트 ID (null 가능)
     * @param number 카드 번호 (null 가능)
     * @return 조합된 검색 쿼리
     */
    /**
     * 포켓몬 이름, 세트 ID, 카드 번호를 조합하여 검색 쿼리를 생성합니다.
     * Pokemon TCG API 문서 참고: https://docs.pokemontcg.io/api-reference/cards/search-cards
     * 
     * 최적화 전략:
     * 1. set.id와 number가 모두 있으면 name 제외 (쿼리 복잡도 감소)
     * 2. 인덱싱이 잘 된 필드 우선 사용 (set.id > number > name)
     * 3. 단순 쿼리로 타임아웃 방지
     * 
     * @param name 포켓몬 이름 (null 가능)
     * @param setId 세트 ID (null 가능)
     * @param number 카드 번호 (null 가능)
     * @return 조합된 검색 쿼리
     */
    private String buildQuery(String name, String setId, String number) {
        StringBuilder queryBuilder = new StringBuilder();
        
        boolean hasSetId = setId != null && !setId.isBlank();
        boolean hasNumber = number != null && !number.isBlank();
        boolean hasName = name != null && !name.isBlank();
        
        // 최적화: set.id와 number가 모두 있으면 name 제외 (쿼리 복잡도 감소)
        // set.id와 number만으로도 특정 카드를 정확히 찾을 수 있음
        boolean shouldExcludeName = hasSetId && hasNumber;

        // 1. 세트 ID (가장 빠른 인덱싱, 최우선)
        if (hasSetId) {
            queryBuilder.append("set.id:").append(setId.trim());
        }

        // 2. 카드 번호 (세트 내에서 빠른 검색)
        if (hasNumber) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" ");
            }
            queryBuilder.append("number:").append(number.trim());
        }

        // 3. 포켓몬 이름 (set.id와 number가 모두 있으면 제외하여 쿼리 단순화)
        if (hasName && !shouldExcludeName) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append(" ");
            }
            String trimmedName = name.trim();
            // 공백이 있거나 특수문자가 있으면 따옴표로 감싸기 (구문 검색)
            if (trimmedName.contains(" ") || trimmedName.contains("-")) {
                queryBuilder.append("name:\"").append(trimmedName).append("\"");
            } else {
                // 단순 키워드 매칭 (따옴표 없이)
                queryBuilder.append("name:").append(trimmedName);
            }
        }

        String query = queryBuilder.toString().trim();
        if (query.isEmpty()) {
            throw new IllegalArgumentException("포켓몬 이름, 카드 번호 또는 세트 ID 중 최소한 하나는 입력해야 합니다.");
        }

        log.debug("[QUERY BUILDER] 생성된 쿼리: {} (name 제외 여부: {})", query, shouldExcludeName);
        return query;
    }

    /**
     * 쿼리 문자열에서 포켓몬 이름을 추출합니다.
     * Pokemon TCG API 문서 참고: https://docs.pokemontcg.io/api-reference/cards/search-cards
     * 
     * 지원 형식:
     * - name:charizard
     * - name:"venusaur v"
     * 
     * @param query 검색 쿼리 (예: "name:Pikachu set.id:sv3pt5" 또는 "name:\"Pikachu V\" set.id:sv3pt5")
     * @return 포켓몬 이름 (없으면 null)
     */
    private String extractNameFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        
        // name:"..." 패턴 찾기 (구문 검색)
        int quotedNameIndex = query.indexOf("name:\"");
        if (quotedNameIndex != -1) {
            int startIndex = quotedNameIndex + 6; // "name:\"" 길이
            int endIndex = query.indexOf("\"", startIndex);
            if (endIndex != -1) {
                return query.substring(startIndex, endIndex);
            }
        }
        
        // name:... 패턴 찾기 (단순 키워드)
        int nameIndex = query.indexOf("name:");
        if (nameIndex != -1) {
            int startIndex = nameIndex + 5; // "name:" 길이
            // 공백이나 다른 필드 시작 전까지 추출
            int endIndex = query.indexOf(" ", startIndex);
            if (endIndex == -1) {
                endIndex = query.length();
            }
            String name = query.substring(startIndex, endIndex).trim();
            // 따옴표 제거 (혹시 있을 경우)
            if (name.startsWith("\"") && name.endsWith("\"")) {
                name = name.substring(1, name.length() - 1);
            }
            return name.isEmpty() ? null : name;
        }
        
        return null;
    }

    /**
     * 쿼리 문자열에서 세트 ID를 추출합니다.
     * @param query 검색 쿼리 (예: "name:\"Pikachu\" set.id:sv3pt5")
     * @return 세트 ID (없으면 null)
     */
    private String extractSetIdFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        
        // set.id:... 패턴 찾기
        int setIdIndex = query.indexOf("set.id:");
        if (setIdIndex == -1) {
            return null;
        }
        
        int startIndex = setIdIndex + 7; // "set.id:" 길이
        // 공백이나 문자열 끝까지 추출
        int endIndex = query.indexOf(" ", startIndex);
        if (endIndex == -1) {
            endIndex = query.length();
        }
        
        return query.substring(startIndex, endIndex).trim();
    }

    /**
     * 쿼리 문자열에서 카드 번호를 추출합니다.
     * Pokemon TCG API 문서 참고: https://docs.pokemontcg.io/api-reference/cards/search-cards
     * 
     * @param query 검색 쿼리 (예: "name:\"Pikachu\" number:175 set.id:sv3pt5")
     * @return 카드 번호 (없으면 null)
     */
    private String extractNumberFromQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        
        // number:... 패턴 찾기
        int numberIndex = query.indexOf("number:");
        if (numberIndex == -1) {
            return null;
        }
        
        int startIndex = numberIndex + 7; // "number:" 길이
        // 공백이나 문자열 끝까지 추출
        int endIndex = query.indexOf(" ", startIndex);
        if (endIndex == -1) {
            endIndex = query.length();
        }
        
        return query.substring(startIndex, endIndex).trim();
    }

    /**
     * 재시도 로직이 포함된 카드 데이터 조회
     * @param query 검색 쿼리
     * @param page 페이지 번호
     * @param pageSize 페이지당 항목 수
     * @param maxRetries 최대 재시도 횟수
     * @return API 응답 DTO
     */
    private PokemonTcgApiResponse fetchCardsBySetWithRetry(String query, int page, int pageSize, int maxRetries) {
        int attempt = 0;
        long baseDelayMs = 5000; // 기본 딜레이 5초 (서버 부하 감소를 위해 증가)

        while (attempt < maxRetries) {
            try {
                log.info("[API CLIENT] PokemonTCG.io 요청 시작. Query: {}, Page: {}, PageSize: {}, 시도: {}/{}", 
                        query, page, pageSize, attempt + 1, maxRetries);

                // 상대 경로(/cards)를 사용하여 URL 충돌 및 인코딩 문제 원천 차단
                // select 파라미터 제거: 타임아웃 방지를 위해 전체 필드 요청 (API가 더 빠르게 응답)
                // 필요한 필드는 DTO에서 @JsonIgnoreProperties로 무시됨
                PokemonTcgApiResponse response = restClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/cards")                     // baseUrl 뒤에 붙음 -> https://api.pokemontcg.io/v2/cards
                                .queryParam("q", query)
                                .queryParam("page", page)
                                .queryParam("pageSize", Math.min(pageSize, 250)) // API 최대값 250
                                // select 파라미터 제거: 타임아웃 방지를 위해 전체 필드 요청
                                .build())
                        .header("X-Api-Key", apiKey)
                        .retrieve()
                        .body(PokemonTcgApiResponse.class);

                log.info("[API CLIENT] 요청 성공. Query: {}, Page: {}", query, page);
                return response;

            } catch (HttpServerErrorException e) {
                // 504 Gateway Timeout 또는 5xx 서버 오류 처리
                if (e.getStatusCode() == HttpStatus.GATEWAY_TIMEOUT || 
                    (e.getStatusCode().is5xxServerError() && attempt < maxRetries - 1)) {
                    
                    attempt++;
                    long delayMs = baseDelayMs * (long) Math.pow(2, attempt - 1); // Exponential backoff
                    
                    log.warn("[API CLIENT] 서버 오류 발생 ({}). {}ms 후 재시도 {}/{} - Query: {}, Page: {}", 
                            e.getStatusCode(), delayMs, attempt, maxRetries, query, page);
                    
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 중 인터럽트 발생", ie);
                    }
                    continue; // 재시도
                } else {
                    // 재시도 불가능한 경우 또는 마지막 시도
                    log.error("[API CLIENT] API 요청 실패 (재시도 불가). Query: {}, Page: {}, Status: {}", 
                            query, page, e.getStatusCode(), e);
                    throw e;
                }
            } catch (RestClientException e) {
                // 네트워크 오류 등 기타 RestClient 예외
                if (attempt < maxRetries - 1) {
                    attempt++;
                    long delayMs = baseDelayMs * (long) Math.pow(2, attempt - 1);
                    
                    log.warn("[API CLIENT] 네트워크 오류 발생. {}ms 후 재시도 {}/{} - Query: {}, Page: {}", 
                            delayMs, attempt, maxRetries, query, page, e);
                    
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("재시도 중 인터럽트 발생", ie);
                    }
                    continue; // 재시도
                } else {
                    log.error("[API CLIENT] API 요청 실패 (재시도 불가). Query: {}, Page: {}", 
                            query, page, e);
                    throw e;
                }
            }
        }

        throw new RuntimeException("최대 재시도 횟수 초과. Query: " + query + ", Page: " + page);
    }
}
