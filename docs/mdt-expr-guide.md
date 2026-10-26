# MDTExpression(MdtExpr) 사용 가이드

MDT 시스템의 자원(Submodel, SubmodelElement, 파라미터, 연산 인자, 시계열 레코드)과 값(리터럴)을
**한 줄 문자열**로 표현하는 표현식 언어 **MdtExpr**의 사용법을 설명한다. 설비 파라미터를 읽거나
시뮬레이션 인자를 연결할 때 긴 모델 경로 대신 `param:line-01:Temperature` 같은 짧은 참조를 쓸 수 있고,
CLI 옵션, REST API의 `ref` 파라미터, 워크플로 태스크 인자, Java API 등 MDT 전반에서 같은 문법이 쓰인다.

이 가이드는 MDT 클라이언트 개발자와 CLI 사용자를 대상으로 기본 문법, Java API, 시계열 조회, 값 갱신,
태스크 활용법을 설명한다.

- 문법 정의: [`src/main/antlr/MdtExpr.g4`](../src/main/antlr/MdtExpr.g4)
- 파서 파사드: [`mdt.model.expr.MDTExpressionParser`](../src/main/java/mdt/model/expr/MDTExpressionParser.java)
- 참조 타입 레퍼런스: [`mdt.model.sm.ref` README](../src/main/api/mdt/model/sm/ref/README.md)

> 이 문서의 예제는 2026년 10월 기준 구현으로 실제 파싱해서 결과를 확인했다. 예제의 `line-01`은
> MDTInstance id이고, `Data`, `Simulation`, `Log` 등은 Submodel idShort이다. 실제 환경의 식별자로 바꿔
> 사용한다.

---

## 1. 한눈에 보기

| 하고 싶은 일 | 표현식 | 파싱 결과 |
|--------------|--------|-----------|
| 인스턴스의 Submodel 지정 | `line-01:Data` | `DefaultSubmodelReference` |
| Submodel 안의 일반 요소 지정 | `line-01:Data:DataInfo.Equipment.EquipmentName` | `DefaultElementReference` |
| 설비 파라미터 값 지정 | `param:line-01:Temperature` | `MDTParameterReference` |
| 파라미터 전체 지정 | `param:line-01:*` | `MDTParameterCollectionReference` |
| 시뮬레이션 입력 인자 지정 | `oparg:line-01:Simulation:in:Speed` | `MDTArgumentReference` |
| AAS `Operation` 요소의 첫 출력 변수 지정 | `opvar:line-01:Ops:Calc:out:0` | `OperationVariableReference` |
| 최근 1시간의 `current` 컬럼 시계열 조회 | `timeseries:line-01:Log#last=1h\|current` | `TimeSeriesRecordsReference` |
| 값 리터럴 생성 | `25.3`, `'hello'`, `'정상'@ko`, `[0, 100]` | `ElementValue` 계열 |

> 표 안의 `\|`는 Markdown 표 때문에 이스케이프한 것이고, 실제 표현식에서는 `|` 한 글자다.

### 처리 흐름

```
문자열 ──▶ MdtExprLexer ──▶ MdtExprParser ──▶ 파스 트리
       ──▶ MDTExpressionVisitor ──▶ *Expr 객체 (MDTExpression)
       ──▶ evaluate() ──▶ 참조 객체 / 값 객체
       ──▶ (참조인 경우) activate(MDTInstanceManager) ──▶ read / updateValue ...
```

