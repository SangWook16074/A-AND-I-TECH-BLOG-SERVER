# PRD.md (Product Requirements Document)

## 📌 프로젝트 개요

**프로젝트명:** Blog Service API  
**작성일:** 2026-02-15  
**작성자:** 상욱 (Product Owner / Developer)  
**문서 목적:** 블로그 서비스 API를 **WebFlux, PostgreSQL 기반 MSA 서비스**로 개발하기 위한 요구사항을 정리한다.  
본 문서는 API의 기능적 요구사항 및 비기능 요구사항을 모두 담고 있다. :contentReference[oaicite:1]{index=1}

---

## 1. 🎯 배경 및 목적

본 서비스는 MSA 환경에서 분리된 독립 서비스로서 아래 목표를 가진다:

- 사용자(클라이언트)는 **Markdown으로 작성된 블로그 글**을 CRUD 할 수 있어야 한다.
- 이미지는 **S3에 저장**하고, 업로드 후 URL을 반환해 클라이언트가 Markdown에 삽입할 수 있도록 한다.
- 서비스는 **비동기 WebFlux** 기반으로 개발한다.
- DB는 **PostgreSQL**을 사용하며, 추후 캐싱을 위해 **Redis** 확장 고려.
- API 문서화는 **springdoc OpenAPI** 기반으로 자동 문서 제공. :contentReference[oaicite:2]{index=2}

---

## 2. 🧩 기술 스택

| 영역 | 기술 |
|------|------|
| Framework | Spring Boot + Spring WebFlux (Reactive) |
| DB | PostgreSQL (R2DBC 기반) |
| API 문서화 | springdoc OpenAPI |
| 테스트 | JUnit5, WebTestClient, Testcontainers |
| 캐싱(확장) | Redis (추후) |
| 이미지 저장 | AWS S3 |

---

## 3. 👥 사용자 및 역할

| 사용자 | 권한 |
|--------|------|
| 익명 사용자 | 게시글 조회(Read only) |
| 인증된 사용자 | 게시글 CRUD (Create/Read/Update/Delete) |
| 관리자 | 추후 도입 가능 |

---

## 4. 🧠 도메인 모델

### Post (게시물)

| 필드명 | 타입 | 설명 |
|--------|------|------|
| id | UUID | 고유 게시물 식별자 |
| title | String | 제목 (1~200자) |
| contentMarkdown | TEXT | Markdown 원문 |
| authorId | UUID | 작성자 식별자 |
| status | Enum | Draft / Published / Deleted |
| createdAt | Timestamp | 등록 시간 |
| updatedAt | Timestamp | 수정 시간 |

> 본문은 Markdown 원문만 저장하며, **HTML 변환은 클라이언트에서 처리한다**. :contentReference[oaicite:3]{index=3}

---

## 5. 🛠 API 명세

### 🖼 이미지 업로드

#### POST /api/v1/images
이미지 업로드 후 **S3 URL**을 반환한다.

**Request**
POST /api/v1/images
Content-Type: multipart/form-data
file: 이미지 파일


**Response**
```json
{
  "url": "https://bucket.s3.amazonaws.com/abcd.png",
  "key": "images/abcd.png",
  "contentType": "image/png",
  "size": 12345
}
📌 게시글 생성
POST /api/v1/posts
Request

{
  "title": "타이틀",
  "contentMarkdown": "Markdown 본문",
  "authorId": "작성자 UUID",
  "status": "Draft"
}
Response

201 Created
{
  "id": "UUID",
  "title": "...",
  "contentMarkdown": "...",
  "authorId": "...",
  "status": "Draft",
  "createdAt": "...",
  "updatedAt": "..."
}
📖 게시글 상세 조회
GET /api/v1/posts/{postId}
Response

200 OK
{
  "id": "UUID",
  "title": "...",
  "contentMarkdown": "...",
  "authorId": "...",
  "status": "Published",
  "createdAt": "...",
  "updatedAt": "..."
}
Not Found: 404 Not Found

📄 게시글 목록 조회
GET /api/v1/posts?page=&size=&status=
Response

200 OK
{
  "items":[...],
  "page":0,
  "size":20,
  "totalElements":100,
  "totalPages":5
}
✏️ 게시글 수정
PATCH /api/v1/posts/{postId}
Request

{
  "title": "...",
  "contentMarkdown": "...",
  "status": "Published"
}
Response

200 OK
{ ... }
❌ 게시글 삭제
DELETE /api/v1/posts/{postId}
Response

204 No Content
6. 💾 DB 설계 (개요)
posts 테이블
CREATE TABLE posts (
  id UUID PRIMARY KEY,
  title VARCHAR(200) NOT NULL,
  content_markdown TEXT NOT NULL,
  author_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
7. 🧪 테스트 요구사항 (TDD)
모든 API는 TDD 원칙을 지킨다.

단위 테스트: Service/Repository 단위 검증

통합 테스트: Testcontainers PostgreSQL

WebLayer 테스트: WebTestClient 기반

이미지 업로드 테스트: 허용 타입과 크기 정책 검증

8. 📈 비기능 요구사항
반응형 응답 (Reactive)

로깅/Tracing (Correlation ID)

springdoc으로 자동 문서 제공

캐싱 (추후 Redis)

9. 📊 성공 기준
API Endpoint 정상 동작

springdoc 문서 자동 확인 가능

TDD 기준: 코드 커버리지 ≥ 80%

S3 업로드 정상 URL 반환

Markdown 본문이 훼손 없이 저장 및 조회

10. 🗺 향후 기능 고려
Presigned URL 방식 이미지 업로드

관리자 기능

댓글/좋아요 기능

Redis 캐싱