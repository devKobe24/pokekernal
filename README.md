# 🃏 PokeKernel - Pokemon Card Collection & Market Tracker

> Pokemon 카드 컬렉션 관리 및 시세 추적 시스템

Pokemon TCG API와 연동하여 카드 정보를 자동으로 수집하고, 시세 변동을 추적하며, 개인 컬렉션의 수익률을 실시간으로 확인할 수 있는 웹 애플리케이션입니다.

## 📋 목차

- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [프로젝트 구조](#-프로젝트-구조)
- [시작하기](#-시작하기)
- [환경 설정](#-환경-설정)
- [API 문서](#-api-문서)
- [배포](#-배포)

## ✨ 주요 기능

### 🎯 카드 관리
- **자동 카드 등록**: Pokemon TCG API 연동으로 카드 정보 자동 수집
- **수동 카드 등록**: 관리자 페이지에서 카드명, 번호, 세트 ID로 검색 및 등록
- **이미지 업로드**: 로컬 파일 또는 AWS S3를 통한 커스텀 이미지 업로드
- **카드 정보 수정**: 카드명, 세트, 번호, 희귀도, 판매 가격 등 편집

### 📊 시세 추적
- **실시간 시세 수집**: Pokemon TCG API의 CardMarket 데이터 자동 동기화
- **시세 히스토리**: 일별 가격 변동 추적 및 차트 시각화 (Chart.js)
- **통화 변환**: EUR → USD 자동 환율 적용
- **배치 작업**: 매일 새벽 4시 자동 시세 업데이트

### 💼 컬렉션 관리
- **개인 컬렉션**: 보유 카드 등록 및 상태(MINT, NEAR_MINT 등) 관리
- **구매가 추적**: 구매 가격 기록 및 현재가 대비 수익률 자동 계산
- **수익률 분석**: 총 구매액, 평가액, 수익금, 수익률 요약 대시보드
- **메모 기능**: 카드별 구매 경로, 특이사항 메모

### 🔐 보안
- **Spring Security**: 관리자 전용 페이지 접근 제어
- **프로필별 보안**: 개발 환경(H2 Console 허용) / 운영 환경(엄격한 보안) 분리
- **CSRF 보호**: API 엔드포인트 보호
- **비밀번호 암호화**: BCrypt 해시 알고리즘 적용

## 🛠 기술 스택

### Backend
- **Java 17**
- **Spring Boot 3.4.1**
- **Spring Data JPA** + **QueryDSL**
- **Spring Security 6**
- **Hibernate**
- **Lombok**

### Frontend
- **Thymeleaf**
- **HTML5 / CSS3**
- **JavaScript (ES6+)**
- **Chart.js** (시세 차트)

### Database
- **H2 Database** (개발 환경)
- **MySQL** (운영 환경)

### Infrastructure
- **AWS S3** (이미지 저장)
- **AWS CloudFront** (CDN)
- **AWS Secrets Manager** (운영 환경 설정 관리)
- **GitHub Actions** (CI/CD)

### API
- **Pokemon TCG API v2** ([docs.pokemontcg.io](https://docs.pokemontcg.io))

## 📁 프로젝트 구조

```
src/main/java/com/kobe/pokekernle/
├── domain/
│   ├── admin/              # 관리자 기능
│   │   ├── controller/     # 카드 등록/수정/삭제 컨트롤러
│   │   └── service/        # 이미지 업로드 서비스
│   ├── card/               # 카드 도메인
│   │   ├── entity/         # Card, MarketPrice, PriceHistory, Rarity
│   │   ├── repository/     # JPA Repository
│   │   ├── service/        # 카드 서비스, 시세 동기화 서비스
│   │   └── dto/            # DTO (external API, response)
│   ├── collection/         # 컬렉션 도메인
│   │   ├── entity/         # UserCard, CardCondition, CollectionStatus
│   │   ├── repository/     # JPA Repository
│   │   └── service/        # 컬렉션 서비스
│   ├── user/               # 사용자 도메인
│   │   ├── entity/         # User, Role
│   │   └── repository/     # JPA Repository
│   └── batch/              # 배치 작업
│       └── PriceUpdateBatch.java  # 일일 시세 업데이트
├── global/
│   ├── config/             # 설정 파일
│   │   └── security/       # Spring Security 설정 (dev/prod)
│   └── entity/             # BaseTimeEntity (Auditing)
└── infrastructure/
    └── api/                # Pokemon TCG API 클라이언트
```

## 🚀 시작하기

### 사전 요구사항

- **Java 17** 이상
- **Gradle 8.x**
- **Pokemon TCG API Key** ([pokemontcg.io](https://pokemontcg.io) 가입 후 발급)

### 로컬 실행

1. **저장소 클론**
```bash
git clone https://github.com/yourusername/pokekernle.git
cd pokekernle
```

2. **application-dev.yml 설정**
```yaml
# src/main/resources/application-dev.yml
pokemontcg:
  api-key: YOUR_API_KEY_HERE

admin:
  email: admin@pokekernel.com
  password: your_admin_password
  nickname: Administrator
```

3. **애플리케이션 실행**
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

4. **접속**
- 메인 페이지: http://localhost:8080
- 카드 목록: http://localhost:8080/cards
- 관리자 로그인: http://localhost:8080/admin/login
- H2 Console: http://localhost:8080/h2-console

### 관리자 계정

개발 환경에서는 애플리케이션 실행 시 자동으로 관리자 계정이 생성됩니다.

- **이메일**: `application-dev.yml`에 설정한 `admin.email`
- **비밀번호**: `application-dev.yml`에 설정한 `admin.password`

## ⚙️ 환경 설정

### 프로필 구성

#### 1. dev (개발 환경)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  
  security:
    # H2 Console 허용
    # CSRF 비활성화 (편의성)
```

**특징**:
- H2 인메모리 DB 사용
- 로컬 파일 시스템에 이미지 저장 (`/uploads/images`)
- H2 Console 접근 가능
- 보안 제약 완화

#### 2. prod (운영 환경)
```yaml
spring:
  datasource:
    url: jdbc:mysql://your-db-host:3306/pokekernel
  
  cloud:
    aws:
      s3:
        bucket: your-bucket-name
```

**특징**:
- MySQL 사용
- AWS S3에 이미지 저장
- CloudFront CDN 연동
- AWS Secrets Manager로 설정 관리
- 강화된 보안 설정 (CSRF 활성화, 엄격한 접근 제어)

### AWS 설정 (운영 환경)

#### 1. S3 버킷 생성
```bash
# 버킷 생성
aws s3 mb s3://your-bucket-name --region ap-northeast-2

# 퍼블릭 액세스 차단 해제 (선택사항)
aws s3api put-public-access-block \
  --bucket your-bucket-name \
  --public-access-block-configuration \
  "BlockPublicAcls=false,IgnorePublicAcls=false,BlockPublicPolicy=false,RestrictPublicBuckets=false"
```

#### 2. Secrets Manager 설정
```json
{
  "admin.email": "admin@example.com",
  "admin.password": "secure_password",
  "admin.nickname": "Administrator",
  "spring.datasource.url": "jdbc:mysql://host:3306/db",
  "spring.datasource.username": "username",
  "spring.datasource.password": "password"
}
```

## 📡 API 문서

### Pokemon TCG API 연동

#### 카드 검색 쿼리 예시
```java
// 1. 이름으로 검색
"name:pikachu"

// 2. 세트로 검색
"set.id:sv3pt5"

// 3. 복합 검색
"name:\"charizard\" number:6 set.id:sv3pt5"
```

#### 지원 필드
- `name`: 카드 이름
- `number`: 카드 번호
- `set.id`: 세트 ID
- `rarity`: 희귀도
- `cardmarket.prices.trendPrice`: 시세

### 내부 API 엔드포인트

#### 컬렉션 추가
```http
POST /api/collection
Content-Type: application/json

{
  "cardId": 1,
  "purchasePrice": 50000.00,
  "condition": "MINT",
  "memo": "기차역 자판기에서 구매"
}
```

## 🔄 배치 작업

### 일일 시세 업데이트
- **실행 시각**: 매일 새벽 4:00
- **대상**: Pokemon 151 세트 (`sv3pt5`)
- **작업 내용**:
  1. Pokemon TCG API에서 최신 시세 조회
  2. `MarketPrice` 테이블 업데이트
  3. `PriceHistory` 테이블에 히스토리 기록

```java
@Scheduled(cron = "0 0 4 * * *")
public void dailyPriceSyncTask() {
    cardPriceSyncService.syncLatestPrices("set.id:sv3pt5");
}
```

### 수동 데이터 수집
관리자 페이지에서 수동으로 카드 등록 시 즉시 API 호출 및 DB 저장

## 📦 배포

### GitHub Actions CI/CD

```yaml
# .github/workflows/deploy.yml
- name: Build with Gradle
  run: ./gradlew build -x test
  
- name: Deploy to AWS
  # EC2, ECS, 또는 Elastic Beanstalk 배포
```

### Docker 컨테이너

```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🧪 테스트

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests UserCardRepositoryTest
```

## 📝 컨벤션

### 커밋 메시지
- **feat**: 새로운 기능 추가
- **fix**: 버그 수정
- **refactor**: 코드 리팩토링
- **docs**: 문서 수정
- **test**: 테스트 코드 추가/수정
- **chore**: 빌드 설정, 패키지 매니저 설정

예시:
```
feat: 카드 이미지 S3 업로드 기능 구현
fix: 시세 동기화 중 타임아웃 오류 수정
refactor: CardService 레이어 구조 개선
```

## 📄 라이선스

This project is licensed under the MIT License.

## 👨‍💻 개발자

- **Kobe** - [GitHub](https://github.com/devKobe24)

---