# PLAN.md

## 목적
`PRD.md`를 기준으로 Blog Service API 서버를 실제 배포 가능한 수준으로 완성하기 위한 구현 작업 목록을 정의한다.

## 현재 상태 요약
- Spring Boot 기본 애플리케이션만 존재
- WebFlux/R2DBC/OpenAPI/S3/보안/테스트 인프라 미구현
- 도메인, API, DB 마이그레이션, 예외 처리, 관측성 모두 미구현

## 구현 원칙
- TDD 우선: 테스트 작성 후 구현
- Reactive End-to-End: Controller ~ DB/S3 모두 논블로킹
- 작은 단위로 병합: 기능 단위 PR/커밋
- 운영 관점 포함: 에러 응답 표준화, 로깅, 문서화, 설정 분리

## 작업 목록 (우선순위 순)

## 0. 프로젝트 기반 설정 (P0)
- [ ] Gradle 의존성 추가
  - Spring WebFlux
  - Spring Data R2DBC
  - R2DBC PostgreSQL Driver
  - Flyway 또는 Liquibase (스키마 관리)
  - springdoc-openapi (WebFlux 대응)
  - Validation
  - AWS SDK v2 S3 (async client)
  - 테스트: reactor-test, WebTestClient, Testcontainers(PostgreSQL), Mockk/Mockito
- [ ] 패키지 구조 정리
  - `domain`, `application`, `infrastructure`, `presentation`, `common`
- [ ] 환경별 설정 분리
  - `application.yaml`, `application-local.yaml`, `application-test.yaml`
  - DB/S3/업로드 정책(허용 MIME, 최대 용량) 설정값 외부화

완료 기준:
- 애플리케이션이 로컬에서 WebFlux + R2DBC 구성으로 기동됨
- 테스트 프로필로 기동 시 Testcontainers 연결 가능

## 1. 도메인/DB 설계 구현 (P0)
- [ ] Post 도메인 모델 구현
  - `id`, `title`, `contentMarkdown`, `authorId`, `status`, `createdAt`, `updatedAt`
  - `PostStatus` enum: `Draft`, `Published`, `Deleted`
- [ ] 입력 검증 규칙 구현
  - 제목 1~200자, 필수 필드 검증
- [ ] DB 마이그레이션 작성
  - `posts` 테이블 생성
  - 인덱스 설계: `status`, `created_at`(목록 조회 대비)
- [ ] Reactive Repository 구현
  - 단건 조회/저장/수정/삭제(소프트 삭제 포함 여부 정책화)

완료 기준:
- 마이그레이션 후 스키마가 PRD와 일치
- 리포지토리 단위 테스트 통과

## 2. 게시글 API 구현 (P0)
- [ ] DTO 및 매핑 구현
  - Create/Patch/List/Detail 응답 모델
- [ ] 엔드포인트 구현
  - `POST /api/v1/posts`
  - `GET /api/v1/posts/{postId}`
  - `GET /api/v1/posts?page=&size=&status=`
  - `PATCH /api/v1/posts/{postId}`
  - `DELETE /api/v1/posts/{postId}`
- [ ] 상태코드/헤더 준수
  - 생성 시 `201 Created`
  - 삭제 시 `204 No Content`
  - 미존재 시 `404 Not Found`
- [ ] 페이징 모델 표준화
  - `items`, `page`, `size`, `totalElements`, `totalPages`

완료 기준:
- PRD 명세의 5개 게시글 API가 모두 동작
- WebTestClient 기반 API 테스트 통과

## 3. 이미지 업로드 API 구현 (P0)
- [ ] 업로드 정책 구현
  - 허용 타입 이미지 MIME 제한
  - 파일 크기 제한
- [ ] S3 Async 업로드 서비스 구현
  - key 생성 규칙 정의 (`images/{uuid}.{ext}` 등)
  - Content-Type/Size 저장
- [ ] 엔드포인트 구현
  - `POST /api/v1/images` multipart/form-data
  - 응답: `url`, `key`, `contentType`, `size`
- [ ] 실패 처리
  - 잘못된 파일 타입/크기 초과/업로드 실패 시 표준 에러 응답

