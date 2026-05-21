# CLAUDE.md

이 파일은 이 저장소에서 코드를 다룰 때 Claude Code가 참고하는 가이드다.

## 프로젝트 개요

`mdt-client`는 ETRI MDT(Manufacturing Digital Twin) 시스템에 접근하기 위한 Java 17
클라이언트 **라이브러리**다. (실행 가능한 애플리케이션이 아니라 다른 프로젝트가 의존하는 라이브러리)
MDT Manager의 REST/MQTT 서비스에 HTTP로 접속하여 AAS(Asset Administration Shell) 모델,
Submodel, 레지스트리, MDTInstance, 워크플로우/태스크를 조작한다.

## 빌드 / 테스트 명령

```bash
./gradlew build              # 컴파일 + 테스트
./gradlew shadowJar          # 의존성 포함 fat jar 생성
./gradlew test               # 전체 테스트 (JUnit 5)
./gradlew test --tests "mdt.model.sm.*"   # 특정 패키지 테스트
```

- 빌드 버전은 `MDT_BUILD_VERSION` 환경변수로 주입된다(미지정 시 `"unknown"`).
- 테스트는 JUnit 5(Jupiter) + Mockito 사용. `--add-opens=java.base/java.lang=ALL-UNNAMED` JVM 옵션이 필요하다(`build.gradle`에 설정됨).

## 소스 레이아웃 (중요)

`build.gradle`에서 main 소스셋은 **세 개의 srcDir**로 구성된다:

- `src/main/java`  — 구현 코드
- `src/main/api`   — 공개 인터페이스 (예: `mdt.model.MDTManager`, `mdt.model.instance.MDTInstanceManager`)
- `src/main/antlr` — ANTLR 문법(`MdtExpr.g4`)과 생성/연동 코드

따라서 인터페이스를 찾을 때는 `src/main/api`를, 구현을 찾을 때는 `src/main/java`를 보라.
같은 패키지명(`mdt.*`)이 세 디렉토리에 걸쳐 있다.

## 핵심 진입점

- `mdt.model.MDTManager` (api) — 시스템 최상위 인터페이스. `getInstanceManager()`, `getSubmodelRegistry()`, `getWorkflowManager()` 제공.
- `mdt.client.HttpMDTManager` (java) — `MDTManager`의 HTTP 구현. Builder 패턴으로 생성. MQTT 구독 지원.
- `mdt.client.MDTClientConfig` — `mdt_client_config.yaml` 로딩 (YAML, 환경변수 보간 지원).

## 의존성 메모

- `:utils` — `../../common/utils`에 위치한 형제 프로젝트(`settings.gradle`). Lombok, logback, 다양한 유틸리티(`utils.*` 패키지)를 이 프로젝트에서 가져온다.
- AAS 모델: `org.eclipse.digitaltwin.aas4j` (JSON / AASX 데이터포맷)
- HTTP: Spring `spring-web` (RestClient/HTTP Interface) + OkHttp
- 직렬화: Jackson (databind, jsr310, yaml)
- MQTT: Eclipse Paho
- 표현식: ANTLR 4.13.2

## 코드 컨벤션

- 필드는 `m_` 접두사를 사용한다 (예: `m_endpoint`, `m_restfulClient`).
- Lombok 사용 (`@Nullable`은 jetbrains, `@GuardedBy`는 jsr305 — 둘 다 `compileOnly`).
- Builder 패턴이 클라이언트/매니저 클래스에 광범위하게 쓰인다.
- 주석/Javadoc은 **한국어**로 작성되어 있다. 새 주석도 한국어로 맞춘다.
- 저자 태그: `@author Kang-Woo Lee (ETRI)`.

## 코드 수정 정책

- 명시적으로 코드 수정/작성을 요청한 경우에만 코드를 수정한다.
- 질문·설명·의견 요청에는 답변만 하고 코드는 건드리지 않는다.
