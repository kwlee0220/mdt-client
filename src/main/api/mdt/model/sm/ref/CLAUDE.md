# CLAUDE.md — mdt.model.sm.ref (ElementReference & MdtExpr)

SubmodelElement에 대한 **참조(reference)**를 추상화한 패키지(`src/main/api`)와, 참조를 문자열로
표현·파싱하는 DSL **MdtExpr**(ANTLR4 문법, `src/main/antlr/`)에 대한 통합 가이드다.
사용자용 구문 레퍼런스는 [README.md](README.md)에 있다.

## 구성 파일과 위치

| 역할 | 경로 |
|------|------|
| 참조 타입(도메인) | `src/main/api/mdt/model/sm/ref/*.java` |
| 시계열 참조/판독기/범위 | `src/main/api/mdt/model/sm/ref/timeseries/*.java` |
| JSON 직렬화 파사드 | `mdt/model/sm/ref/ElementReferences.java` |
| MdtExpr 문법(소스) | `src/main/antlr/MdtExpr.g4` |
| ANTLR jar / 재생성 스크립트 | `src/main/antlr/antlr-4.13.2-complete.jar`, `src/main/antlr/antlr4_script.sh` |
| 생성 파서/렉서 | `src/main/antlr/mdt/model/expr/Mdt*{Parser,Lexer}.java`, `*Visitor.java` |
| 파서 파사드 / 파스 트리 → 객체 | `src/main/java/mdt/model/expr/MDTExpressionParser.java`, `MDTExpressionVisitor.java` |
| 표현식 Expr 객체 | `src/main/java/mdt/model/expr/*Expr.java`, `LiteralExpr.java`, `TerminalExpr.java` |

## 타입 계층

```
ElementReference (interface)                — 읽기/쓰기/값/첨부파일/JSON 직렬화 추상화
├─ AbstractElementReference (abstract)      — update/toJsonString/toJsonNode/Logger 공통 구현
│  ├─ FileStoreReference                    — 로컬 파일에 저장된 SubmodelElement
│  └─ SubmodelBasedElementReference (abstract)  — SubmodelService + idShortPath 기반 참조
│     ├─ DefaultElementReference            — Submodel 참조 + idShort 경로
│     ├─ MDTParameterReference              — Data Submodel의 개별 파라미터
│     ├─ MDTParameterCollectionReference    — Data Submodel의 파라미터 전체 집합
│     ├─ MDTArgumentReference               — MDT 연산(Simulation/AI)의 입/출력 인자
│     ├─ OperationVariableReference         — Operation 요소의 입력/출력/입출력 변수
│     └─ TimeSeriesElementReference         — (timeseries/) 시계열 Submodel 레코드
└─ MDTElementReference (interface)          — MDT 관리 요소 (instanceId/submodelIdShort/idShortPath)
```

- **`ElementReference`** — `read()`/`readValue()`/타입별 편의 읽기, `write()`/`update()`/`updateValue()`,
  첨부파일 입출력, JSON 직렬화(`toJsonString`/`serializeFields`/`getSerializationType`). `@JsonSerialize`/
  `@JsonDeserialize`로 `ElementReferences.Serializer`/`Deserializer`에 위임.
- **`MDTElementReference`** — 세 좌표(instanceId, Submodel idShort, idShortPath) + 활성화 API.
- **`SubmodelBasedElementReference`** — `getSubmodelReference()`/`getSubmodelService()`/
  `getIdShortPathString()`/`getInstance()`를 하위 클래스가 채운다.

## 활성화(activation) 라이프사이클

`MDTElementReference` 계열은 **생성/역직렬화 직후 비활성**이다. `activate(MDTInstanceManager)` 전에
`getInstance()`/`getSubmodelService()`를 호출하면 `IllegalStateException`. `getIdShortPathString()`은
활성화와 무관. `ElementReferences.activate(ref, manager)`는 `MDTElementReference`가 아니면 IAE.

## JSON 직렬화 — 타입 판별자

| SERIALIZATION_TYPE   | 클래스 |
|----------------------|--------|
| `mdt:ref:element`    | `DefaultElementReference` |
| `mdt:ref:param`      | `MDTParameterReference` / `MDTParameterCollectionReference` (분기: `parameterExpr=="*"`) |
| `mdt:ref:oparg`      | `MDTArgumentReference` |
| `mdt:ref:opvar`      | `OperationVariableReference` |
| `mdt:ref:timeseries` | `TimeSeriesElementReference` |
| `mdt:ref:file`       | `FileStoreReference` |

새 참조 타입 추가 시: (1) `SERIALIZATION_TYPE` 상수, (2) `serializeFields`/`deserializeFields`,
(3) `ElementReferences.parseTypedJsonNode` 분기, (4) 표현식이 필요하면 문법·visitor·Expr까지 함께 갱신.

## 표현식 파싱 — MdtExpr 문법으로 일원화

`ElementReferences.parseExpr(String)`는 **ANTLR `MDTExpressionParser`에 위임**한다(옛 손코딩 SplitStream
파서는 폐기). 처리 흐름:
```
문자열 → MdtExprLexer → MdtExprParser(시작 규칙) → 파스 트리
       → MDTExpressionVisitor.visit() → *Expr 객체 → .evaluate() → 참조 객체
```
- `MDTExpressionParser`의 각 정적 메서드는 시작 규칙 하나에 대응하며 **EOF를 강제**(후행 토큰/구문
  오류 시 `ThrowingErrorListener`가 `IllegalArgumentException`).
