# mdt.model.sm.ref — ElementReference & MdtExpr 표현식

SubmodelElement에 대한 **참조(reference)**를 다루는 패키지다. 참조 대상이 어디에 어떻게 저장되어
있든(로컬 모델, 원격 Submodel 저장소, 로컬 파일, MDT 연산 인자, 시계열 등) 동일한 인터페이스로
요소를 읽고/쓰고/값 갱신/첨부파일 입출력/JSON 직렬화할 수 있게 한다.

참조를 한 줄 문자열로 표현·파싱하는 DSL이 **MdtExpr**이며, 문법은 ANTLR4로 정의되어 있다
(`src/main/antlr/MdtExpr.g4`). `ElementReferences.parseExpr(String)`이 이 문법으로 파싱하고,
각 참조의 `toStringExpr()`이 역방향(참조 → 문자열)을 제공한다.

## 핵심 개념

- **`ElementReference`**: 모든 참조의 최상위 인터페이스. 저장 위치와 무관한 읽기/쓰기/값 갱신/
  첨부파일 입출력/JSON 직렬화를 제공한다.
- **`MDTElementReference`**: MDT 프레임워크가 관리하는 요소에 대한 참조. (instanceId, Submodel
  idShort, Element idShort path) 좌표로 대상을 식별하며, 사용 전에 **활성화(activation)** 가 필요하다.

## 참조 종류

| 클래스 | 가리키는 대상 | 직렬화 타입 |
|--------|----------------|-------------|
| `DefaultElementReference` | Submodel 참조 + idShort 경로로 지정한 임의의 SubmodelElement | `mdt:ref:element` |
| `MDTParameterReference` | 인스턴스 `Data` Submodel의 개별 파라미터(또는 하위 경로) | `mdt:ref:param` |
| `MDTParameterCollectionReference` | 인스턴스 `Data` Submodel의 파라미터 전체 집합 | `mdt:ref:param` |
| `MDTArgumentReference` | MDT 연산(Simulation/AI)의 입력/출력 인자 | `mdt:ref:oparg` |
| `OperationVariableReference` | `Operation` 요소의 입력/출력/입출력 변수 | `mdt:ref:opvar` |
| `TimeSeriesElementReference` | 시계열 Submodel의 세그먼트 레코드 | `mdt:ref:timeseries` |
| `FileStoreReference` | 로컬 파일에 저장된 SubmodelElement | `mdt:ref:file` |

## MdtExpr 표현식

`ElementReferences.parseExpr(String)`이 MdtExpr 문법으로 참조를 파싱한다(내부적으로
`mdt.model.expr.MDTExpressionParser` → 파스 트리 → `MDTExpressionVisitor` → 참조 객체).
선두 라벨로 종류가 결정되며, 라벨이 없으면 `DefaultElementReference`로 해석된다.
파서는 **입력을 끝까지 소비(EOF)** 하며 오류 시 `IllegalArgumentException`을 던진다.

### 서브모델 참조 (submodelSpec)
```
inst:Data                         # 인스턴스 inst의 idShort=Data 서브모델
inst:idShort=Data                 # 위와 동일(명시형)
submodel:id='http://.../sm/Data'  # 서브모델 ID로 직접 지정(URL은 따옴표 필요)
```

### 요소 참조
| 표현식 | 결과 |
|--------|------|
| `<submodelSpec>:<idShortPath>` | `DefaultElementReference` |
| `param:<instanceId>:<idShortPath\|INTEGER>` | `MDTParameterReference` |
| `param:<instanceId>:*` | `MDTParameterCollectionReference` |
| `oparg:<submodelSpec>:(in\|out):<name\|INTEGER\|*>` | `MDTArgumentReference` |
| `opvar:<submodelSpec>:<idShortPath>:(in\|out\|inout):<INTEGER>` | `OperationVariableReference` |
| `timeseries:<submodelSpec>:<idShortPath>[#<range>][\|<cols>]` | `TimeSeriesElementReference` |

- `idShortPath`는 `.`로 하위 요소, `[n]`으로 배열 인덱스를 표현한다(인덱스는 음수 불가).
- **opvar**는 연산 *요소*를 idShortPath로 가리킨다. 예: `opvar:inst:Data:Operation:out:1`.

### timeseries range / projection
range는 `#`, projection은 `|`로 구분한다. **projection을 생략하면 전체 컬럼**을 의미한다.

