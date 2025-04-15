# 🌡️IoT-TempLog💧
![iot 현재](https://github.com/user-attachments/assets/f62e2632-126d-4395-b2c6-7d7818b8f9a2)

<br/><br/>

## 소개 및 개요
- 프로젝트 기간 : 2025.03.14 ~ 2025.03.21
- 인원 : 백엔드 1명


### [프로젝트 개요]:
이 프로젝트는 가스 벨브를 잠그지 않고 외출한 경험에서 아이디어를 얻어 시작되었습니다. 당시 불안한 마음에 집의 온도와 습도를 실시간으로 확인할 수 있는 시스템을 만들기로 결심하게 되었습니다. 이를 바탕으로 온도 및 습도 모니터링 시스템을 설계하여, 위험 상황을 미리 감지하고 알림을 받을 수 있는 기능을 제공하고자 했습니다.

### [프로젝트 설명]:
- 본 시스템은 ESP32 DHT22 센서를 사용하여 실시간 온도와 습도를 측정하고, 이를 MQTT Mosquitto 브로커를 통해 웹 서버로 전송합니다. 사용자는 설정된 온도 차이나 습도 기준을 초과할 경우, 실시간으로 이메일 알림을 받을 수 있으며, 이메일 알림 기능은 사용자가 선택적으로 활성화할 수 있어 불필요한 알림을 방지할 수 있습니다. 특히, 온도 차이가 일정 범위를 초과할 때 발생하는 알림은 위험 상황을 사전에 감지하여 사용자가 빠르게 대응할 수 있도록 돕습니다.

- 또한, 이 프로젝트는 카카오 소셜 로그인을 지원하여 사용자가 친구들과 쉽게 시스템을 공유하고 사용할 수 있도록 하였으며, 보안 측면에서는 JWT 인증과 HttpOnly 쿠키를 활용하여 웹 보안을 강화했습니다. 만약 기기가 연결 끊어짐 현상이 발생하면, Redis를 사용하여 구독 상태를 관리하고, 기기가 재연결될 경우 자동으로 구독을 복구할 수 있도록 설계되었습니다.

- 데이터 관리 측면에서 실시간 온도 및 습도 정보를 Redis에 캐시하여 빠르게 제공하고, 2시간마다 평균값을 DB에 저장하는 방식으로 효율적인 데이터 처리와 저장을 보장합니다. 이를 통해 사용자는 중요한 데이터가 빠르고 정확하게 제공되며, 시스템의 성능이 최적화됩니다.


## 1. 팀원 소개

| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;임홍현😺&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; |
| :---------------------: |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[@limhhyeon](https://github.com/limhhyeon) |
| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;🖥️ Backend |


<br/>

## 2. 기술 스택
### [사용 기술]
#### **🛠️ Backend**  
![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) ![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white) ![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)  
- **Spring MVC + REST API** : RESTful API 개발  
- **Spring Security** : 인증 및 권한 관리  
- **SLF4J** : 애플리케이션 로깅  
- **Spring Cache** : DB 리소스를 줄이기 위한 캐시 관리  
- **Spring Schedule** : 스케줄 관리
- **WebSocket + STOMP** : 실시간 알림 및 채팅 

#### **💻 Database & Cache**    
 ![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)  
- **비관적 락(Pessimistic Lock)** : 동시성 제어

#### **☁️ DevOps & Deployment**    
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)  
- **AWS EC2** : 클라우드 서버  
- **AWS RDS(MariaDB)** : 클라우드 DB 관리
- **AWS S3** : 파일, 이미지 관리  

#### **📝 Collaboration Tools**  
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)  ![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white) ![Notion](https://img.shields.io/badge/Notion-%23000000.svg?style=for-the-badge&logo=notion&logoColor=white) ![Figma](https://img.shields.io/badge/figma-%23F24E1E.svg?style=for-the-badge&logo=figma&logoColor=white) ![Discord](https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white) 

### [커밋 컨벤션]
![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)

```java
feat: 새로운 기능을 추가했을 때
fix: 버그를 수정했을 때
docs: 문서 수정
refactor: 코드 개선했을 때
perf: 성능 최적화할 때
test: 테스트 코드 추가, 수정
build: 빌드 시스템이나 외부 의존성 변경할 때
ci: CI 설정 수정
```
<br/>

## 3. System Architecture & ERD
### [System Architecture]
 

####broker 처리


### [ERD]
![iotErd](https://github.com/user-attachments/assets/4074d0f0-5742-4cee-a1d0-c385c4ef00bb)


<br/>

## 4. 기능 전략

#### 1. Auth
- ` 로그인 ` :  이메일(아이디), 비밀번호 입력 받아 JWT 및 refresh 토큰 발급 후, HttpOnly 쿠키에 담아서 전달
- ` 회원가입 ` : 닉네임/이메일 중복확인 후 남은 유저 정보를 입력하여 회원가입을 진행
- ` 닉네임 중복확인 ` : 닉네임을 입력 받아 DB에서 존재하는 닉네임이 있는지 확인
- ` 이메일 중복 확인 ` : 이메일을 입력 받아 DB에서 존재하는 이메일이 있는지 확인
- ` 토큰 유효 검증 ` : 토큰 유효기간이 남아있는지 확인
- ` 토큰 재발급 ` : 토큰 유효기간이 만료되었을 시 재발급
- ` 로그아웃 ` : 로그아웃 시 쿠키와 토큰 제거


<br/>


## 5. 트러블 슈팅


| 🔴 오류                           | 🔵 문제                                                                 | 🟢 해결 방법                                                               |
|---------------------------------|----------------------------------------------------------------------|--------------------------------------------------------------------------|
| `@PreDestroy에서 발생한 예외처리`  | `@PreDestroy`에서 Redis에서 값을 가져오기 전에 Redis 연결이 끊겨서 해당 값을 가져오지 못해 로그가 찍히지 않고 `@PreDestroy`가 실행되지 않음. | `@PreDestroy`가 실행되기 전에 Redis에서 먼저 값을 가져와 DB에 저장하는 로직을 `EventListener`로 처리하여, Redis와의 연결을 끊기 전에 값을 저장하도록 수정. |
| `DHT22 기기의 상태를 확인하는 방법` | 기기의 상태(켜짐/꺼짐)를 어떻게 알 수 있을지 고민하고 있었고, MQTT를 사용하여 기기의 상태를 관리하는 방법을 찾음. | MQTT Client를 사용하여 기기의 상태를 관리하고, `device/status/#` 토픽을 구독하여 기기의 상태 변경(켜짐/꺼짐)을 실시간으로 확인하고 처리하도록 함. |
| `서버 재시작 시 MQTT 구독 복원 문제` | 서버가 재시작된 후, MQTT에서 구독한 토픽들이 사라지고 다시 구독을 등록해야 하는 문제 발생. 특히, 상품 등록 시에만 구독을 하도록 설정했지만, 서버가 꺼졌다 켜지면 모든 구독 정보가 사라져 다시 등록해야 함. | 서버가 재시작될 때 활성화된 상품에 대해서만 구독을 다시 하도록 설정. 불필요한 구독을 피하기 위해 데이터베이스에서 활성화된 상품만 구독하도록 필터링하고, 페이지네이션을 통해 한 번에 너무 많은 데이터를 처리하지 않도록 수정. 1. **MQTT 연결 재시도 로직 추가**: MQTT 연결이 제대로 되지 않으면 최대 5번까지 재시도하며 연결을 시도. 연결이 실패하면 로그를 찍고 구독 초기화 실패 처리. 2. **활성화된 상품만 구독 복원**: 서버가 재시작될 때 데이터베이스에서 활성화된 상품들만 조회하여 구독을 다시 등록. 페이지네이션을 사용해 한 번에 너무 많은 데이터를 처리하지 않도록 함. `userProductRepository.findActiveMqttTopicsByActive(PageRequest.of(page, size))`를 사용해 활성화된 토픽들을 페이지 단위로 조회. |
| `Mosquitto 서버 접근 제어 문제`   | Mosquitto 서버에 누구나 접근할 수 있는 상태로 두면 보안상 문제가 발생할 수 있음. 이를 해결하기 위해 Mosquitto 서버에 접근 제어를 추가해야 한다. | Mosquitto 서버에 비밀번호를 설정하여 인증된 사용자만 접근할 수 있도록 구성. 각 클라이언트에 대한 고유한 `clientId`와 `password`를 관리하는 방식으로, 서버에 비밀번호를 몰라도 클라이언트는 파일에 저장된 정보를 통해 접근할 수 있도록 설정. |
| `IoT 온도/습도 데이터 Redis 저장 문제` | IoT 디바이스에서 온도와 습도 정보를 실시간으로 자주 받아오는 상황에서, 데이터를 매번 데이터베이스에 저장하면 DB 부하가 심할 수 있음. 이를 해결하기 위해 빠르게 데이터를 처리할 수 있는 방법이 필요함. | Redis를 활용하여 온도와 습도 데이터를 메모리에 저장한 후, 주기적인 스케줄러를 통해 데이터베이스에 한 번에 저장하는 방식으로 해결. 이를 통해 데이터베이스 부하를 줄이고, 빠른 데이터 처리를 가능하게 함. |



<br/>

## 6. Lessons Learned


### Lessons

### *Redis를 활용한 성능 최적화*  

프로젝트를 진행하면서 **웹소켓 기반 실시간 채팅**과 **데이터 조회 성능 최적화**를 위해 **Redis**를 적용하는 방법을 배웠습니다.  
이를 통해 DB 부하를 줄이고 빠른 데이터 처리를 가능하게 했습니다.  

#### 배운 점:  

##### 1. Redis의 역할  
- **메모리 기반 저장소**로, 빠른 읽기/쓰기 성능을 제공  
- **Key-Value 구조**로 데이터를 저장하며, 캐싱, 세션 관리, 실시간 데이터 저장 등에 활용  
- **웹소켓 기반 채팅**에서 DB에 직접 저장하는 대신 Redis에 저장하여 빠르게 처리하고, 일정 조건에 따라 배치 저장  

##### 2. 설정 방법  
- `채팅 메시지 저장`  
  - 채팅이 오갈 때마다 DB에 바로 저장하지 않고, Redis에 우선 저장  
  - 이후 특정 스케줄(예: 일정 시간이 지나거나 채팅 리스트 불러올 때) DB에 일괄 저장  
- `데이터 조회 최적화`  
  - 매번 DB에서 유저 데이터를 가져오는 대신, Redis에 저장해두고 빠르게 조회  
  - Redis에 데이터가 없을 경우(DB 조회 필요) → DB에서 가져온 후 Redis에 캐싱  
- `활용한 자료구조`  
  - `Hash`: 유저 정보 캐싱  
  - `List`: 채팅 메시지 저장 및 관리  

##### 3. 적용 후 개선점  
- **빠른 응답 속도** → 메모리 기반으로 작동하여 실시간 채팅에서도 지연 없이 데이터 처리 가능  
- **DB 부하 감소** → 모든 요청이 DB를 거치지 않고, Redis를 활용하여 캐싱된 데이터를 먼저 조회  
- **효율적인 데이터 관리** → 필요할 때만 DB와 동기화하여 데이터 일관성을 유지  

Redis를 도입함으로써 **웹소켓 기반 실시간 채팅**과 **데이터 조회 성능**을 최적화할 수 있었습니다.  
이를 통해 빠르고 효율적인 서비스 운영이 가능해졌습니다. 🚀  




### Learned  

- **Redis를 활용하여 성능을 최적화**하면서, 데이터 저장 전략을 보다 효율적으로 설계하는 방법을 배웠다.  




<br/>


## 7. 느낀점