완료 기준:
- 업로드 성공 시 유효한 S3 URL 반환
- 정책 위반 테스트 케이스 통과

## 4. 인증/인가 최소 구현 (P1)
- [ ] 인증 주체 모델 정의 (토큰 연동 전이라도 인터페이스 분리)
- [ ] 권한 규칙 적용
  - 익명: 조회만 가능
  - 인증 사용자: CRUD 가능
- [ ] 임시 전략
  - 개발 단계용 Mock 인증 필터 또는 헤더 기반 사용자 식별
  - 이후 실제 인증 시스템(JWT/OAuth2) 교체 가능하도록 추상화

완료 기준:
- 읽기/쓰기 권한 분리가 테스트로 보장됨

## 5. 공통 에러/관측성/운영성 (P1)
- [ ] 전역 예외 처리
  - Validation 실패, NotFound, 비즈니스 예외, 인프라 예외 매핑
- [ ] 에러 응답 포맷 표준화
  - `code`, `message`, `timestamp`, `path`, `traceId`
- [ ] Correlation ID 처리
  - 요청 단위 traceId 생성/전파/로그 출력
- [ ] 기본 헬스체크/레디니스 노출

완료 기준:
- 실패 시 일관된 JSON 에러 응답 반환
- 요청 로그에서 traceId로 추적 가능

## 6. API 문서화 (P1)
- [ ] springdoc 설정
- [ ] 각 엔드포인트 요청/응답/에러 스키마 문서화
- [ ] 로컬에서 Swagger UI/OpenAPI 스펙 확인

완료 기준:
- PRD API가 문서에 모두 노출되고 실제 응답과 불일치 없음

## 7. 테스트 전략 실행 (P0)
- [ ] 단위 테스트
  - 서비스 로직, 검증, 상태 전이
- [ ] 웹 레이어 테스트
  - WebTestClient로 상태코드/본문/검증 실패 케이스
- [ ] 통합 테스트
  - Testcontainers PostgreSQL 기반 CRUD/페이징 검증
- [ ] S3 업로드 테스트
  - Mock 또는 LocalStack으로 성공/실패/정책 검증
- [ ] 커버리지 리포트 구성

완료 기준:
- 전체 테스트 green
- 라인/브랜치 커버리지 80% 이상

## 8. 성능/안정성 점검 (P2)
- [ ] 대용량 Markdown 저장/조회 시 응답시간 점검
- [ ] 페이징 쿼리 실행계획 확인 및 인덱스 보강
- [ ] 백프레셔/타임아웃/재시도 정책 기본값 점검

완료 기준:
- 주요 API 성능 저하 지점과 대응안 문서화

## 9. 배포 준비 (P2)
- [ ] Dockerfile 작성
- [ ] 환경변수 계약 문서화(DB/S3/보안/업로드 정책)
- [ ] 실행 가이드 정리
  - 로컬 실행
  - 테스트 실행
  - Swagger 접근 경로

완료 기준:
- 신규 개발자가 문서만으로 서버 기동 및 검증 가능

## 권장 구현 순서 (Sprint 단위)
1. Sprint 1: `0~2` (기반 + Post CRUD API + 기본 테스트)
2. Sprint 2: `3, 6, 7` (S3 업로드 + 문서화 + 테스트 강화)
3. Sprint 3: `4, 5, 8, 9` (권한, 운영성, 성능, 배포 준비)

## 리스크 및 선결정 항목
- [ ] 인증 방식 확정 필요 (JWT/OAuth2/외부 인증 위임)
- [ ] 삭제 정책 확정 필요 (하드 삭제 vs 소프트 삭제)
- [ ] S3 버킷/권한/리전/URL 정책 확정 필요
- [ ] 페이징 정렬 기준 확정 필요 (기본 `createdAt desc` 권장)

## 최종 완료 정의 (Definition of Done)
- [ ] PRD의 게시글/이미지 API 전부 구현 및 문서화 완료
- [ ] 익명/인증 사용자 권한 규칙 반영
- [ ] Testcontainers 포함 테스트 통과 및 커버리지 80% 이상
- [ ] 장애/오류 시 추적 가능한 로깅(traceId) 동작
- [ ] 로컬/스테이징에서 재현 가능한 실행 문서 제공
