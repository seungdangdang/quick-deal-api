<p align="center"><img alt="quick-deal-logo(white)" src="https://github.com/user-attachments/assets/4db59def-1ea0-44ab-b055-457439062c0d">

# 1. 퀵딜
- 핫한 상품을 온라인 할인 판매하는 웹사이트
<br>

# 2. 프로젝트 목표 및 설정 사항
### 2.1. 목표
- 동시간대 많은 트래픽을 안정적이고 순차적인 요청 처리 
- 대기 중인 유저에게 대기 정보 준실시간 공유를 통한 대기 UX 향상
- 타당한 근거에 따른 기술 사용
- 성능 테스트를 통한 한계 확인 + 성능 개선
- 객체 지향적인 설계

### 2.2. 설정
- 유저는 1회 1개의 상품만 주문할 수 있습니다.

<br>

# 3. 프로젝트 중점 사항

## 3.1. 대기열 시스템
유저의 동시 주문 요청으로 인한 DB 부하를 방지하기 위해 결제 페이지에 접근할 수 있는 유저 수를 제한하여 처리하는 시스템을 구축했습니다.

### 3.1.1. 주문 플로우 <br>
유저는 주문 후 대기번호가 담긴 토큰을 발급받게 되고, 해당 토큰을 가지고 주기적인 폴링으로 결제 페이지에 접근할 수 있는 여부를 확인합니다. <br>
![order-flow-chart](https://github.com/user-attachments/assets/821e1eb7-ba85-46a7-b4e6-d3973c7b755f)

### 3.1.2. 메시지 큐 <br>
주문 요청시 producer가 카프카에 레코드를 쌓게 됩니다. <br>
consumer는 카프카에 쌓인 레코드를 주기적으로 가져오며, 레코드 전처리를 위해 Redis와 MySQL에서 데이터를 가져와 확인합니다. <br>
결제 페이지에 접근가능한 유저가 있고, 재고가 있다면 가능한 수 만큼 요청 로직을 처리하고 레코드를 컨슘합니다. <br>
Redis와 MySQL 작업의 트랜잭션을 보장해야 하기 때문에 보상 트랜잭션이 구현되어 있습니다.
![message queue system](https://github.com/user-attachments/assets/dbb9f0c4-333b-4794-ac1d-763a0287474e)


## 3.2. 성능 개선
총 105 차례의 nGrinder 테스트를 통해 한계를 테스트하고 환경에 변화를 주며 성능 개선을 진행했습니다.

TPS 그래프의 비대칭성과 결제 페이지 접근 요청이 해소되지 않는 문제를 보고 병목을 의심하게 되었습니다. <br>

1. Prometheus + Grafana 도입 [observability 향상1] <br>
2. (Grafana) Kafka 대시보드 [consume 로직 문제 의심] <br>
3. 커스텀 매트릭 수집을 위해 micrometer 적용 [observability 향상2] <br>
4. (Grafana) 커스텀 대시보드 [consume 로직 문제 확신]

 
 Kafka consume 로직에서 성능 저하가 있음을 발견하였고 개선한 결과 동일한 가상 유저 환경에서 처리율을 56배 향상시킬 수 있었습니다. <br>


[ 상황 별 성능 ]
|        | 1PC    | 3PC    | 3PC + 성능 개선 |
|--------|--------|--------|----------------|
| 가상 유저 수  | ~300   | 1,200  | 1,170          |
| TPS    | 45.3   | 8.4   | 323.7          |
| Peak TPS | 158.5  | 180.0  | 1,501.5        |
| 주문 건 / 1분 | 20.8   | 0.91   | 128.8          |


| 성능 개선 전 (극초반 이후 급격히 TPS가 다운됨)      | 성능 개선 후 (전반적으로 대칭 형태 및 TPS 향상)                    |
|------------|--------------------------------------------------------------------------------------------------------|
<img width="868" alt="개선 전 TPS" src="https://github.com/user-attachments/assets/5c2227d8-8622-424e-a0a0-450723bacbb0">  | <img width="867" alt="개선 후 TPS 그래프" src="https://github.com/user-attachments/assets/c685569c-cf7a-485a-b106-cbc58a8460df"> |

[ 동일한 가상 유저 수로 비교 - 성능 개선 before / after ]
|        | Before    | After |  |
|--------|--------|----------------|-----| 
| 가상 유저 수  | 1,000   |  1,000          |
| TPS    | 20.8   |  936.4         | 45배 🔼  |
| Peak TPS | 108 | 1,500.5     | 13배 🔼  |
| 주문 건 / 1분 | 5.8  | 329.68          |  56배 🔼 |





## 3.3. 내 앞 대기 인원 <br>
유저가 준실시간으로 대기 정보를 공유 받으면서 내 앞 대기 인원에 대해 파악할 수 있도록 정보를 제공했습니다.
<img alt="주문-대기" width="621" src="https://github.com/user-attachments/assets/f241d592-a238-40fb-a3f3-b18375d88c2e">



## 3.4. Modular Monolithic Architecture 도입
추가적인 학습 + Spring Webflux에 대한 러닝 커브 요인으로 Spring MVC 를 사용했습니다.
서비스 분리를 위해 MSA도 고려 사항이었지만 복잡성을 고려하여 유사한 형태인 해당 아키텍처를 선택하게 되었습니다.

현재는 `core` 모듈에서 전체 빌드를 담당하고 있지만, 모듈 간 결합을 최소화하고 인터페이스 기반 설계를 도입했기에 향후 모듈 별 빌드로 변경하고 서비스 간 독립성을 더 최적화한다면 MSA를 고려할 수 있습니다. <br>
| 모듈        | 상세 설명                                                      |
|-----------|-------------------------------------------------------------------|
| core      | 애플리케이션 빌드 및 설정 / yaml / DB 마이그레이션 파일             |
| common    | 커스텀 예외 / 필터 & 인터셉터 / product, stockCache 서비스 인터페이스 |
| auth      | 인증 관련 API                                                    |
| docker    | docker-compose 및 설정                                            |
| product   | 상품 관련 API / 상품 리스트 & 상세 정보 관리                       |
| purchase  | 구매 관련 API / Kafka, Redis 기반 대기열 서비스 관리               |
| scheduler | 스케줄러 관련 API / 재고 캐싱 시스템 & Redis 상태 로그 & 만료된 결제 페이지 액세스 데이터 제거   |



## 3.5. 상품 별 재고 캐싱 시스템

재고 확인이 필요한 시점은 2가지입니다.

(1) 폴링 시 제품 품절 여부 확인 <br>
(2) 결제 진행 시 제품 품절 여부 확인

폴링의 경우 동시에 여러 사람이 짧은 주기로 매번 재고를 확인하면 DB의 부하가 우려되어 재고 캐싱 데이터를 제공하고자 했습니다. <br>

스케줄러가 주기적으로 실제 재고를 자바 내장 클래스인 ConcurrentHashMap에 캐싱하게끔 구현헀습니다. <br>
NoSQL을 고려하기도 하였으나 전체 서비스에서 DB를 추가할만큼의 영향이 크지 않기에 Map을 사용하게 되었습니다.

<img alt="stock scheduler" width="621" src="https://github.com/user-attachments/assets/8255a308-5c16-40ab-a17c-43334fc0641e"> <br>



## 3.6. 주문 가능 시간이 만료된 유저 삭제 시스템

결제 페이지에서 주문이 가능한 시간이 정해져 있으므로, 레디스의 결제 페이지 접근 유저 저장소에서 <br>

<img alt="remove expired orders" width="621" src="https://github.com/user-attachments/assets/8027ffef-bb8c-4ad1-acd8-dc3ed36ec0ad">



# 4. 기술 스택
## 4.1. 백엔드
| **분류**          | **기술 스택**                           |
|-------------------|-----------------------------------------|
| **언어**          | ![Java](https://img.shields.io/badge/Java-17-blue) ![Lua](https://img.shields.io/badge/Lua-script-lightblue)                |
| **데이터베이스**  | ![MySQL](https://img.shields.io/badge/MySQL-8.4.2-blue) ![Redis](https://img.shields.io/badge/Redis-7.4.0-red) ![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-3.3.2-green) |
| **프레임워크**    | ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.2-brightgreen)         |
| **도구 및 플랫폼**| ![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-IDE-orange) ![nGrinder](https://img.shields.io/badge/nGrinder-testing-blue) ![Kafka](https://img.shields.io/badge/Kafka-3.8.0-lightgrey) ![Docker](https://img.shields.io/badge/Docker-container-blue) ![Gradle](https://img.shields.io/badge/Gradle-buildtool-brightgreen) ![Grafana](https://img.shields.io/badge/Grafana-11.2.0-yellow) ![Prometheus](https://img.shields.io/badge/Prometheus-2.54.1-red) |
| **기타**          | ![Flyway](https://img.shields.io/badge/Flyway-10.10.0-red) ![JWT](https://img.shields.io/badge/JWT-security-yellow)                     |

## 4.2. 프론트엔드
| **분류**          | **기술 스택**                           |
|-------------------|-----------------------------------------|
| **언어**          | ![JavaScript](https://img.shields.io/badge/JavaScript-ES6-yellow) ![HTML](https://img.shields.io/badge/HTML5-E34F26?logo=html5&logoColor=white) ![CSS](https://img.shields.io/badge/CSS3-1572B6?logo=css3&logoColor=white) |
| **프레임워크**    | ![React](https://img.shields.io/badge/React-18.3.1-blue)         |




## 5. ERD 다이어그램
![ERD](https://github.com/user-attachments/assets/e94b9229-acae-4d57-bb43-0140e44a600c)

### 6. 전체 아키텍처
<img alt="image" src="https://github.com/user-attachments/assets/56b129af-a0b6-44e8-8ba2-11d595e30878">

# 7. 시연 영상
### 7.1. 메인페이지
✔️ 페이지네이션 - 커서 페이징으로 구현하여 스크롤이 바닥에 닿음을 인지하면 다음 상품 리스트를 가져옵니다. <br>
✔️ 로그인 - 랜덤 UUID를 발급하여 로컬 스트리지에 저장하여 유저 구분에 사용됩니다. <br>

https://github.com/user-attachments/assets/75c6f8a9-de05-43a3-971c-49090a3bd13f



### 7.2. 상세페이지 (대기 있을 때)
✔️ 비회원 감지 - 비로그인 상태에서 주문 시도 시 로그인을 유도합니다.
✔️ 주문 시 대기 O - 대기 정보를 준실시간으로 공유 받으며 결제 페이지에 진입할 때까지 폴링을 시도합니다. <br>

https://github.com/user-attachments/assets/aaf650e7-d437-4dd0-9d05-89a9755101e8



### 7.3. 결제페이지
✔️ 결제 진행 <br>

https://github.com/user-attachments/assets/6da0fa46-3327-48a2-a402-d8ad6e70a9b8

