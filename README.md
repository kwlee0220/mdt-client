# mdt-client

ETRI MDT(Manufacturing Digital Twin) 시스템에 접속하기 위한 Java 클라이언트 라이브러리.

MDT Manager가 제공하는 REST/MQTT 서비스에 HTTP로 접근하여 AAS(Asset Administration Shell)
모델, Submodel, 레지스트리, MDTInstance, 워크플로우 및 태스크를 조작할 수 있는 클라이언트 API를 제공한다.

프로젝트 구조, 주요 API, 설정·사용 예제와 현재 구현의 제한사항은
[mdt-client 개발자 가이드](docs/mdt-client-guide.md)를 참고한다.

## 주요 기능

- **MDTManager 접근**: `HttpMDTManager`를 통해 MDT 시스템의 최상위 진입점 제공
- **AAS 모델**: `aas4j` 기반의 Asset Administration Shell / Submodel 모델 처리 (JSON, AASX)
- **레지스트리**: Shell Registry, Submodel Registry 클라이언트
- **MDTInstance 관리**: 인스턴스 조회/생성/제어 (`HttpMDTInstanceManager`)
- **워크플로우 / 태스크**: 워크플로우 관리, `SetTask`, `AASOperationTask`, `OperationSubmodelTask`, REST RPC 태스크
- **MQTT 구독**: MDT Manager 이벤트 구독
- **표현식 처리**: ANTLR 기반 MDT 표현식 파서(`MdtExpr.g4`)

MDT 표현식의 문법, Java API와 CLI 활용 예제는 [MDTExpression 사용 가이드](docs/mdt-expr-guide.md)를 참고한다.

## 요구 사항

- JDK 21 (Gradle toolchain)
- 설치된 Gradle (현재 저장소에는 Gradle Wrapper 실행 파일이 없음)
- 의존 프로젝트 `:utils` (`../../common/utils`에 위치, `settings.gradle` 참조)

## 빌드

```bash
# 빌드 버전은 환경변수로 지정 (미지정 시 "unknown")
export MDT_BUILD_VERSION=1.0.0

gradle build                # 컴파일 + 테스트
gradle shadowJar            # 의존성 포함 fat jar (build/libs에서 산출물 확인)
gradle test                 # 테스트만 실행
```

## 설정

클라이언트 설정은 `mdt_client_config.yaml`로 관리한다.

```yaml
mdtUrl: "http://localhost:12985"
connectTimeout: "10s"
readTimeout: "30s"
workflowManagerUrl: "http://localhost:12989"
```

## Docker

`docker/` 디렉토리에 컨테이너 이미지 빌드 스크립트가 있다.

```bash
cd docker
./build_image.sh    # mdt-client-all.jar + 설정으로 이미지 빌드
./push_image.sh     # 레지스트리에 푸시
```

## 프로젝트 구조

```
src/main/java/mdt/
  client/        # HTTP 클라이언트 구현 (HttpMDTManager, 레지스트리/인스턴스/워크플로우 클라이언트)
  model/         # AAS/Submodel 모델, timeseries, 표현식(expr)
  task/          # 빌트인 태스크 구현
  workflow/      # 워크플로우 모델
  rt/            # ROS2 JSON·Java record·AAS 값 변환
  test/          # 수동 실행 예제
src/main/api/    # 공개 인터페이스 (mdt.model.MDTManager 등)
src/main/antlr/  # MdtExpr.g4 표현식 문법
src/test/java/   # JUnit 5 테스트
```

## 라이선스 / 저자

ETRI, Kang-Woo Lee