| range | 의미 |
|-------|------|
| `#last=5` | 마지막 5개 레코드 (접미사 없는 정수) |
| `#last=1h` / `#last=1h@latest` | 마지막 레코드 시각 기준 이전 1시간(기본 anchor=latest) |
| `#last=1h@now` | 현재 시각 기준 이전 1시간 |
| `#2024-01-01~2024-02-01` | 절대 구간 [from, to] |
| `#2024-01-01T09:00:00Z~` / `#~2024-02-01` | 한쪽 개방 구간 |

duration 값은 단순형(`30s`,`500ms`,`2h`,`1d`) 또는 ISO8601(`PT2H30M`,`P1D`,`P1DT2H30M`,`PT0.5S`).

예:
```
timeseries:inst:Data:Tail
timeseries:inst:Data:Tail#last=5
timeseries:inst:Data:Records.Temp#last=PT2H30M@now
timeseries:inst:Data:Tail#2024-01-01~2024-02-01|current,power
```

### 값 리터럴 (parseValueLiteral)
```
42  -5  3.14  true            # 숫자(음수는 값 리터럴에서만)/불리언
abc  'hello'                  # 식별자형/문자열
'안녕'@ko                      # 다국어 속성값(MLP)
file:'/tmp/a.txt'('text/plain')   # 파일 값
[1, 10]                       # 범위 값(Range)
```

> 문법 키워드(`length`,`last`,`in`,`file` 등)는 식별자로도 쓸 수 있다(soft keyword). 더 자세한
> 문법은 [CLAUDE.md](CLAUDE.md)와 `src/main/antlr/MdtExpr.g4` 참고.

## round-trip

참조 ↔ 문자열, 참조 ↔ JSON 양방향이 보존된다.

```java
// 표현식 round-trip
String expr = ref.toStringExpr();
ElementReference back = ElementReferences.parseExpr(expr);   // back.toStringExpr().equals(expr)

// JSON round-trip
String json = ref.toJsonString();
ElementReference back2 = ElementReferences.parseJsonString(json);
```
> `TimeSeriesElementReference.equals`는 submodel+tsSpec만 비교하고 range/columns는 제외한다.
> 따라서 range/columns 보존 검증은 `equals`가 아니라 `toStringExpr()`(이 둘을 인코딩)로 한다.

## 활성화 (activation)

`MDTElementReference` 계열은 생성/역직렬화 직후에는 좌표 정보만 가진 **비활성 상태**다. 실제 읽기/쓰기를
하려면 `MDTInstanceManager`로 활성화해야 한다.

```java
MDTElementReference ref = ElementReferences.parseExpr("param:test-instance:Temperature");
ref.activate(manager);          // 또는 ElementReferences.activate(ref, manager);
Property prop = ref.readAsProperty();
```
활성화 전 `getSubmodelService()`/`getInstance()` 호출 시 `IllegalStateException`. `getIdShortPathString()`은
활성화와 무관하게 호출 가능.

## 읽기 / 쓰기

```java
String  s = ref.readAsString();
Integer i = ref.readAsInt();      // readAsFloat/readAsLong/readAsBoolean/readAsDateTime/readAsDuration
SubmodelElementCollection coll = ref.readCollection();

ref.updateValue(elementValue);                 // ElementValue로 갱신
ref.updateValue("{\"value\": \"42\"}");        // JSON 문자열로 갱신
ref.write(submodelElement);                    // 요소 전체 교체

ref.readAttachment(outputFile);                // 'File' 요소 첨부파일
ref.updateAttachment(contentFile);
ref.removeAttachment();
```

## JSON 직렬화

각 참조는 `getSerializationType()`이 반환하는 `@type` 판별자로 구분되어 직렬화/역직렬화된다.
`ElementReferences`가 Jackson `Serializer`/`Deserializer`를 제공한다.

```java
String json = ref.toJsonString();
MDTElementReference parsed = ElementReferences.parseJsonString(json);
// 역직렬화된 참조는 비활성 상태이므로 사용 전 activate() 필요
```

> `mdt:ref:param`은 `MDTParameterReference`와 `MDTParameterCollectionReference`가 공유한다.
> 역직렬화 시 `parameterExpr`이 `*`이면 전체 집합 참조로, 아니면 개별 파라미터 참조로 복원된다.
