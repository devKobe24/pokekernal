package com.kobe.pokekernle.domain.collection.repository;

import com.kobe.pokekernle.domain.card.entity.Card;
import com.kobe.pokekernle.domain.card.entity.MarketPrice;
import com.kobe.pokekernle.domain.card.entity.Rarity;
import com.kobe.pokekernle.domain.card.repository.CardRepository;
import com.kobe.pokekernle.domain.card.repository.MarketPriceRepository;
import com.kobe.pokekernle.domain.collection.entity.CardCondition;
import com.kobe.pokekernle.domain.collection.entity.CollectionStatus;
import com.kobe.pokekernle.domain.collection.entity.UserCard;
import com.kobe.pokekernle.domain.user.entity.Role;
import com.kobe.pokekernle.domain.user.entity.User;
import com.kobe.pokekernle.domain.user.repository.UserRepository;
import com.kobe.pokekernle.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * packageName    : com.kobe.pokekernle.domain.collection.repository
 * fileName       : UserCardRepositoryTest
 * author         : kobe
 * date           : 2025. 12. 21.
 * description    :
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2025. 12. 21.        kobe       최초 생성
 */
@DataJpaTest // JPA 관련 빈만 로드하여 가볍게 테스트 (H2 사용)
@Import(QueryDslConfig.class) // QueryDSL 설정이 필요하다면 추가 (현재는 필수가 아님)
class UserCardRepositoryTest {

    @Autowired UserRepository userRepository;
    @Autowired CardRepository cardRepository;
    @Autowired MarketPriceRepository marketPriceRepository;
    @Autowired UserCardRepository userCardRepository;

    @Autowired EntityManager em;

    @Test
    @DisplayName("유저가 카드를 수집하면, UserCard에 정상적으로 저장되고 조회되어야 한다.")
    void saveAndFindUserCard() {
        // 1. [GIVEN] 기초 데이터 세팅 (User, Card)
        User user = User.builder()
                .email("jiwoo@pokemon.com")
                .password("1234")
                .nickname("한지우")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        Card card = Card.builder()
                .name("리자몽")
                .setName("151")
                .number("6/165")
                .rarity(Rarity.RARE)
                .imageUrl("https://github.com/devKobe24/images2/blob/main/charizard.jpeg?raw=true")
                .build();
        cardRepository.save(card);

        // 2. [When] 유저가 카드를 수집 (UserCard 생성)
        UserCard userCard = UserCard.builder()
                .user(user)
                .card(card)
                .cardCondition(CardCondition.MINT)
                .status(CollectionStatus.OWNED)
                .purchasePrice(new BigDecimal("50000.00")) // 5만원에 구매
                .memo("기차역 포켓몬카드 자판기에서 151 구매하여 뽑음")
                .build();

        UserCard savedUserCard = userCardRepository.save(userCard);

        // 영속성 컨텍스트 초기화 (DB에서 진짜로 쿼리를 날려서 가져오는지 확인하기 위함)
        em.flush();
        em.clear();

        // 3. [Then] 검증
        UserCard findUserCard = userCardRepository.findById(savedUserCard.getId())
                .orElseThrow(() -> new IllegalArgumentException("저장된 카드가 없습니다"));

        // 데이터 정합성 체크
        assertThat(findUserCard.getUser().getNickname()).isEqualTo("한지우");
        assertThat(findUserCard.getCard().getName()).isEqualTo("리자몽");
        assertThat(findUserCard.getCardCondition()).isEqualTo(CardCondition.MINT);
        assertThat(findUserCard.getPurchasePrice()).isEqualByComparingTo(new BigDecimal("50000"));

        // Auditing 기능 체크 (자동 생성 일자)
        assertThat(findUserCard.getCreatedAt()).isNotNull();
        System.out.println("생성일자: " + findUserCard.getCreatedAt());

    }
}