`MDTExpression`은 `Object evaluate()` 하나만 가진 인터페이스다. 파서는 표현식을 `*Expr` 객체로
만들고, `evaluate()`를 호출하면 실제 참조 객체(`ElementReference`)나 값 객체(`ElementValue`)가 나온다.
대부분의 경우 이 단계를 감싼 파사드(`ElementReferences.parseExpr()` 등, [9장](#9-파서-api-정리))를
쓰면 된다.

참조 표현식을 파싱하면 **대상을 가리키는 객체**만 생긴다. 파싱 성공은 대상의 존재나 읽기·쓰기 가능
여부를 보장하지 않으며, 인스턴스와 모델을 찾는 작업은 활성화 또는 실제 접근 단계에서 이루어진다.

---

## 2. 빠른 시작 (Java)

```java
import mdt.client.HttpMDTManager;
import mdt.model.instance.MDTInstanceManager;
import mdt.model.sm.ref.ElementReferences;
import mdt.model.sm.ref.MDTElementReference;
import mdt.model.sm.value.ElementValue;
import mdt.model.sm.value.ElementValues;

// mdt_client_config.yaml 설정으로 접속. 엔드포인트를 직접 주려면 HttpMDTManager.connect("http://localhost:12985")
MDTInstanceManager manager = HttpMDTManager.connectWithDefault().getInstanceManager();

// 1) 표현식 → 참조 객체 (이 시점에는 좌표만 가진 '비활성' 상태, 네트워크 접근 없음)
MDTElementReference ref = ElementReferences.parseExpr("param:line-01:Temperature");

// 2) 활성화: 실제 MDTInstance/SubmodelService에 연결
ref.activate(manager);

// 3) 읽기
ElementValue value = ref.readValue();        // 값 객체
Float temperature = ref.readAsFloat();       // 타입별 편의 메서드

// 4) 쓰기: 값 리터럴 표현식 → ElementValue
ref.updateValue(ElementValues.parseExpr("25.3"));
```

네트워크 작업의 `IOException`은 호출자가 처리한다. 대상이 실제로 있는지는 `activate()` 또는 첫
읽기/쓰기에서 확인되며, 없으면 그때 `ResourceNotFoundException` 등이 발생한다.

---

## 3. 공통 어휘 규칙

### 3.1 기호

| 기호 | 용도 | 예 |
|------|------|----|
| `:` | 참조 종류, 인스턴스, Submodel, 경로 구분 | `line-01:Data:Status` |
| `.` | 하위 요소 선택 | `Status.Temperature` |
| `[n]` | 0부터 시작하는 리스트 인덱스 | `Samples[0].Value` |
| `*` | 파라미터 전체 또는 MDT 연산 인자 전체 | `param:line-01:*`, `oparg:...:out:*` |
| `#` | 시계열 조회 범위 시작 | `#last=100` |
| `\|` | 시계열 컬럼 선택 시작 | `\|Temperature,Pressure` |
| `@` | 언어 코드 또는 기간의 기준점 | `'정상'@ko`, `last=1h@now` |
| `~` | 시계열 시작·종료 시각 구분 | `2026-10-08T00:00:00Z~2026-10-08T01:00:00Z` |
| `+` | 시계열 시작 시각과 레코드 개수 연결 | `2026-10-08T00:00:00Z+100` |
| `=` | 명시적 식별자, 조회 조건, 대입 | `idShort=Data`, `last=10`, `A = 10` |

MdtExpr는 모델 참조와 값 기술에 쓰는 DSL이다. 다음은 지원하지 않는다.

- 산술식(`1 + 2`), 비교식, 조건식, 함수 호출
- 표현식 안의 주석
- `*`를 이용한 패턴 매칭이나 재귀 검색 (`*`는 위 표의 "전체 선택" 위치에서만 쓸 수 있다)

### 3.2 구분자와 공백

- 요소 사이의 구분자는 `:`이다.
- 토큰 사이의 공백은 무시된다. `line-01 : Data : A . B`는 `line-01:Data:A.B`와 같다. 다만 셸이나 URL에서
  다루기 쉽도록 공백 없이 쓰기를 권장한다.
- 파서는 **입력 전체를 소비해야** 성공한다. 남는 토큰이 있으면 오류다(예: `line-01:Data:X`를 Submodel
  참조로 파싱하면 `:X`가 남아 실패).

### 3.3 이름(식별자)과 따옴표

인스턴스 id, Submodel idShort, 요소 idShort, 컬럼명 같은 이름은 다음 두 형태로 쓴다.

| 형태 | 규칙 | 예 |
|------|------|----|
| 따옴표 없는 식별자 | `[a-zA-Z_][a-zA-Z0-9_-]*` (영문자/`_`로 시작, 이후 영숫자·`_`·`-`). 대소문자 구별 | `line-01`, `Data`, `_tmp`, `EquipmentName` |
| 문자열 | `'...'` 또는 `"..."` | `'my inst'`, `"Item 1"`, `'설비 1'`, `'http://x/sm/1'` |

문자열은 바깥 따옴표만 제거하며 escape 문법이 없다. `\n`을 줄바꿈으로 바꾸거나 `\'`로 작은따옴표를
넣을 수 없고, 작은따옴표가 들어간 이름은 큰따옴표로 감싼다(`"it's"`).

**다음 경우에는 반드시 따옴표로 감싸야 한다.**

| 경우 | 잘못된 예 | 올바른 예 |
|------|-----------|-----------|
| 공백·한글·`/`·`:`·`.` 등 특수문자 포함 | `line-01:Data:Item 1` | `line-01:Data:'Item 1'` |
| 숫자로 시작 | `line-01:Data:1stAxis` | `line-01:Data:'1stAxis'` |
| 예약 토큰 `true`, `false`, `null` | `line-01:Data:true` | `line-01:Data:'true'` |
| 기간 단위 토큰 `ms`, `s`, `m`, `h`, `d` | `line-01:Data:s` | `line-01:Data:'s'` |
| ISO8601 기간 모양(`P1D`, `PT5M` 등) | `line-01:Data:P1D` | `line-01:Data:'P1D'` |
| URL 형태의 Submodel id | `submodel:id=http://...` | `submodel:id='http://...'` |

반면 문법 키워드인 `mdt`, `idShort`, `submodel`, `id`, `param`, `oparg`, `opvar`, `timeseries`, `in`,
`out`, `inout`, `last`, `now`, `latest`, `file`은 **soft keyword**여서 이름 자리에 따옴표 없이 쓸 수 있다.
예를 들어 `param:last:in`은 인스턴스 `last`의 파라미터 `in`을 가리킨다. 다만 값 리터럴 자리에서는
같은 규칙이 적용되지 않으므로, 문자열 값은 `'last'`처럼 따옴표로 감싸는 편이 명확하다.

> **예외:** `oparg:`의 인자 이름은 따옴표를 벗기지 않고 원문 그대로 보존한다([6.3절](#63-연산-인자-참조--oparg)).

### 3.4 숫자

- 정수 `INTEGER`는 `[0-9]+`, 실수 `FLOAT`는 `[0-9]+.[0-9]+`이다. `.5`, `1.`, `1e3` 같은 표기는 쓸 수 없다.
- **음수는 값 리터럴에서만** 쓸 수 있다(`-5`, `-0.5`). 배열 인덱스, 파라미터/인자 번호, 개수, 기간에는
  음수를 쓸 수 없다.
- 정수 리터럴은 Java `int` 범위(최대 `2147483647`)까지만 허용된다.

---

## 4. Submodel 참조 (`submodelSpec`)

Submodel을 가리키는 형태는 세 가지다.

| 형태 | 의미 |
|------|------|
| `<instanceId>:<submodelIdShort>` | 인스턴스의 idShort가 일치하는 Submodel |
| `<instanceId>:idShort=<submodelIdShort>` | 위와 같음(명시형) |
| `submodel:id=<submodelId>` | 전역 Submodel id로 직접 지정 |

```text
line-01:Data
line-01:idShort=Data
'line 01':'Operation Data'
submodel:id='http://mdt.etri.re.kr/line-01/sm/Data'
```

- Submodel idShort(인스턴스 안에서만 유일)와 전역 id(대개 URL·URN)를 혼동하지 않도록 한다.
- Submodel id에는 `:`·`/`가 들어가므로 따옴표로 감싸야 한다.
- **주의:** `submodel:Data`는 id 기반 참조가 아니다. 인스턴스 id가 `submodel`인 인스턴스의 `Data`
  Submodel로 해석된다. id 기반 참조는 반드시 `submodel:id=...` 형태로 쓴다.

Submodel 참조는 단독으로도 쓰이고(`mdt run submodel`, REST의 Submodel 조회 등), 아래의 요소 참조와
`oparg`/`opvar`/`timeseries` 참조의 앞부분으로도 쓰인다.

---

## 5. idShortPath

Submodel 안의 요소 경로는 idShort를 `.`로 잇고, 리스트(`SubmodelElementList`) 원소는 `[n]`(0부터)으로
지정한다.

```text
DataInfo.Equipment.EquipmentName
DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue
Matrix[0][2]
Items.'Item 1'.Value
```

- 경로는 Submodel 바로 아래 요소의 idShort로 시작해야 한다(`[0].A`는 쓸 수 없다).
- 인덱스는 음수를 쓸 수 없고(`A[-1]`은 오류), 슬라이스나 `*` 선택도 지원하지 않는다.
- 끝에 `.`이 남으면 오류다(`A.B.`).

---

## 6. 요소 참조

요소 참조는 선두 라벨(`param:`, `oparg:`, `opvar:`, `timeseries:`)에 따라 종류가 정해진다. 라벨이
없으면 기본 요소 참조로 해석한다. 모든 요소 참조는 `ElementReferences.parseExpr(String)`으로 파싱한다.

### 6.1 기본 요소 참조

```text
<submodelSpec>:<idShortPath>
```

Submodel 안의 임의의 SubmodelElement를 가리킨다. 결과는 `DefaultElementReference`이다.

```text
line-01:Data:DataInfo.Equipment.EquipmentName
line-01:idShort=Data:DataInfo.Equipment
submodel:id='http://mdt.etri.re.kr/line-01/sm/Data':DataInfo.Equipment
line-01:Data:DataInfo.Equipment.EquipmentParameterValues[1].ParameterValue
```

### 6.2 파라미터 참조 — `param:`

```text
param:<instanceId>:<parameterId | index | parameterId 하위경로 | *>
```

인스턴스 `Data` Submodel에 정의된 **파라미터의 값(`ParameterValue`)**을 가리킨다. 파라미터 컬렉션 안의
실제 경로를 몰라도 파라미터 이름만으로 접근할 수 있다.

| 예 | 의미 |
|----|------|
| `param:line-01:Temperature` | id가 `Temperature`인 파라미터의 값 |
| `param:line-01:0` | 0번째 파라미터의 값 |
| `param:line-01:Pose.Position.X` | 파라미터 `Pose` 값의 하위 요소 `Position.X` |
| `param:line-01:Points[2]` | 파라미터 `Points` 값(리스트)의 2번 원소 |
| `param:line-01:*` | 파라미터 값 집합 전체 (`MDTParameterCollectionReference`) |

하위 경로는 선택한 `ParameterValue` 내부에 적용된다. 활성화 시점에 인스턴스의 asset type에 따라 실제
경로가 결정된다.

| asset type | 실제 경로 |
|------------|-----------|
| `Machine` | `DataInfo.Equipment.EquipmentParameterValues[i].ParameterValue<하위경로>` |
| `Process` | `DataInfo.Operation.OperationParameterValues[i].ParameterValue<하위경로>` |

그 밖의 asset type에서는 `param:` 참조를 쓸 수 없다. `Data` Submodel의 다른 필드는 기본 요소 참조로
지정한다.

### 6.3 연산 인자 참조 — `oparg:`

```text
oparg:<submodelSpec>:(in | out):<argName | index | *>
```

`Simulation` 또는 `AI` Submodel로 기술된 MDT 연산의 입력/출력 인자 값을 가리킨다.

| 예 | 의미 |
|----|------|
| `oparg:line-01:Simulation:in:Speed` | 입력 인자 중 `InputID`가 `Speed`인 인자의 값 |
| `oparg:line-01:Simulation:out:0` | 0번째 출력 인자의 값 |
| `oparg:line-01:Simulation:out:*` | 모든 출력 인자 |
| `oparg:submodel:id='urn:sm:sim':out:Result` | id로 지정한 Submodel의 출력 인자 `Result` |

- 종류는 `in`과 `out`만 쓸 수 있다(`inout` 불가).
- 활성화 시 Submodel의 semanticId로 연산 종류를 판별하고, 실제 경로
  `SimulationInfo.Inputs[i].InputValue`, `AIInfo.Outputs[i].OutputValue` 등으로 바꾼다. Simulation/AI
  Submodel이 아니면 오류가 발생한다.
- `*`로 읽으면 각 인자 값 요소(idShort가 인자 이름으로 설정된 것)를 담은 `SubmodelElementList`가 반환된다.
- 이 참조를 읽는 것만으로 시뮬레이션이나 AI 연산이 실행되지는 않는다. 입력값을 준비하는 용도이고, 실행은
  별도의 연산 실행 API나 CLI 명령(`mdt run submodel`)으로 한다.
- **인자 이름은 따옴표를 포함한 원문이 그대로 보존된다.** `oparg:...:in:'Speed'`는 `Speed`가 아니라
  `'Speed'`라는 이름으로 조회되므로, 따옴표 없는 이름이나 인덱스를 쓴다.

### 6.4 Operation 변수 참조 — `opvar:`

```text
opvar:<submodelSpec>:<idShortPath>:(in | out | inout):<index>
```

AAS `Operation` 요소의 입력/출력/입출력 변수를 가리킨다. `<idShortPath>`는 **Operation 요소 자체**의
경로이고, `<index>`는 같은 종류 변수 목록 안에서의 순번(0부터)이다.

```text
opvar:line-01:Ops:Calc:in:0          # Ops Submodel의 Operation 'Calc'의 첫 번째 입력 변수
opvar:line-01:Ops:Group.Calc:out:1   # 하위 경로에 있는 Operation의 두 번째 출력 변수
opvar:line-01:Ops:Calc:inout:0       # 첫 번째 입출력 변수
```

- `oparg`가 MDT의 인자 모델(`Inputs`/`Outputs` 리스트)을 이름이나 번호로 찾는 것과 달리, `opvar`는 실제
  AAS `Operation` 요소의 변수 목록에서 번호로 찾는다.
- 변수는 이름이나 `*`가 아니라 번호로만 지정할 수 있다(`opvar:...:in:x`는 오류).
- 변수 참조 자체는 Operation을 호출하지 않는다.

### 6.5 시계열 참조 — `timeseries:`

```text
timeseries:<submodelSpec>[#<range>][|<column>(,<column>)*]
```

시계열 Submodel의 레코드를 조회한다. 범위는 `#`로, 컬럼 선택은 `|`로 지정하며 둘 다 생략할 수 있다
(대괄호는 설명용 표기이고 실제 입력에는 쓰지 않는다). 실제 읽기는 대상 Submodel의 `ReadRecordsByRange`
연산을 호출하므로 Submodel이 이 연산을 제공해야 한다. **읽기 전용**이라 `write()`/`updateValue()`는
`UnsupportedOperationException`을 던진다.

```text
timeseries:line-01:Log                                   # 전체 레코드, 전체 컬럼
timeseries:line-01:Log#last=5                            # 마지막 5개 레코드
timeseries:line-01:Log#last=10s                          # 최신 레코드 기준 이전 10초
timeseries:line-01:Log#last=1h@now                       # 현재 시각 기준 이전 1시간
timeseries:line-01:Log#last=PT2H30M|current,power        # 최근 2시간 30분, 두 컬럼만
timeseries:line-01:Log#2024-01-01T09:00:00Z~2024-01-01T18:00:00Z
timeseries:line-01:Log#2024-01-01T09:00:00+09:00~        # 해당 시각 이후 전부
timeseries:line-01:Log#~2024-02-01T00:00:00Z             # 해당 시각 이전 전부
timeseries:line-01:Log#2024-01-01T09:00:00Z+100          # 해당 시각부터 100개
timeseries:line-01:Log|current                           # 전체 기간, current 컬럼만
```

> 시계열은 Submodel 단위로 가리킨다. `timeseries:line-01:Log:Tail#last=5`나 `...:Segments.FullRange`처럼
> Submodel 뒤에 요소 경로를 붙이는 형태는 지원하지 않는다.

`read()`는 레코드들을 담은 `SubmodelElementCollection`을, `readValue()`는 `ElementCollectionValue`를
반환한다. `TimeSeriesRecordsReference.equals()`는 Submodel 참조만 비교하므로 범위와 컬럼까지 같은 조회인지
확인하려면 그 조건을 따로 비교해야 한다.

#### 범위(range)

| 문법 | 의미 | `TimeSeriesRange` |
|------|------|-------------------|
| (생략) | 범위 조건 없이 전체 조회 | — |
| `last=N` | 마지막 N개 레코드 (단위 없는 정수) | `Count(N)` |
| `last=<기간>` 또는 `last=<기간>@latest` | **가장 최신 레코드 시각**부터 거슬러 올라간 기간 | `Trailing(기간, LATEST)` |
| `last=<기간>@now` | **현재 시각**부터 거슬러 올라간 기간 | `Trailing(기간, NOW)` |
| `<from>~<to>` | 절대 시간 구간(양끝 포함) | `Absolute(from, to)` |
| `<from>~` / `~<to>` | 한쪽이 열린 구간 | `Absolute(from, null)` / `Absolute(null, to)` |
| `<from>+N` | `from`(포함)부터 시간 순으로 최대 N개 | `Since(from, N)` |

- `last=10`은 레코드 개수이고 `last=10s`는 기간이다. `last=5@now`처럼 개수 범위에는 anchor를 붙일 수 없다.
- 절대 구간의 시작은 종료보다 늦을 수 없다.
- 결과 레코드는 범위 종류와 무관하게 항상 **시각 오름차순**이다.
- `@latest`와 `@now`의 차이: 데이터 수집이 멈춘 장비라면 `last=1h@now`는 빈 결과일 수 있지만
  `last=1h`(=`@latest`)는 마지막으로 수집된 1시간을 돌려준다.

#### 기간(duration)

| 형태 | 예 | 비고 |
|------|----|------|
| 단순형 | `500ms`, `30s`, `2m`, `2h`, `1d` | 정수 + 단위(`ms`, `s`, `m`, `h`, `d`). 소수 불가(`0.5s`는 오류) |
| ISO8601 | `PT0.5S`, `PT2H30M`, `P1D`, `P1DT2H30M` | 일(`D`)/시(`H`)/분(`M`)/초(`S`, 소수 가능) |

복합 기간이나 소수 초는 ISO8601 표기를 쓴다. `1w`, `P1M`(월), `P1Y`(년)처럼 길이가 일정하지 않은 달력
단위는 지원하지 않으며, 주 단위는 `7d`로 쓴다.

#### 시각(timestamp)

```text
YYYY-MM-DDThh:mm[:ss[.fff]][Z | ±hh:mm]
```

- **시간대(`Z` 또는 `±hh:mm`)를 명시하기를 권장한다.** 시간대가 없으면 *표현식을 평가하는 JVM의 기본
  시간대*로 해석한다. 예를 들어 KST 환경에서 `2024-01-01T09:00:00`은 `2024-01-01T00:00:00Z`가 된다.
- 날짜만 쓴 형태(`2024-01-01`)는 문법상으로는 통과하지만 현재 구현에서는 평가 단계에서
  `DateTimeParseException`이 발생한다. 하루 전체를 원하면 `2024-01-01T00:00:00Z~2024-01-02T00:00:00Z`처럼
  시각까지 쓴다.
- 타임존 오프셋의 `+hh:mm`과 개수 범위의 `+N`은 충돌하지 않는다.
  `2024-01-01T09:00:00+09:00+100`은 "KST 09시부터 100개"로 해석된다.

#### 컬럼 선택(projection)

- `|` 뒤에 컬럼 이름을 `,`로 나열한다. 생략하면 전체 컬럼을 조회한다. 범위(`#`)는 컬럼 목록 앞에 쓴다.
- 시각 컬럼 `Time`은 지정하지 않아도 항상 맨 앞에 포함된다.
- 컬럼 이름에도 [3.3절](#33-이름식별자과-따옴표)의 따옴표 규칙이 적용된다.

---

## 7. 값 리터럴

`ElementValues.parseExpr(String)`(또는 `MDTExpressionParser.parseValueLiteral(text).evaluate()`)으로
파싱하며, 결과는 `ElementValue`이다. 요소 값을 갱신하거나 연산 입력에 상수를 넘길 때 쓴다.

| 리터럴 | 예 | 결과 타입 |
|--------|----|-----------|
| 정수 | `42`, `-5` | `PropertyValue` (`xs:int`) |
| 실수 | `3.14`, `-0.5` | `PropertyValue` (`xs:double`) |
| 불리언 | `true`, `false` | `PropertyValue` (`xs:boolean`) |
| 문자열 | `abc`, `'hello world'`, `"정상"`, `"2024-01-01"` | `PropertyValue` (`xs:string`) |
| 다국어 문자열 | `'안녕'@ko`, `'hello'@en` | `MultiLanguagePropertyValue` (언어·텍스트 한 쌍) |
| 파일 | `file:'/tmp/a.txt'('text/plain')` | `FileValue` (경로, MIME 타입) |
| 범위 | `[0, 100]`, `[0.5, 1.5]`, `['a', 'z']` | `RangeValue` (최솟값 타입 기준) |

- 따옴표 없는 식별자(`abc`)도 문자열 리터럴이 된다. 공백이나 특수문자가 있으면 따옴표가 필요하다.
- 날짜·시각 값은 리터럴 타입이 따로 없으므로 따옴표로 감싼 문자열로 넘긴다(`'2024-01-01T09:00:00Z'`).
- 정수는 `xs:int`, 실수는 `xs:double`로만 만들어진다. `long`/`float` 등 다른 타입이 필요하면
  `PropertyValue.LONG(...)`처럼 Java API로 직접 만든다.
- `[1, 10]`은 배열이 아니라 최솟값·최댓값이다. 두 끝값의 타입을 맞춰 쓰며, 임의의 JSON 배열·객체 리터럴은
  이 문법에 없다.
- 파일 리터럴을 평가하면 경로와 MIME 타입만 만들어진다. 파일 내용을 읽거나 서버에 올리지 않는다.
- `null`은 값 리터럴이 아니라 범용 표현식의 별도 종류다. `parseExpr("null").evaluate()`는 Java `null`을
  돌려주지만 `parseValueLiteral("null")`은 실패하며, 요소 삭제나 값 초기화를 뜻하지도 않는다. 문자열
  `"null"`이 필요하면 `'null'`을 쓴다.

```java
import mdt.model.expr.MDTExpressionParser;
import mdt.model.sm.value.ElementValue;

ElementValue temperature = MDTExpressionParser.parseValueLiteral("23.5").evaluate();
ElementValue state = MDTExpressionParser.parseValueLiteral("'정상'@ko").evaluate();
ElementValue bounds = MDTExpressionParser.parseValueLiteral("[-10.0, 80.0]").evaluate();
```

---

## 8. 범용 표현식 `parseExpr`

`MDTExpressionParser.parseExpr(String)`은 입력이 어떤 종류인지 모를 때 쓰는 진입점이다. 다음 다섯 가지를
모두 받아 들이며, 결과 `MDTExpression`의 클래스를 보고 분기한다.

| 입력 예 | 반환 Expr | `evaluate()` 결과 |
|---------|-----------|-------------------|
| `42`, `'abc'`, `abc` | `LiteralExpr` | `ElementValue` |
| `line-01:Data` | `MDTSubmodelExpr` | `DefaultSubmodelReference` |
| `line-01:Data:A.B`, `param:line-01:P`, ... | `MDTElementReferenceExpr` | `MDTElementReference` |
| `line-01:Data:A = 10` | `AssignmentExpr` | (아래 8.1 참고) |
| `null` | `NullExpr` | `null` |

`:`가 없는 단일 이름(`abc`)은 문자열 리터럴로, `a:b`는 Submodel 참조로, `a:b:c`는 요소 참조로 해석된다는
점에 유의한다. 시계열 범위(`last=10m`)처럼 개별 파서 전용 입력은 `parseExpr`가 받지 않는다.

```java
MDTExpression expr = MDTExpressionParser.parseExpr(input);
if ( expr instanceof MDTElementReferenceExpr refExpr ) {
	MDTElementReference ref = refExpr.evaluate();
	ref.activate(manager);
	// ... 요소 처리
}
else if ( expr instanceof MDTSubmodelExpr smExpr ) {
	DefaultSubmodelReference smRef = smExpr.evaluate();
	smRef.activate(manager);
	// ... Submodel 처리
}
else if ( expr instanceof LiteralExpr litExpr ) {
	ElementValue value = litExpr.evaluate();
	// ... 값 처리
}
```

MDT Manager의 REST 핸들러(`MDTInstanceManagerController`)가 이 패턴으로 `ref` 파라미터를 처리한다.

### 8.1 대입 표현식

```text
<요소 참조> = <값 리터럴>
<요소 참조> = <요소 참조>
```

```text
line-01:Data:DataInfo.Equipment.Speed = 10
oparg:line-01:Simulation:in:Speed = param:line-01:Speed
```

대입을 파싱하면 `AssignmentExpr`가 만들어진다. 다른 참조 표현식과 달리, `activate(MDTInstanceManager)`를
먼저 호출한 뒤 `evaluate()`하면 오른쪽 값을 실제로 읽어 왼쪽 요소에 기록하고 왼쪽 참조를 반환한다.

```java
import mdt.model.expr.AssignmentExpr;

AssignmentExpr assignment = (AssignmentExpr)MDTExpressionParser.parseExpr(
        "oparg:line-01:Simulation:in:Speed = param:line-01:Speed");
assignment.activate(manager);
assignment.evaluate();
```

> **현재 제약:** 오른쪽이 값 리터럴인 경우 평가 결과가 왼쪽에 반영되지 않는다(구현상 리터럴 값이
> 버려진다). 따라서 `param:line-01:SetPoint = 25.0`을 `AssignmentExpr.evaluate()`로 실행하는 코드는 쓰지
> 않는다. 리터럴 대입이 필요하면 `ref.updateValue(ElementValues.parseExpr(...))`를 직접 호출하거나
> `SetTask`([12.6절](#126-java-settask로-값-설정))를 쓴다. CLI의 `mdt run set`은 `SetTask`를 쓰므로 리터럴
> 설정에도 문제가 없다.

---

## 9. 파서 API 정리

`MDTExpressionParser`의 정적 메서드는 각각 문법 규칙 하나에 대응한다. 일반 코드에서는 오른쪽 열의
파사드를 쓰는 편이 간단하다.

| `MDTExpressionParser` 메서드 | 받는 형태 | 반환 Expr → `evaluate()` | 파사드 |
|------------------------------|-----------|---------------------------|--------|
| `parseElementReference` | 요소 참조 (6장) | `MDTElementReferenceExpr` → `MDTElementReference` | `ElementReferences.parseExpr` |
| `parseSubmodelReference` | Submodel 참조 (4장) | `MDTSubmodelExpr` → `DefaultSubmodelReference` | `ElementReferences.parseSubmodelReference`, `DefaultSubmodelReference.parseStringExpr` |
| `parseValueLiteral` | 값 리터럴 (7장) | `LiteralExpr` → `ElementValue` | `ElementValues.parseExpr` |
| `parseTimeSeriesRange` | 시계열 범위 (`last=5`, `from~to` ...) | `TimeSeriesRangeExpr` → `TimeSeriesRange` | `TimeSeriesRange.parse` |
| `parseIdShortPath` | idShortPath (5장) | `SimpleValueExpr<IdShortPath>` → `IdShortPath` | — |
| `parseInstanceReference` | 인스턴스 id (`line-01`, `mdt:` 접두사 없이) | `StringExpr` → `String` | — |
| `parseExpr` | 위의 것 전부 + 대입 + `null` (8장) | `MDTExpression` | `TaskArgument.parseTaskArgument` (참조/리터럴) |

```java
TimeSeriesRange range = TimeSeriesRange.parse("last=1h@now");
IdShortPath path = MDTExpressionParser.parseIdShortPath("A.B[2].C").evaluate();
DefaultSubmodelReference smRef = ElementReferences.parseSubmodelReference("line-01:Simulation");
```

---

## 10. 참조 사용하기

### 10.1 활성화

파싱 직후의 `MDTElementReference`/`DefaultSubmodelReference`는 좌표(인스턴스 id, Submodel, 경로)만 가진
**비활성 상태**다. 읽기/쓰기 전에 반드시 활성화한다.

```java
MDTElementReference ref = ElementReferences.parseExpr("oparg:line-01:Simulation:out:Result");
ref.activate(manager);     // 또는 ElementReferences.activate(ref, manager)
```

활성화 전에 `getInstance()`, `getSubmodelService()`를 호출하면 `IllegalStateException`이 발생한다.
`param:`/`oparg:`처럼 실제 경로가 활성화 때 정해지는 참조는 `getIdShortPathString()`도 마찬가지다.

### 10.2 읽기 / 쓰기

```java
SubmodelElement sme   = ref.read();          // 요소 전체
ElementValue    value = ref.readValue();     // 값만
String  s = ref.readAsString();              // readAsInt / readAsLong / readAsFloat / readAsBoolean
Instant t = ref.readAsDateTime();            // readAsDuration ...

ref.updateValue(ElementValues.parseExpr("42"));    // 값 갱신 (요소 타입과 호환되는 값이어야 한다)
ref.write(newElement);                             // 요소 전체 교체
```

`updateValue()`는 기존 요소의 값만 갱신하고, `write()`는 요소 전체를 교체한다. 둘을 구분해서 쓴다.

### 10.3 JSON 직렬화

참조 객체는 JSON으로도 직렬화된다. 표현식 문자열과 JSON 중 상황에 맞는 쪽을 쓰면 된다.

```java
String json = ref.toJsonString();                          // {"@type":"mdt:ref:param", ...}
ElementReference back = ElementReferences.parseJsonString(json);   // 역직렬화 직후에도 비활성 상태
back.activate(manager);
```

자세한 API는 [`mdt.model.sm.ref` README](../src/main/api/mdt/model/sm/ref/README.md)를 참고한다.

---

## 11. 참조 → 표현식 (`toStringExpr`)과 정규화

모든 참조 객체는 `toStringExpr()`로 표현식 문자열을 다시 만들 수 있다. 출력은 입력과 다를 수 있으며
다음과 같이 **정규화**된다.

| 입력 | `toStringExpr()` |
|------|------------------|
| `line-01:idShort=Data:A` | `line-01:Data:A` |
| `timeseries:line-01:Log#last=10s` | `timeseries:line-01:Log#last=PT10S@latest` |
| `timeseries:line-01:Log#last=1d` | `timeseries:line-01:Log#last=PT24H@latest` |
| `timeseries:line-01:Log#2024-01-01T09:00:00+09:00+100` | `timeseries:line-01:Log#2024-01-01T00:00:00Z+100` |
| `param:line-01:'Speed'` | `param:line-01:Speed` |

> **주의:** 현재 `toStringExpr()`는 따옴표를 복원하지 않는다. 따옴표가 필요한 이름(공백, URL 형태의
> Submodel id 등)이 들어간 참조는 `toStringExpr()` 결과를 다시 파싱하면 실패한다.
> 예: `submodel:id='http://x/sm/Data':A` → `submodel:id=http://x/sm/Data:A`(재파싱 불가).
> 이런 참조를 저장하거나 전달할 때는 JSON 직렬화(`toJsonString()`)를 쓴다.

---

## 12. 활용 예제

### 12.1 CLI (`mdt`)

`mdt-cli`의 `sbin/mdt`가 `mdt`라는 이름으로 실행 가능하고 접속 설정이 준비되어 있다고 가정한 Bash
예제다. 이 절의 `#` 주석은 Bash 주석이며 MdtExpr 문법에는 주석이 없다.

```bash
# 요소 조회
mdt get element --ref 'param:line-01:Temperature' -o value
mdt get element --ref 'line-01:Data:DataInfo.Equipment' -o json
mdt get element --ref 'timeseries:line-01:Log#last=10|current,power' -o json

# 파일 요소
mdt get file    'param:line-01:Manual' -f ./manual.pdf
mdt set file    'param:line-01:Manual' ./manual.pdf --mimeType application/pdf
mdt remove file 'param:line-01:Manual'

# 연산 실행: 입력은 참조 또는 리터럴, 출력은 참조
mdt run submodel line-01 Simulation --in.Speed 'param:line-01:Speed' --out.Result 'param:line-01:SimResult'
mdt run operation line-01 Ops predict --in.x 3 --out.y 'param:line-01:result' --showResult

# 값 설정/복사 (SetTask 사용 — 리터럴 대입 가능)
mdt run set --in.source 25.3 --out.target 'param:line-01:Temperature'
mdt run set --in.source 'param:line-01:Speed' --out.target 'oparg:line-01:Simulation:in:Speed'

# 숫자 25가 아니라 문자열 "25"를 전달: 셸 큰따옴표 안에 MdtExpr 작은따옴표
mdt run set --in.source "'25'" --out.target 'line-01:Data:Label'

# 참조를 REST URL로 해석
mdt resolve reference 'param:line-01:Temperature'
```

셸에서는 표현식을 **항상 작은따옴표로 감싸는 것**이 안전하다. `|`(파이프), `*`(glob), `'`/`"`(따옴표),
`#`(주석), 공백 등이 셸에서 특별한 의미를 갖기 때문이다. 셸의 바깥 따옴표는 인자를 전달할 때 제거되므로,
MdtExpr 쪽에 따옴표를 전달하려면 위 마지막 예처럼 셸 따옴표 안에 다른 종류의 따옴표를 넣는다.

```bash
mdt get element --ref 'line-01:Data:Items."Item 1".Value' -o value
mdt get element --ref 'param:line-01:*' -o json
```

`--in.<name>` 값은 먼저 요소 참조로 파싱해 보고, 참조가 아니면 리터럴로 처리한다. 따라서 `3`, `25.3`,
`hello`는 리터럴, `param:line-01:Speed`는 참조가 된다.

### 12.2 REST API (MDT Manager)

MDT Manager(기본 `http://localhost:12985`)의 `/instance-manager` 아래 엔드포인트는 `ref` 쿼리 파라미터로
MdtExpr를 받는다. 표현식에는 `:`, `|`, `#`, `+`, `'` 등이 들어가므로 **반드시 URL 인코딩**한다.

| 메서드 & 경로 | 동작 |
|---------------|------|
| `GET /instance-manager/submodel-element?ref=...` | 요소(또는 Submodel) JSON 조회 |
| `GET /instance-manager/submodel-element/$value?ref=...` | 요소 값(value-only) 조회 |
| `PATCH /instance-manager/submodel-element/$value?ref=...` | 요소 값 갱신 |
| `GET /instance-manager/references/$url?ref=...` | 참조를 접근 URL로 해석 |
| `GET /instance-manager/references/$json?ref=...` | 참조의 JSON 직렬화 결과 |

```bash
curl -G 'http://localhost:12985/instance-manager/submodel-element/$value' \
     --data-urlencode 'ref=param:line-01:Temperature'

curl -G 'http://localhost:12985/instance-manager/submodel-element' \
     --data-urlencode 'ref=timeseries:line-01:Log#last=1h@now|current'
```

`curl -G --data-urlencode`를 쓰면 인코딩을 curl이 처리한다. 특히 `#`을 인코딩하지 않으면 URL fragment로
잘려 서버에 전달되지 않는다.

표현식에 오류가 있으면 `RESTfulErrorEntity` 본문과 함께 오류 응답이 온다. 현재 구현에서는 후행 토큰
오류(`IllegalArgumentException`)는 `400`, 문법 오류(`MDTParserException`)는 `500`으로 응답한다.

### 12.3 Java: 파라미터 읽고 쓰기

```java
MDTElementReference speed = ElementReferences.parseExpr("param:line-01:Speed");
speed.activate(manager);

Integer current = speed.readAsInt();
speed.updateValue(ElementValues.parseExpr(String.valueOf(current + 10)));

// 파라미터 전체
MDTElementReference all = ElementReferences.parseExpr("param:line-01:*");
all.activate(manager);
SubmodelElement params = all.read();
```

### 12.4 Java: 설비 값을 시뮬레이션 입력으로 복사

```java
MDTElementReference source = ElementReferences.parseExpr("param:line-01:Speed");
MDTElementReference target = ElementReferences.parseExpr("oparg:line-01:Simulation:in:Speed");

source.activate(manager);
target.activate(manager);
target.updateValue(source.readValue());
```

이 작업은 입력값을 준비할 뿐이다. 시뮬레이션 실행은 별도의 연산 실행 API(`SimulationSubmodelService.run`)나
CLI 명령(`mdt run submodel`)으로 수행한다.

### 12.5 Java: 시계열 조회

```java
String expr = "timeseries:line-01:Log#last=30m@now|current,power";
MDTElementReference ref = ElementReferences.parseExpr(expr);
ref.activate(manager);

SubmodelElementCollection records = ref.readCollection();   // Time, current, power 컬럼
System.out.println(ref.readValue().toValueJsonString());
```

범위를 프로그램에서 만들 때는 문자열을 이어 붙이기보다 `TimeSeriesRecordsReference.builder()`와
`TimeSeriesRange` 팩토리(`TimeSeriesRange.length(n)`, `last(duration, anchor)`, `between(from, to)`,
`since(from, n)`)를 쓰는 편이 안전하다.

### 12.6 Java: `SetTask`로 값 설정

대입 표현식의 리터럴 제약([8.1절](#81-대입-표현식))을 피하면서 리터럴이나 참조를 대상에 기록하려면
`SetTask`를 쓴다. CLI의 `mdt run set`이 이 태스크를 실행한다.

```java
import mdt.task.SetTask;
import mdt.task.TaskArgument;

SetTask task = SetTask.builder()
        .source(TaskArgument.parseTaskArgument("25.0"))          // 리터럴 또는 참조
        .target(TaskArgument.reference("param:line-01:SetPoint"))
        .build();
task.run(manager);
```

### 12.7 Java: 연산 인자 읽기

```java
// 시뮬레이션 결과(출력 인자 전체)
MDTElementReference outputs = ElementReferences.parseExpr("oparg:line-01:Simulation:out:*");
outputs.activate(manager);
SubmodelElementList list = outputs.readList();
for ( SubmodelElement arg : list.getValue() ) {
	System.out.println(arg.getIdShort() + " = " + ElementValues.getValue(arg));
}
```

### 12.8 Java: 사용자 입력을 참조/리터럴로 분기

```java
// 참조이면 ReferenceArgument, 리터럴이면 LiteralArgument
TaskArgument a1 = TaskArgument.parseTaskArgument("param:line-01:Speed");   // reference
TaskArgument a2 = TaskArgument.parseTaskArgument("25.3");                  // literal(xs:double)
TaskArgument a3 = TaskArgument.parseTaskArgument("hello");                 // literal(xs:string)

// 명시적으로 만들 때
TaskArgument ref = TaskArgument.reference("oparg:line-01:Simulation:out:Result");
TaskArgument lit = TaskArgument.literal(25.3);
```

`parseTaskArgument`는 Submodel 참조(`line-01:Data`)나 `null`처럼 요소 참조도 리터럴도 아닌 입력, 그리고
일부 파싱 실패를 문자열 리터럴로 취급한다. 입력 검증 용도로 쓰면 오타가 문자열로 넘어갈 수 있으므로,
참조가 꼭 필요한 자리에는 `TaskArgument.reference` 또는 `ElementReferences.parseExpr`를 쓴다.

### 12.9 워크플로 기술자(JSON)

워크플로 태스크 인자에서 참조는 `reference` 필드에 MdtExpr 문자열로 기록한다.

```json
{
  "@type": "mdt:task_arg:reference",
  "id": "speed",
  "reference": "param:line-01:Speed"
}
```

태스크 실행 시 `ElementReferences.parseExpr()`로 파싱되고 워크플로 엔진의 `MDTInstanceManager`로
활성화된다.

---

## 13. 오류 처리

파서는 입력 전체를 검사한다. 유효한 표현식 뒤에 불필요한 토큰이 남아도 실패한다.

| 상황 | 예외 | 발생 시점 |
|------|------|-----------|
| 문법 오류, 인식할 수 없는 문자 | `MDTParserException` (`RuntimeException`) | 파싱 |
| 규칙이 끝났는데 입력이 남음 (후행 토큰) | `IllegalArgumentException` | 파싱 |
| 정수 리터럴이 `int` 범위 초과 | `NumberFormatException` | 파싱 |
| 시각 형식 오류(날짜만 쓴 경우 포함) | `DateTimeParseException` | `evaluate()` |
| 활성화 전 인스턴스/Submodel 접근 | `IllegalStateException` | 사용 |
| 인스턴스·Submodel·파라미터·인자를 찾을 수 없음 | `ResourceNotFoundException` | 활성화 / 사용 |
| `param:`을 지원하지 않는 asset type, Simulation/AI가 아닌 Submodel에 `oparg:` | `IllegalArgumentException`, `ModelValidationException` | 활성화 / 사용 |
| 시계열 참조에 쓰기 | `UnsupportedOperationException` | 사용 |

**"참조이면 참조로, 아니면 리터럴로"** 처리하는 코드를 직접 짤 때는 `MDTParserException`만 잡으면
안 된다. 후행 토큰 오류는 `IllegalArgumentException`으로 나오기 때문이다(예: `hello world`는 `hello`까지
파싱된 뒤 `world`가 남는다). 두 예외를 모두 잡는다.

```java
try {
	return TaskArgument.reference(ElementReferences.parseExpr(input));
}
catch ( MDTParserException | IllegalArgumentException notAReference ) {
	return TaskArgument.literal(input);
}
```

---

## 14. 자주 하는 실수

| 증상 | 원인 | 해결 |
|------|------|------|
| `submodel:Data:...`가 엉뚱한 인스턴스를 찾음 | `submodel`이 인스턴스 id로 해석됨 | id 기반은 `submodel:id='...'` |
| URL 형태 Submodel id에서 `token recognition error at: '/'` | 따옴표 누락 | `submodel:id='http://...'` |
| idShort `1stAxis`, `s`, `true`에서 파싱 오류 | 숫자로 시작 / 예약 토큰 | `'1stAxis'`, `'s'`, `'true'` |
| `line-01:Data:Samples[-1]` 오류 | 인덱스는 0 이상 정수만 | `Samples[0]` 등 |
| `timeseries:inst:Log:Tail#last=5` 실패 | 시계열은 Submodel 단위 | `timeseries:inst:Log#last=5` |
| `last=10@now` 오류 | 개수 범위에는 anchor 불가 | 기간이면 `last=10s@now` |
| `last=0.5s` 오류 | 단순형 기간은 정수만 | `last=PT0.5S` 또는 `last=500ms` |
| `#2024-01-01~2024-02-01`이 평가 단계에서 실패 | 날짜만 쓴 시각은 현재 미지원 | `#2024-01-01T00:00:00Z~2024-02-01T00:00:00Z` |
| 시간 범위가 9시간 어긋남 | 시간대 없는 시각은 JVM 기본 시간대로 해석 | `Z` 또는 `+09:00`을 명시 |
| `last=1w`, `last=P1M` 오류 | 주/월/년 단위 미지원 | `last=7d`, `last=P30D` |
| `oparg:...:inout:0` 오류 | `oparg`는 `in`/`out`만 지원 | AAS Operation 변수라면 `opvar`의 `inout` |
| `opvar:...:in:x` 오류 | `opvar`는 번호로만 지정 | `opvar:...:in:0` |
| `oparg:...:in:'Speed'`가 인자를 못 찾음 | `oparg` 인자 이름은 따옴표가 보존됨 | `oparg:...:in:Speed` 또는 인덱스 |
| `parseValueLiteral("null")` 실패 | `null`은 값 리터럴이 아님 | Java `null`은 `parseExpr("null")`, 문자열은 `'null'` |
| 활성화 전 읽기에서 `IllegalStateException` | 참조가 비활성 상태 | `ref.activate(manager)` 후 읽기 |
| 파싱은 됐는데 대상을 찾지 못함 | 인스턴스·Submodel·파라미터 id 또는 모델 구조 불일치 | 식별자와 모델 구조 확인 |
| REST 호출에서 `#` 이후가 사라짐 | URL fragment로 해석됨 | `--data-urlencode` 등으로 인코딩 |
| 셸에서 `\|` 뒤가 다른 명령으로 실행됨 | 셸 파이프 | 표현식 전체를 작은따옴표로 감쌈 |
| `toStringExpr()` 결과를 다시 파싱하면 실패 | 따옴표가 복원되지 않음 | JSON 직렬화 사용 ([11장](#11-참조--표현식-tostringexpr과-정규화)) |

---

## 부록 A. 문법 요약

`MdtExpr.g4`를 읽기 쉽게 줄인 것이다. `'...'`는 리터럴 토큰, `?`/`*`는 생략 가능/반복이다.

```text
expr                := valueLiteral | submodelSpec | elementSpec | assignment | 'null'
assignment          := elementSpec '=' ( valueLiteral | elementSpec )

submodelSpec        := name ':' ( 'idShort' '=' )? name
                     | 'submodel' ':' 'id' '=' name

elementSpec         := 'param'      ':' name ':' ( idShortPath | INTEGER | '*' )
                     | 'oparg'      ':' submodelSpec ':' ( 'in' | 'out' ) ':' ( name | INTEGER | '*' )
                     | 'opvar'      ':' submodelSpec ':' idShortPath ':' ( 'in' | 'out' | 'inout' ) ':' INTEGER
                     | 'timeseries' ':' submodelSpec ( '#' tsRange )? ( '|' name ( ',' name )* )?
                     | submodelSpec ':' idShortPath

idShortPath         := name ( '.' name | '[' INTEGER ']' )*

tsRange             := 'last' '=' INTEGER
                     | 'last' '=' duration ( '@' ( 'now' | 'latest' ) )?
                     | TIMESTAMP '~' TIMESTAMP | TIMESTAMP '~' | '~' TIMESTAMP
                     | TIMESTAMP '+' INTEGER
duration            := INTEGER ( 'ms' | 's' | 'm' | 'h' | 'd' ) | ISO8601_DURATION

valueLiteral        := ID | STRING | '-'? INTEGER | '-'? FLOAT | BOOLEAN
                     | STRING '@' ID                          (다국어 문자열)
                     | 'file' ':' STRING '(' STRING ')'       (파일: 경로, MIME 타입)
                     | '[' valueLiteral ',' valueLiteral ']'  (범위; 단순 값만)

name                := ID | STRING | soft-keyword
soft-keyword        := 'mdt' | 'idShort' | 'submodel' | 'id' | 'param' | 'oparg' | 'opvar'
                     | 'timeseries' | 'in' | 'out' | 'inout' | 'last' | 'now' | 'latest' | 'file'

ID                  := [a-zA-Z_] [a-zA-Z0-9_-]*
STRING              := '"' ... '"' | "'" ... "'"              (escape 없음)
INTEGER             := [0-9]+
FLOAT               := [0-9]+ '.' [0-9]+
BOOLEAN             := 'true' | 'false'
TIMESTAMP           := YYYY-MM-DD ( 'T' hh:mm ( ':' ss ( '.' f+ )? )? ( 'Z' | ('+'|'-') hh:mm )? )?
ISO8601_DURATION    := 'P' n 'D' ( 'T' time )? | 'PT' time      (time: nH nM n[.n]S 조합)
```

문법을 수정하려면 `.g4`를 고친 뒤 파서를 재생성해야 한다. 절차는
[`mdt.model.sm.ref` CLAUDE.md](../src/main/api/mdt/model/sm/ref/CLAUDE.md)의 "문법 수정 워크플로"를 따른다.

---

## 부록 B. 관련 구현과 문서

- [표현식 문법 `MdtExpr.g4`](../src/main/antlr/MdtExpr.g4)
- [`MDTExpressionParser`](../src/main/java/mdt/model/expr/MDTExpressionParser.java) — 파서 파사드
- [`MDTExpressionVisitor`](../src/main/java/mdt/model/expr/MDTExpressionVisitor.java) — 파스 트리 → `*Expr` 변환
- [`AssignmentExpr`](../src/main/java/mdt/model/expr/AssignmentExpr.java) — 대입 평가 구현
- [`mdt.model.sm.ref` README](../src/main/api/mdt/model/sm/ref/README.md) — 요소 참조 패키지 안내
- [`TimeSeriesRange`](../src/main/api/mdt/model/sm/ref/timeseries/TimeSeriesRange.java) — 시계열 범위 모델
- [`TaskArgument`](../src/main/api/mdt/task/TaskArgument.java) — 태스크 인자 구현
- [`SetTask`](../src/main/java/mdt/task/SetTask.java) — 값 설정 태스크