- 새 문법 규칙을 추가하면 대응 `visitXxx`를 `MDTExpressionVisitor`에 구현해야 한다(미구현 시 기본
  visitor가 자식 결과를 흘려보내 캐스팅 오류).

### 문법 수정 워크플로 (중요)
`.g4`를 고치면 **반드시 재생성** 후 컴파일한다(생성물은 커밋 대상):
```bash
cd src/main/antlr && ./antlr4_script.sh && cd ../../..
./gradlew compileJava
```

### 표현식 형태 (현재 문법)
- `<submodelSpec>:<idShortPath>` → `DefaultElementReference`
- `param:<instanceId>:<idShortPath|INTEGER|*>` → `MDTParameterReference`(/`*`면 Collection)
- `oparg:<submodelSpec>:(in|out):<idOrString|INTEGER|*>` → `MDTArgumentReference`
- `opvar:<defaultElementSpec>:(in|out|inout):<INTEGER>` → `OperationVariableReference`
- `timeseries:<submodelSpec>:<idShortPath>[#<range>][|<cols>]` → `TimeSeriesElementReference`

`submodelSpec` = `inst:Data` | `inst:idShort=Data` | `submodel:id='...'`.

## 문법/렉서 작성 시 주의

- **렉서 규칙 선언 순서 = 동일 길이 매칭 시 우선순위.** `NULL`/`BOOLEAN`은 `ID`보다,
  `TIMESTAMP`/`ISO8601_DURATION`은 `ID`/`INTEGER`보다 **앞에** 둔다(뒤에 두면 `ID`/`INTEGER`가 먼저
  먹어 토큰이 죽는다).
- **음수 정책**: `INTEGER`/`FLOAT`는 부호 없음. 음수는 `propertyValueLiteralSpec`에서 `'-'?`로만 허용
  (length/배열 인덱스/파라미터·연산변수 인덱스/duration은 음수 차단).
- **soft keyword**: 문법 리터럴(`length`,`in`,`file`…)을 식별자로도 쓰려면 `keyword` 규칙에 넣어
  `idOrString`에 합친다. 새 리터럴 키워드 추가 시 충돌 방지를 위해 `keyword` 포함 검토.
- **Java 17**: switch 패턴 매칭은 preview라 사용 금지. sealed 타입 분기는 `instanceof` 패턴 + if-else.

## timeseries/ 서브패키지

`TimeSeriesElementReference` + 판독기(`SegmentRecordsReader`, `InternalSegmentRecordsReader`,
`LinkedSegmentRecordsReader`) + 범위 `Range`(sealed). 문법 `tsRangeSpec` ↔ `Range` 매핑:

| 문법 | `Range` |
|------|---------|
| `last=N`(접미사 없는 정수) | `Range.Count(N)` — 마지막 N개 레코드 |
| `last=dur@latest`(기본) | `Range.Trailing(dur, LATEST)` — 마지막 레코드 시각 기준 |
| `last=dur@now` | `Range.Trailing(dur, NOW)` — 현재 시각 기준 |
| `from~to` | `Range.Absolute(from, to)` (한쪽 null = 개방) |
| `\|col,...` | `columns([...])`, 생략 시 전체 |

- `TIMESTAMP`→`Instant` 정책(`MDTExpressionVisitor.parseTimestamp`): zone 명시 시 그대로, zone 없으면
  UTC, 날짜만이면 그 날 00:00:00 UTC.
- `TimeSeriesElementReference`는 내부적으로 컬럼 맨 앞에 `"Time"`을 추가하지만, `toStringExpr`/
  `serializeFields` 모두 직렬화 시 `"Time"`을 제외한다(역직렬화 시 생성자가 다시 prepend).
- `equals`는 submodel+tsSpec만 비교(range/columns 제외).

## opvar 특이사항

`opVarSpec: defaultElementSpec ':' (in|out|inout) ':' INTEGER` — 연산 *요소*를 idShortPath로 가리킨다.
`OperationVariableReference.newInstance(SubmodelBasedElementReference opRef, Kind, int)`가 그 요소
참조를 보유한다. `Kind`는 INPUT/OUTPUT/INOUTPUT이며 `toString()`이 `in`/`out`/`inout`을 반환해
`toStringExpr`(`opvar:<elementExpr>:<kind>:<ordinal>`)이 문법과 round-trip된다.

## 테스트

`src/test/java/mdt/model/sm/ref/`(및 `.../timeseries/`)에 클래스별 테스트가 있다. 모든 직렬화 대상
참조는 **expr round-trip**(`parseExpr(toStringExpr())`)과 **JSON round-trip**
(`parseJsonString(toJsonString())`)을 검증한다. `TimeSeriesElementReference`처럼 `equals`가 일부
필드를 무시하는 경우 round-trip 비교는 `toStringExpr()` 문자열로 한다.

## 규약

- 인자 검증은 `utils.Preconditions.checkNotNullArgument`/`checkArgument`/`checkState`.
- 비활성/활성 참조를 구분하고, `SubmodelService`가 필요한 메서드는 활성화 전제를 문서화.
- 새 `SubmodelBasedElementReference` 하위 클래스는 4개 추상 메서드를 구현.
- 새 참조 타입은 JSON 판별자·문법·visitor·Expr·`toStringExpr`를 일괄 갱신(round-trip 유지).
