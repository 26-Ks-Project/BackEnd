# Pull Request

## Title
[Feat/Refactor] 특정 날짜 센서 히스토리 API 구현 및 트랜잭션 분리 기반 일일 퀘스트 할당 스케줄러 개선

---

## Description
스마트 팜 플랫폼의 안정성과 성능을 고도화하기 위해 아래 두 가지 핵심 기능을 개발하고 리팩토링을 완료했습니다.
1. 특정 날짜 센서 히스토리 조회 API 구현: 프론트엔드가 날짜 단위(YYYY-MM-DD)로 상세 센서 정보를 일괄 조회할 수 있는 엔드포인트를 제공합니다.
2. 일일 퀘스트 할당 트랜잭션 안전성 확보: 대용량 유저 처리 시 발생할 수 있는 메모리 부하와 트랜잭션 오염(UnexpectedRollbackException) 문제를 방지하기 위해 배정 로직을 페이징 처리하고, 개별 유저별 트랜잭션을 분리(REQUIRES_NEW)했습니다.

---

## Key Changes

### 1. 특정 날짜 센서 히스토리 조회 API
*   엔드포인트 추가:
    *   DeviceController에 GET /api/v1/devices/{deviceId}/{day}/history 엔드포인트를 신설했습니다.
    *   경로 변수로 전달받은 day를 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate를 통해 타입 안정성을 유지한 채 파싱합니다.
*   DTO 설계:
    *   조회된 상세 로그 목록과 검색 날짜 정보를 함께 담아 응답하는 SensorHistoryResponse DTO를 정의했습니다.
*   비즈니스 로직 및 계층 분리:
    *   SensorLogService에 getSensorLogsByDay를 추가하여, 지정 날짜의 00:00:00 ~ 23:59:59.999999 범위 내 데이터를 Between 조건으로 안전하게 슬라이싱합니다.
    *   [아키텍처 가이드라인 준수] 기존에 컨트롤러(DeviceController)가 레포지토리(SensorLogRepository)를 직접 호출하던 레거시 의존성을 전면 제거하고, 모든 조회 메서드를 SensorLogService를 거치도록 통합 리팩토링을 수행했습니다.

### 2. 트랜잭션 격리(REQUIRES_NEW)를 적용한 일일 퀘스트 할당 로직 고도화
*   트랜잭션 오염 방지 및 전파 레벨 튜닝:
    *   QuestScheduler에 존재하던 무거운 전체 DB 트랜잭션 및 DB 쓰기 로직을 전면 제거하고 퀘스트 서비스로 위임했습니다.
    *   개별 유저에 퀘스트를 할당하는 쓰기 연산을 수행할 때, 독립된 트랜잭션 경계를 수립하기 위해 신규 빈인 QuestAssignExecutor를 생성하여 @Transactional(propagation = Propagation.REQUIRES_NEW)를 부여했습니다.
    *   이를 통해 특정 유저에 대한 배정이 DB Unique Constraint 등의 에러로 인해 실패(Rollback)하더라도, 다른 유저의 할당 연산 및 전체 배치 트랜잭션이 영향을 받지 않도록 격리했습니다.
*   대규모 유저 처리 페이징 도입:
    *   QuestService의 processDailyQuestAssignment 내부에서 userRepository.findAll(PageRequest.of(page, pageSize)) 기반의 페이징(Chunk size = 100)을 적용하여 전체 유저를 메모리에 일괄 로딩하지 않고 나누어 처리하도록 개선했습니다.
*   스케줄러 설정 복구:
    *   QuestScheduler의 실행 주기를 매일 자정(0 0 0 * * *) 스케줄로 복구하고 서울 시간대(zone = "Asia/Seoul")를 보장했습니다.

### 3. 단위 테스트 추가
*   SensorLogServiceTest에 특정 날짜 센서 데이터의 조회 범위를 Between 조건으로 변환하여 목킹 호출하는 단위 테스트를 추가하고 검증을 완료했습니다.

---

## Verification and Test Results
*   단위 테스트 구동 결과:
    *   ./gradlew test --tests com.example.smart_farm.domain.device.service.SensorLogServiceTest -> BUILD SUCCESSFUL 통과
*   수동 호출 검증 예시:
    *   GET /api/v1/devices/{deviceId}/{day}/history -> JSON 응답 포맷 검증 및 예외 부재 완료.
