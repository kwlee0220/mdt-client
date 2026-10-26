# mdt-client: mdt.model 패키지 코드 해설

Oct 15, 2026 · @Kang-Woo Lee

## 개요

`src/main/api/mdt/model`은 mdt-client의 **공개 API 계층**이다. 최상위 진입점 `MDTManager`에서 출발해 MDTInstance, Submodel, SubmodelElement 값까지 내려가는 인터페이스와 값 타입 148개(Java 파일, 약 13,000줄)를 담고 있다.

- **위치의 의미**: `build.gradle`의 main 소스셋은 `src/main/java`, `src/main/api`, `src/main/antlr` 세 디렉터리를 하나로 합친다. 따라서 `mdt.model` 패키지는 세 곳에 걸쳐 있고, `api` 쪽은 "호출자가 의존하는 계약", `java` 쪽은 그 구현(`AASUtils`, `DefaultInput`, `MDTModelSerDe` 등)이다.
- **기반 모델**: AAS 메타모델은 Eclipse `aas4j`(`org.eclipse.digitaltwin.aas4j.v3.model`)를 그대로 쓰고, 이 패키지는 그 위에 MDT 고유의 개념(인스턴스, 파라미터, 연산 인자, 시계열 참조)을 얹는다.
- **외부 의존**: `MDTManager`는 `mdt.aas.SubmodelRegistry`와 `mdt.workflow.WorkflowManager`를 반환하므로 이 둘은 `mdt.model` 밖에 있다. 공용 유틸리티(`utils.KeyValue`, `utils.io.TempFile` 등)는 형제 프로젝트 `:utils`에서 온다.

## 패키지 구조

코드의 약 절반(49%)은 `sm.ref`(요소 참조)과 `sm.value`(요소 값) 두 패키지에 있다. 나머지는 진입점·인스턴스 관리와, MDT 표준 서브모델(데이터·정보모델·AI·시뮬레이션·시계열)을 표현하는 도메인 인터페이스다.

| 패키지 | 파일 | 줄 수 | 역할 | 대표 타입 |
| --- | --- | --- | --- | --- |
| `mdt.model.sm.ref` (+`timeseries`) | 17 | 3,921 | SubmodelElement에 대한 저장 위치 독립적 참조와 MdtExpr 표현식 | `ElementReference`, `ElementReferences`, `MDTParameterReference` |
| `mdt.model.sm.value` | 13 | 2,570 | SubmodelElement의 값(value-only) 표현과 JSON 변환 | `ElementValue`, `PropertyValue`, `ElementValues` |
| `mdt.model.instance` | 15 | 1,958 | MDTInstance의 조회·등록·생명주기와 디스크립터 | `MDTInstanceManager`, `MDTInstance`, `MDTInstanceStatus` |
| `mdt.model.sm.entity` | 15 | 1,213 | 어노테이션 기반 Java 객체 ↔ SubmodelElement 매핑 | `SubmodelEntity`, `AbstractSMEContainerEntity`, `@PropertyField` |
| `mdt.model.sm.data` | 32 | 1,116 | `Data` 서브모델: 설비·공정, 파라미터, 생산 도메인 타입 | `Data`, `Equipment`, `Parameter`, `ParameterValue` |
| `mdt.model` (루트) | 19 | 809 | 최상위 진입점, AAS/Submodel 서비스, 예외 계층, semantic ID | `MDTManager`, `SubmodelService`, `MDTException` |
| `mdt.model.timeseries` | 12 | 505 | 시계열 서브모델(메타데이터·세그먼트·레코드) | `TimeSeries`, `Segment`, `Record` |
| `mdt.model.sm` (루트) | 4 | 335 | 연산형 서브모델 서비스와 파일 표현 | `OperationSubmodelService`, `AASFile` |
| `mdt.model.sm.info` | 9 | 226 | `InformationModel` 서브모델: 자산 정보와 트윈 구성 | `InformationModel`, `MDTInfo`, `TwinComposition` |
| `mdt.model.sm.simulation` | 6 | 139 | `Simulation` 서브모델 | `Simulation`, `SimulationSubmodelService` |
| `mdt.model.sm.ai` | 4 | 86 | `AI` 서브모델 | `AI`, `AISubmodelService` |
| `mdt.model.sm.shape` | 2 | 30 | `Shape` 서브모델 | `Shape`, `ShapeInfo` |

구현 클래스는 같은 패키지명으로 `src/main/java/mdt/model/…`에 있다. 표현식 파서(`mdt.model.expr`)는 `src/main/java`와 `src/main/antlr`에만 있고 `api`에는 없다.

## 핵심 타입 (루트 패키지 `mdt.model`)

루트 패키지는 시스템 진입점 `MDTManager`, AAS 표준 서비스 두 개, 예외 계층, semantic ID 상수를 정의한다. 하위 패키지는 모두 이 타입들을 기준으로 엮인다.

### MDTManager — 시스템 진입점

세 하위 관리자와 범용 서비스 조회를 제공하는 최상위 인터페이스다. HTTP 구현은 `mdt.client.HttpMDTManager`다.

| 메서드 | 반환 | 설명 |
| --- | --- | --- |
| `getInstanceManager()` | `MDTInstanceManager` | MDTInstance 조회·등록·제어 |
| `getSubmodelRegistry()` | `mdt.aas.SubmodelRegistry` | Submodel 디스크립터 레지스트리 |
| `getWorkflowManager()` | `mdt.workflow.WorkflowManager` | 워크플로우 관리 |
| `getService(Class<T>)` | `T` | 클래스로 임의 서비스 조회, 없으면 `null` 가능 |

### SubmodelService — Submodel 하나를 다루는 서비스

AAS Part 2(API)의 Submodel 서비스에 대응하며, 327줄로 루트 패키지에서 가장 크다. 대상 요소는 모두 **idShortPath**(`a.b[0].c` 형식)로 지정한다.

- **조회 수준**: `Modifier` 열거형(`NONE`, `METADATA`, `VALUE`)으로 전체·메타데이터·값만 중 하나를 고른다. 인자 없는 오버로드는 `NONE`을 쓴다.
- **Submodel 전체**: `getSubmodel`, `putSubmodel`, `getAllSubmodelElements`.
- **요소 CRUD**: `getSubmodelElementByPath`, `addSubmodelElement`, `addSubmodelElementByPath`, `setSubmodelElementByPath`(요소 교체), `updateSubmodelElementByPath`(값만 갱신), `deleteSubmodelElementByPath`.
- **값 단위 접근**: `getSubmodelElementValueByPath(path, prototype)`은 `ElementValue`를 돌려주고, `updateSubmodelElementValueByPath(path, json)`은 value-only JSON 문자열로 값을 바꾼다.
- **첨부파일(File 요소)**: `getAttachmentByPath`(OutputStream으로 받기), `getAASFileByPath`(기본 메서드, `TempFile`로 받기), `putAttachmentByPath`(InputStream 또는 `java.io.File`), `deleteAttachmentByPath`.
- **Operation 실행**: `invokeOperationSync`, `invokeOperationAsync` → `OperationHandle`, `getOperationAsyncStatus`, `getOperationAsyncResult`. 기본 메서드 `runOperation(path, in, inout, timeout, pollInterval)`은 비동기로 시작한 뒤 `pollInterval`마다 상태를 확인해 끝나면 결과를 반환한다. `timeout`이 `null`이면 7일(`P7D`)을 쓰고, 상태가 `TIMEOUT`이면 `TimeoutException`, `CANCELED`이면 `CancellationException`을 던진다.

### AssetAdministrationShellService — AAS 셸 하나를 다루는 서비스

AAS 객체 조회·설정(`get/putAssetAdministrationShell`), 서브모델 참조 목록 관리(`getAllSubmodelReferences`, `postSubmodelReference`, `deleteSubmodelReference`), `AssetInformation`과 썸네일(`Resource`) 조회·설정·삭제를 제공한다.

### MDTSemanticIds — semantic ID 상수

MDT 표준 서브모델과 요소를 식별하는 URI 상수 모음이다. 모두 `https://etri.re.kr/mdt` + 경로 + `/1/1` 형식이다.

- 서브모델: `SUBMODEL_INFO`(`/Submodel/InformationModel`), `SUBMODEL_DATA`(`/Submodel/Data`), `SUBMODEL_AI`, `SUBMODEL_SIMULATION`, `SUBMODEL_SHAPE`.
- 요소: `MDT_INFO`, `TWIN_COMPOSITION`, `COMPOSITION_ITEM`, `COMPOSITION_DEPENDENCY`, `EQUIPMENT`, `OPERATION`, `PARAMETER`, `PARAMETER_VALUE`, `ARGUMENT_INPUT`, `ARGUMENT_OUTPUT`.
- 시계열 연산: `TIME_SERIES_READ_RECORDS_BY_RANGE`, `TIME_SERIES_RANGE`, `TIME_SERIES_COLUMNS`.
- `toReference(String)`은 문자열을 `EXTERNAL_REFERENCE` + `GLOBAL_REFERENCE` 키 하나짜리 aas4j `Reference`로 바꾼다. 각 도메인 인터페이스는 이를 `SEMANTIC_ID_REFERENCE` 상수로 미리 만들어 둔다.

### 예외 계층

모든 예외는 unchecked(`RuntimeException`)다.

- `MDTException`
  - `ResourceException`
    - `ResourceNotFoundException` — 정적 팩토리 `ofId`, `ofIdShort`, `ofSemanticId`. 메시지 형식 `타입(id=…)`.
    - `ResourceAlreadyExistsException`
    - `InvalidResourceStatusException` — 인스턴스 상태가 요청과 맞지 않을 때, `MDTInstanceStatus`를 메시지에 넣는다.
  - `ModelGenerationException`, `ModelValidationException`
  - `MDTInstanceManagerException` (`instance` 패키지)

### 그 밖의 루트 타입

| 타입 | 종류 | 설명 |
| --- | --- | --- |
| `Input` / `Output` | 인터페이스 | 연산 인자 한 개(ID·이름·값 `SubmodelElement`·타입). semantic ID는 `ARGUMENT_INPUT` / `ARGUMENT_OUTPUT` |
| `NameValue` | final 클래스 | `utils.KeyValue<String,String>` + `Named`. JSON `{"name":…, "value":…}` |
| `SubModelInfo` | 인터페이스 | 서브모델 공통 정보(title, creator, type, format, identifier) |
| `MinResourceRequirements` | 인터페이스 | GPU/CPU 사양, 메모리 크기·속도 (모두 문자열) |
| `MDTManagerEvent` | 클래스 | `eventType` 하나를 가진 이벤트 기반 클래스, Jackson 직렬화 |
| `MDTEntityFactory` | 인터페이스 | id로 `SubmodelElementEntity`를 만드는 팩토리 |
| `TopLevelEntity` | 인터페이스 | `AasCRUDActions`를 확장하는 마커 |

## 하위 패키지별 상세 설명

### mdt.model.instance — MDTInstance 관리

`MDTInstanceManager`가 인스턴스의 등록·조회·제거를, `MDTInstance`가 기동·정지와 AAS/Submodel 서비스 접근을 맡는다. 나머지는 JSON으로 주고받는 디스크립터와 이벤트다.

#### MDTInstanceManager

- **조회**: `getInstance(id)`, `getInstanceAll()`, `countInstances()`, `existsInstance(id)`, `getInstanceAllByFilter(filterExpr)`.
- **필터 기반 기본 메서드**: `getInstanceByAasId`, `getInstanceAllByAasIdShort`, `getInstanceAllByAssetId`, `getInstanceBySubmodelId`, `getInstanceAllBySubmodelIdShort`. 단건 조회는 0건이면 `ResourceNotFoundException`, 2건 이상이면 `MDTInstanceManagerException`을 던진다.
- **필터 문법**: `<속성> (= | !=) '<리터럴>'` 형식의 단일 조건이다. 속성은 `instance.{instanceId, status, baseEndpoint, aasId, aasIdShort, globalAssetId, assetType}`와 `submodel.{id, idShort, semanticId}`다. 예: `instance.status != 'RUNNING'`.
- **등록**: `addInstance(id, port, instanceFile)`은 인스턴스 구현 디렉터리 또는 zip 파일을 받는다. 디렉터리는 zip으로 묶어 등록하고, `port = -1`이면 자동 할당한다. `addInstance(id, file)`은 `-1`을 쓴다.
- **제거**: `removeInstance(id)`는 없는 id를 조용히 무시하고, RUNNING 인스턴스면 `InvalidResourceStatusException`을 던진다. `removeInstanceAll()`도 있다.
- **상수**: 인스턴스 구성 파일 이름 `mdt-instance-all.jar`, `model.json`, `model.aasx`, `config.json`, `mdt_cert.p12`, `mdt_global_config.json`.
- 모든 메서드는 `null` 인자를 받지 않으며 `IllegalArgumentException`을 던진다. 인스턴스 기동·정지 메서드는 이 인터페이스에 없다.

#### MDTInstance

| 범주 | 메서드 | 비고 |
| --- | --- | --- |
| 식별·속성 | `getId`, `getAasId`, `getAasIdShort`, `getGlobalAssetId`, `getAssetType`, `getInstanceDescriptor`, `getServiceEndpoint` | 앞의 다섯은 `InstanceDescriptor`에 위임하는 기본 메서드 |
| 생명주기 | `getStatus`, `isRunning`, `start(pollInterval, timeout)`, `stop(pollInterval, timeout)`, `getOutputLog` | 아래 참고 |
| AAS/Submodel 서비스 | `getAssetAdministrationShellService`, `getSubmodelServiceAll`, `getSubmodelServiceById`, `getSubmodelServiceByIdShort`, `getSubmodelServiceAllBySemanticId` | id/idShort 조회는 `FOption<SubmodelService>` 반환. idShort는 인스턴스 안에서만 유일 |
| 디스크립터 | `getAASShellDescriptor`, `getAASSubmodelDescriptorAll`, `getMDTSubmodelDescriptorAll`, `getMDTOperationDescriptorAll`, `getMDTTwinCompositionDescriptor` | 앞의 둘은 aas4j 타입. TwinComposition이 없으면 `null` |
| 파라미터 | `getParameterServiceAll`, `getParameterCollection` | `Data` 서브모델이 없으면 `ResourceNotFoundException` |
| 인스턴스 간 의존 | `getComponentInstanceAll`, `getTargetInstanceAllOfDependency(type)`, `getSourceInstanceAllOfDependency(type)` | `getComponentInstanceAll`은 `"contain"` 의존의 대상 목록 |

`start`/`stop`은 `pollInterval`이 `null`이면 요청만 보내고 곧바로 반환하므로 STARTING/STOPPING 상태가 돌아올 수 있다. `timeout`이 `null`이면 무제한 기다린다. 이미 실행 중인데 `start`하거나 실행 중이 아닌데 `stop`하면 `InvalidResourceStatusException`이다.

#### MDTInstanceStatus

7개 상태 `ADDING`, `STOPPED`, `STARTING`, `RUNNING`, `STOPPING`, `FAILED`, `REMOVED`를 갖는다. ADDING·STARTING·STOPPING은 일시 상태이고, 서비스 엔드포인트는 RUNNING에서만 유효하다.

&#91;embedded content: MDTInstanceStatus 상태 전이 · 7개 상태\]

FAILED에서도 `start()`로 다시 시작할 수 있고, REMOVED는 더 이상 어떤 상태로도 바뀌지 않는다(`MDTInstanceStatus` Javadoc 기준).

#### 디스크립터와 이벤트

| 타입 | JSON 필드 | 비고 |
| --- | --- | --- |
| `InstanceDescriptor` | `id`, `aasId`, `aasIdEncoded`, `aasIdShort`, `globalAssetId`, `assetType`, `status`, `baseEndpoint` | 전용 Serializer/Deserializer. `aasIdEncoded`는 aasId의 Base64 URL-safe 값으로, 역직렬화 때 무시한다 |
| `MDTModel` | 위 필드(−`aasIdEncoded`) + `submodels`, `parameters`, `operations`, `twinComposition` | 인스턴스 전체를 한 번에 보여주는 집계 뷰. mdt-manager 컨트롤러가 만든다 |
| `MDTSubmodelDescriptor` | `id`, `idShort`, `semanticId`, `endpoint` | `equals`/`hashCode`는 id만 비교 |
| `MDTParameterDescriptor` | `id`, `name`, `valueType`, `reference`, `endpoint` | `endpoint`는 파라미터 값의 REST 주소 |
| `MDTOperationDescriptor` | `id`, `operationType`, `inputArguments`, `outputArguments` | 인자는 `ArgumentDescriptor(id, idShortPath, valueType, reference)`. `toSignatureString()` → `id(in1, in2) -> (out1)` |
| `MDTTwinCompositionDescriptor` | `id`, `type`, `compositionItems`, `compositionDependencies` | 항목은 `(id, reference)`, 의존은 `(type, sourceItem, targetItem)` |
| `InstanceStatusChangeEvent` | `eventType`, `instanceId`, `statusChange`, `serviceEndpoint` | `statusChange`는 문자열: 7개 상태명 + `ADDED`, `ADD_FAILED`. `getInstanceStatus()`가 각각 STOPPED, REMOVED로 바꾼다 |

`InstanceDescriptor.getParameterEndpoint(idx, smEndpoint)`는 파라미터 값의 REST 경로를 만든다. 자산 타입이 `Machine`이면 `DataInfo.Equipment.EquipmentParameterValues[idx].ParameterValue`, `Process`면 `Operation`으로 바꾼 경로를 쓰고, 그 외 타입은 `IllegalStateException`이다.

#### 파라미터 서비스

- `MDTParameterService`(클래스): 파라미터 하나의 `getId`, `getDescriptor`, `getReference`, `read()` → `ElementValue`, `update(ElementValue)`. 참조는 처음 쓸 때 `MDTParameterReference.newInstance(instanceId, paramId)`로 만들고 인스턴스 관리자로 `activate`한다.
- `MDTParameterServiceCollection`: `AbstractList<MDTParameterService>`로, 인덱스와 파라미터 id(`get(String)`, 없으면 `null`) 두 방식으로 조회한다. 수정 메서드는 `UnsupportedOperationException`. 현재 두 구현(`HttpMDTInstanceClient`, mdt-manager의 `JpaInstance`)의 `getParameterServiceAll()`이 이 타입을 반환한다.
- 그 밖에 `MDTInstanceManagerAware`(`activate(MDTInstanceManager)`), `StatusChangedListener`(`statusChanged(status)`), `MDTInstanceManagerException`이 있다.

### mdt.model.sm.value — SubmodelElement의 값

`ElementValue`는 SubmodelElement에서 메타데이터(idShort, semanticId 등)를 버린 **값 부분**만 표현한다. `ElementValues.getValue(sme)`로 요소에서 값을 뽑고, `ElementValues.update(sme, value)`로 요소에 다시 넣는다.

`ElementValue` 인터페이스는 메서드 여섯 개만 갖는다: `toValueObject()`(Java 값), `toJsonString()`/`toJsonNode()`(타입 정보를 포함한 전체 JSON), `toValueJsonString()`/`toValueJsonNode()`(값만), `toDisplayString()`. 모든 구현은 `AbstractElementValue`를 상속하며 `MDTModelSerDe`의 Jackson 매퍼를 쓴다.

| 클래스 | AAS 요소 | `@type` | 값 전용 JSON 형태 |
| --- | --- | --- | --- |
| `PropertyValue<T>` (하위 10종) | Property | `mdt:value:string`, `integer`, `float`, `double`, `boolean`, `dateTime`, `duration`, `long`, `short`, `decimal` | Java 값 그대로 (Duration은 ISO-8601 문자열) |
| `MultiLanguagePropertyValue` | MultiLanguageProperty | `mdt:value:mlprop` | `[{"ko": "…"}, …]` |
| `RangeValue<T>` | Range | `mdt:value:range` | `{"min": …, "max": …}` |
| `FileValue` | File | `mdt:value:file` | `{"contentType": …, "value": …}` |
| `ReferenceElementValue` | ReferenceElement | `mdt:value:reference` | `[{"type": …, "value": …}, …]` (키 목록만) |
| `ElementCollectionValue` | SubmodelElementCollection | `mdt:value:collection` | `{idShort: 값, …}` (삽입 순서 유지) |
| `ElementListValue` | SubmodelElementList | `mdt:value:list` | `[값, …]` |
| `OperationVariableValue` | Operation | `mdt:value:opvars` | `{"inputs": […], "outputs": […], "inoutputs": […]}` |

`PropertyValue`의 XSD 타입 대응은 STRING → `String`, INT → `Integer`, FLOAT → `Float`, DOUBLE → `Double`, BOOLEAN → `Boolean`, DATE\_TIME → `Instant`, DURATION → `Duration`, LONG → `Long`, SHORT → `Short`, DECIMAL → `BigDecimal`이다. 정적 팩토리는 `PropertyValue.STRING(…)`, `PropertyValue.INTEGER(…)`처럼 타입 이름을 그대로 쓴다. 그 밖의 XSD 타입은 `IllegalArgumentException`이다.

#### 두 가지 JSON 형식

- **전체 JSON**: `{"@type": "mdt:value:<종류>", "value": …}`. 스스로 타입을 설명하므로 `ElementValues.parseJsonString`으로 바로 복원된다. 컨렉션·리스트의 멤버도 각자 이 봉투를 갖는다.
- **값 전용 JSON**: `toValueObject()`를 JSON으로 바꾼 것(AAS ValueOnly와 비슷한 형태)이다. 타입 정보가 없어서 파싱할 때 **프로토타입** `ElementValue`가 필요하며, 보통 현재 요소의 `getValue(sme)`를 쓴다.
- 프로토타입 규칙: Property는 프로토타입의 XSD 타입을 따른다. 컨렉션은 프로토타입의 필드만 순회해서 JSON에 없는 필드는 `null`, 프로토타입에 없는 JSON 필드는 무시한다. 리스트는 크기가 같아야 하고 위치로 짝을 짓는다. File·MultiLanguageProperty는 프로토타입이 필요 없다.

#### ElementValues 정적 메서드

| 목적 | 메서드 |
| --- | --- |
| 요소 → 값 | `getValue(SubmodelElement)` (Blob, Entity 등 미지원 타입은 `IllegalArgumentException`) |
| 전체 JSON → 값 | `parseJsonString`, `parseJsonNode` |
| 값 전용 JSON·Java 객체 → 값 | `parseValueJsonString(json, proto)`, `parseValueJsonNode(node, proto)`, `fromValueObject(obj, proto)` |
| 형식 자동 판별 | `parseGeneralJsonNode(node, proto)` — `@type`이 없으면 값 전용, `mdt:value:`로 시작하면 전체 JSON. `Supplier` 오버로드는 값 전용일 때만 프로토타입을 가져와 원격 조회를 아낀다 |
| 표현식 리터럴 → 값 | `parseExpr("25.3")`, `parseExpr("'안녕'@ko")` 등 |
| 요소 갱신 | `update(sme, ElementValue \| JsonNode \| String \| Integer \| Float \| Double)`, `updateWithValueJsonString(sme, json)` |

`NamedValueType`은 `(name, valueType)` 쌍과 `"name (type)"` 문자열 형식을 다루는 독립 POJO로, `ElementValue` 계층에 속하지 않는다.

### mdt.model.sm.ref — 요소 참조와 MdtExpr

`ElementReference`는 SubmodelElement 하나를 저장 위치와 상관없이 같은 방식으로 읽고 쓰는 핸들이다. 참조는 문자열 표현식(MdtExpr)이나 JSON으로 만들고, MDT가 관리하는 대상이면 `activate(MDTInstanceManager)`로 실제 인스턴스에 연결한 뒤 쓴다. 표현식 문법은 패키지의 `README.md`와 `docs/mdt-expr-claude-guide.md`에 자세히 있다.

#### ElementReference 메서드

- **읽기**: `getPrototype()`(대상의 구조), `read()` → `SubmodelElement`, `readValue()` → `ElementValue`. 기본 메서드로 `readAsProperty`, `readAsString`, `readCollection`, `readList`, `readCollectionValue`, `readListValue`, `readAASFileValue`와 타입별 `readAsInt`, `readAsInteger`(`BigInteger`), `readAsLong`, `readAsBoolean`, `readAsFloat`, `readAsDuration`, `readAsDateTime`이 있다. 타입별 메서드는 XSD 타입이 정확히 맞아야 하며, 아니면 `IOException`이다.
- **쓰기**: `write(sme)`(요소 교체), `update(sme)`, `updateValue(ElementValue | JsonNode | String)`.
- **첨부파일**: `readAttachment(OutputStream | File)`, `updateAttachment(FileValue, InputStream)`, `updateAttachment(InputStream | File)`, `removeAttachment()`.
- **직렬화**: `toStringExpr()`, `getSerializationType()`, `toJsonString()`, `toJsonNode()`.

`MDTElementReference`는 여기에 `isActivated`, `activate(manager)`, `getInstanceId`, `getInstance`, `getSubmodelService`, `getIdShortPathString`을 더한다. 기본 메서드 `updatePropertyValue(PropertyValue)`는 대상이 파라미터 값이면 `EventDateTime`을 현재 시각으로 함께 갱신한다.

#### 참조 종류

`FileStoreReference`를 뺀 모든 참조는 `SubmodelBasedElementReference`를 상속한다. 이 기반 클래스는 `MDTSubmodelReference` + idShortPath를 갖고, 모든 읽기·쓰기를 `SubmodelService`로 보낸다. 서비스는 호출마다 조회하고, 프로토타입은 처음 한 번만 가져와 재사용한다.

| 클래스 | 표현식 예 | `@type` | 활성화 때 정해지는 idShortPath |
| --- | --- | --- | --- |
| `DefaultElementReference` | `inst:Data:DataInfo.Equipment.EquipmentName` | `mdt:ref:element` | 주어진 경로 그대로 |
| `MDTParameterReference` | `param:inst:Temperature` | `mdt:ref:param` | `DataInfo.{Equipment,Operation}.{…}ParameterValues[i].ParameterValue`. i는 파라미터 id로 원격 조회 |
| `MDTParameterCollectionReference` | `param:inst:*` | `mdt:ref:param` | `DataInfo.{Equipment,Operation}.{…}ParameterValues` |
| `MDTArgumentReference` | `oparg:inst:Simulation:in:Speed` | `mdt:ref:oparg` | `{Simulation,AI}Info.{Inputs,Outputs}[i].{Input,Output}Value`. 이름이면 인자 목록을 원격 조회 |
| `OperationVariableReference` | `opvar:inst:Ops:Calc:out:0` | `mdt:ref:opvar` | 감싼 Operation 요소의 경로 |
| `TimeSeriesRecordsReference` | `timeseries:inst:Log#last=1h` | `mdt:ref:timeseries` | `Segments.FullRange` |
| `FileStoreReference` | 없음 (JSON 전용) | `mdt:ref:file` | 활성화 불필요. 로컬 JSON 파일을 읽고 씀 |

파라미터 참조는 자산 타입이 `Machine`이면 `Equipment`, `Process`면 `Operation` 경로를 쓰고, 다른 타입은 지원하지 않는다. 파라미터 컬렉션 참조는 읽을 때 `{파라미터 id: 값}` 형태의 `ElementCollectionValue`로 바꿔 돌려준다.

#### 서브모델 참조와 활성화

`DefaultSubmodelReference`에는 두 변형이 있다. JSON은 `@type` 없이 두 필드 중 하나로 쓴다.

| 변형 | 표현식 | JSON | `activate` | `get()` |
| --- | --- | --- | --- | --- |
| idShort 기준 | `inst:Data` | `{"instanceId", "submodelIdShort"}` | `manager.getInstance(inst)` | `getSubmodelServiceByIdShort` |
| id 기준 | `submodel:id='…'` | `{"submodelId"}` | `manager.getInstanceBySubmodelId(id)` (필터 조회) | `getSubmodelServiceById` |

- 파싱과 역직렬화는 네트워크를 쓰지 않는다. `activate`가 서브모델 참조를 활성화하고 idShortPath를 확정하며, 이때 원격 조회가 일어날 수 있다.
- 활성화 전에 `read`/`write` 등을 부르면 `IllegalStateException`이다. `toString()`은 동작하며 `(deactivated)`를 붙인다.
- `ElementReferences.activate(ref, manager)`는 `MDTElementReference`가 아닌 참조에 `IllegalArgumentException`을 던진다.

#### ElementReferences 정적 메서드

- `parseExpr(String)` → `MDTElementReference`, `parseSubmodelReference(String)` → `DefaultSubmodelReference`.
- `parseJsonString`/`parseJsonNode` → `ElementReference`. JSON은 `{"@type": "mdt:ref:…", 필드…}` 형태이며 Jackson `Serializer`/`Deserializer`가 처리한다.
- `activate(ref, manager)`.

#### timeseries 하위 패키지

- `TimeSeriesRecordsReference.read()`는 시계열 서브모델의 `ReadRecordsByRange` 연산을 호출한다. 입력 `Range`에는 범위 문자열을, `Columns`에는 쉼표로 잇은 컬럼 목록(항상 `Time`이 첫 컬럼)을 넣고, 결과 SMC를 반환한다.
- 쓰기 계열(`write`, `updateValue`)은 `UnsupportedOperationException`이다.
- 빌더는 `last(n)`, `last(Duration, Anchor)`, `between(from, to)`, `since(from, n)`을 제공한다.
- `TimeSeriesRange`는 sealed 인터페이스로 `Count`, `Trailing`(기준점 `LATEST`/`NOW`), `Absolute`, `Since` 네 record를 갖는다. `parse(String)`과 `toString()`이 표현식 형식(`last=5`, `last=PT1H@now`, `from~to`, `from+N`)을 오간다.

### mdt.model.sm.entity — 어노테이션 기반 매핑

Java 필드에 어노테이션을 붙이면 `AbstractSMEContainerEntity`가 리플렉션으로 그 필드와 SubmodelElement를 양방향 변환한다. `sm.data`·`sm.info`·`sm.ai`·`sm.simulation`의 구현 클래스(`Default*`)는 모두 이 틀 위에 있다.

| 타입 | 역할 |
| --- | --- |
| `AASModelEntity<T>` | `updateAasModel(T)`(엔티티 → AAS), `updateFromAasModel(T)`(AAS → 엔티티) |
| `SubmodelElementEntity` | 위 인터페이스 + `getIdShort()`, `newSubmodelElement()` |
| `AbstractSMEContainerEntity<T>` | idShort·semanticId를 갖고 리플렉션 매핑 로직 전부를 담은 추상 기반 클래스 (691줄) |
| `SubmodelEntity` | `Submodel` 대응. `id` 추가, `newSubmodel()` |
| `SubmodelElementCollectionEntity` | SubmodelElementCollection(SMC) 대응 |
| `SubmodelElementListEntity<E>` | SubmodelElementList(SML) 대응. 멤버 엔티티 목록을 갖고, 하위 클래스가 `newMemberEntity()`를 구현한다 |
| `PropertyEntity` | Property 하나(idShort, valueType, 문자열 value). 원시값 SML을 읽을 때 쓴다 |
| `AasCRUDActions` | 메서드 없는 마커 (`update` 메서드는 주석 처리됨) |

| 어노테이션 | 대응 요소 | 속성(기본값) |
| --- | --- | --- |
| `@PropertyField` | Property | `idShort`, `valueType`(`DataTypes` 이름), `keepNullField=false` |
| `@MultiLanguagePropertyField` | MultiLanguageProperty | `idShort`, `keepNullField=false` |
| `@SMCollectionField` | SMC | `idShort`, `keepNullField=false`, `adaptorClass=void.class` |
| `@SMListField` | SML | `idShort`, `typeValueListElement=SMC`, `valueTypeListElement=STRING`, `orderRelevant=false`, `keepNullField=false`, `elementClass=void.class` |
| `@SMElementField` | 임의 SubmodelElement(그대로 보관) | `idShort` |
| `@ReferenceField` | ReferenceElement | `idShort` (현재 변환 코드 없음) |

각 어노테이션은 `@SubmodelElementEntityField(AasSubmodelElements.X)`로 메타 어노테이션되어 있고, 이것이 없는 필드는 무시된다.

- **읽기(AAS → 엔티티)**: `updateFromAasModel`이 idShort·id·semanticId를 채운 뒤 `updateFields(children)`를 부른다. 자식을 idShort로 색인하고, 상위 클래스 필드를 포함한 모든 어노테이션 필드마다 `anno.idShort()`로 요소를 찾아 변환한다. Property는 DataType으로 문자열을 파싱(Enum은 `valueOf`)하고, SMC는 `adaptorClass`(없으면 필드 타입) 인스턴스를 만들어 재귀한다. SML은 `elementClass`에 따라 엔티티 목록, 문자열 목록, 원본 SubmodelElement 목록 중 하나가 된다. 타입이 맞지 않으면 `ModelGenerationException`.
- **쓰기(엔티티 → AAS)**: `newSubmodel()`/`newSubmodelElement()`가 빈 모델을 만들고 `updateAasModel(model)`을 부른다. 기존 자식을 idShort로 색인한 뒤 `readSubmodelElementFromFields()` 결과로 덮어쓴다. Property는 값이 `null`이 아니거나 `keepNullField`일 때만 만들고, Enum은 `name()`을 STRING으로 기록한다.
- **접근자**: 필드 값은 Apache Commons `PropertyUtils`로 getter/setter를 거쳐 읽고 쓰므로 공개 접근자가 필요하다. 구현 클래스는 주로 Lombok `@Getter`/`@Setter`를 쓴다.

### 도메인 서브모델 (`sm` 루트, `sm.data`, `sm.info`, `sm.ai`, `sm.simulation`, `sm.shape`)

MDT 표준 서브모델 다섯 개를 Java 인터페이스로 표현한다. 인터페이스는 `api`에, 구현(`Default*`, 대부분 entity 프레임워크 기반)은 같은 패키지명으로 `src/main/java`에 있다.

| 서브모델 | 루트 인터페이스 | semantic ID 경로 | 하위 구조 | 서비스 클래스 |
| --- | --- | --- | --- | --- |
| Data | `Data` | `/Submodel/Data` | `SubModelInfo`, `DataInfo`(Equipment 또는 Operation 등) | `DataSubmodelService` |
| InformationModel | `InformationModel` | `/Submodel/InformationModel` | `MDTInfo`, `TwinComposition` | 없음 |
| AI | `AI` | `/Submodel/AI` | `SubModelInfo`, `AIInfo`(Inputs, Outputs, MinResourceRequirements) | `AISubmodelService` |
| Simulation | `Simulation` | `/Submodel/Simulation` | `SubModelInfo`, `SimulationInfo`(Model, SimulationTool, Inputs, Outputs, MinResourceRequirements) | `SimulationSubmodelService` |
| Shape | `Shape` | `/Submodel/Shape` | `ShapeInfo`(Inputs, Outputs) | 없음 (구현 클래스도 없음) |

#### sm 루트 — 연산형 서브모델 공통

- `OperationSubmodelService`(클래스): `SubmodelService`를 Lombok `@Delegate`로 감싸고 연산 인자를 다룬다. 생성 시 semanticId로 경로 접두사를 정한다(Data → `DataInfo`, AI → `AIInfo`, Simulation → `SimulationInfo`).
  - `getInputs()`/`getOutputs()`: `<접두사>.Inputs`/`.Outputs` SML을 `ArgumentCollection`(인자 id → `ElementValue`)으로 돌려준다.
  - `run(inputs, timeout, pollInterval)`: `AASOperationClient`로 `Operation` 요소를 실행하고 출력 idShort → `ElementValue` 맵을 반환한다.
- `OperationEntity`: `get/setInputs(List<Input>)`, `get/setOutputs(List<Output>)`. `AIInfo`, `SimulationInfo`, `ShapeInfo`가 확장한다.
- `AASFile`: 경로와 contentType을 가진 POJO. `getMediaType()`(OkHttp `MediaType`), `getValue()` → `FileValue`.
- `Metadata`: SubmodelElement에서 idShort, modelType, valueType, typeValueListElement, semanticId를 뽑은 불변 객체.

#### sm.data — Data 서브모델

- `Data`는 `getSubModelInfo()`와 `getDataInfo()`를 갖는다. `DataInfo`는 엔티티 목록이며, Equipment가 있으면 설비형, Operation이 있으면 공정형이다(`isEquipment`, `isOperation`, `getEquipment`, `getOperation`).
- `DataInfo`의 하위 엔티티는 `DefaultMDTEntityFactory`가 idShort로 만든다: `Equipment`, `Operation`, `Line`, `Equipments`, `ProductionPlannings`, `ProductionOrders`, `ProductionPerformances`, `Repairs`, `Andons`, `ItemMasters`, `BOMs`, `Routings`. 그 밖의 idShort는 `IllegalArgumentException`이다.
- `ParameterCollection`은 `getParameterList()`와 `getParameterValueList()` 두 목록을 갖고, 두 목록은 인덱스로 1:1 대응한다. `getParameter(int|String)`, `getParameterValue(int|String)`, 인덱스 조회 기본 메서드를 제공한다. `Equipment`와 `Operation`이 이를 확장한다.

| 타입 | 주요 필드 |
| --- | --- |
| `Equipment` | EquipmentId, EquipmentName, EquipmentType, UseIndicator (+ 파라미터 목록) |
| `Operation` | OperationId, OperationName, OperationType, UseIndicator, ProductionOrders (+ 파라미터 목록) |
| `Parameter` | EntityId, ParameterId(키), ParameterName, ParameterType, ParameterGrade, ParameterUOMCode, LSL, USL, PeriodicDataCollectionIndicator, DataCollectionPeriod |
| `ParameterValue` | EntityId, ParameterId(키), ParameterValue(`SubmodelElement`), EventDateTime(`Instant`), ValidationResultCode |
| `Equipment/OperationParameter(Value)` | 위 두 타입에 EquipmentId 또는 OperationId를 더한 특화형 |

`ParameterValue.setParameterValue(ElementValue)` 기본 메서드는 `ElementValues.update`로 값만 바꾼다.

생산 도메인 타입은 semantic ID 없이 getter/setter만 가진다. 복수형(`Xs`)은 `getElementAll()`을 가진 목록 래퍼다.

| 타입 | 내용 | 주요 필드 |
| --- | --- | --- |
| `Andon(s)` | 라인 정지(안돈) 이벤트 | AndonID, OperationID, StartDateTime, EndDateTime, CauseName, LineStopType |
| `BOM(s)` | 자재명세 | BOMID, BOMType, ItemID, BOMQuantity, ValidStart/EndDateTime |
| `ItemMaster(s)` | 품목 마스터 | ItemID, ItemType, ItemName, ItemUOMCode, LotSize |
| `LOT` | 작업 로트 (Equipment의 WorkingLOTs) | LotId, ItemId, State, Quantity, Start/EndDateTime, AppliedTactTime |
| `Line` | 생산 라인 | LineID, LineName, LineType, UseIndicator, LineStatus |
| `ProductionOrder(s)` | 생산 지시 | ProductionOrderID, ItemID, ProductionOrderQuantity, ProductionDueDateTime, ScheduleStart/EndDateTime |
| `ProductionPerformance(s)` | 생산 실적 | ProductionPerformanceID, ProductionOrderID, ProducedQuantity, DefectQuantity, ExecutionStart/EndDateTime |
| `ProductionPlanning(s)` | 생산 계획 | ProductionPlanID, ItemID, ProductionPlanQuantity, ScheduleStart/EndDateTime |
| `Repair(s)` | 불량 등록·수리 | RepairID, DefectRegOperationID, DefectRegDateTime, RepairDateTime, InitialDefectLevel1\~3 |
| `Routing(s)` | 라우팅 | RoutingID, RoutingName, ItemID, SetupTime |

#### sm.info — InformationModel 서브모델

- `MDTInfo`: IdShort(자산 ID), AssetType(`MDTAssetType`: `Machine`, `Process`, `Line`, `Factory`), AssetName, Status(`MDTAssetStatus`: `Created`, `Registered`, `Executed`).
- `TwinComposition`: CompositionID, CompositionType, CompositionItems(`ID`, `Reference`, `Description`), CompositionDependencies(`SourceId`, `TargetId`, `DependencyType` 문자열). `MDTInstance`의 의존 조회(`getComponentInstanceAll` 등)가 이 구성 정보를 표현한다.

#### sm.ai, sm.simulation, sm.shape

- **AI**: `AIInfo`는 Inputs/Outputs와 `MinResourceRequirements`를 갖는다. `ai.Model`은 LearningMethod, AIModelType, AIFramework, ModelDescription, LastExecutionTime. `AISubmodelService`는 `OperationSubmodelService`를 확장하고 `getAI()`를 더한다.
- **Simulation**: `SimulationInfo`는 Model, MinResourceRequirements, SimulationTool, Inputs/Outputs를 갖는다. `SimulationTool`은 SimToolName, OperatingSystem, LicenseModel, SimulatorEndpoint, SimulationTimeout, SessionRetainTimeout. `simulation.Model`은 ModelFileType, ModelFileVersion, LastExecutionTime. `Simulation`에는 SimulatorEndpoint 등의 idShortPath 상수와 `getSimulationOperation()`(aas4j `Operation`)이 있다.
- **Shape**: `ShapeInfo`는 `OperationEntity`를 확장하는 빈 인터페이스다.
- `HttpMDTInstanceClient`는 semanticId를 보고 Simulation/AI 서브모델을 각각 `SimulationSubmodelService`/`AISubmodelService`로 감싼다. Data 서브모델은 감싸지 않는다.

### mdt.model.timeseries — 시계열 서브모델

IDTA TimeSeries 서브모델 템플릿을 표현하는 인터페이스다. semantic ID는 MDT 고유 URI가 아니라 `https://admin-shell.io/idta/TimeSeries/…/1/1`이며 `TimeSeriesSemanticIds`에 모여 있다. 레코드를 실제로 읽을 때는 앞의 `TimeSeriesRecordsReference`를 쓴다.

| 타입 | 내용 | 주요 메서드 |
| --- | --- | --- |
| `TimeSeries` | 서브모델 루트 | `getMetadata()`, `getSegments()` |
| `Metadata` | 이름·설명·레코드 구조 | `getName()`/`getDescription()`(`MultiLanguagePropertyValue`), `getRecord()` → `RecordMetadata`(컬럼 이름·타입) |
| `Record` / `Records` | 레코드 한 건 / 목록 | `getTimestamp()`(`Instant`), `getFieldValues()` / `getRecordList()` |
| `Segment` / `Segments` | 구간 / 목록 | 이름, 설명, `getRecordCount()`, 시작·종료 시각, `getDuration()`, `getSamplingInterval()`, `getSamplingRate()`, `getState()`, `getLastUpdate()` |
| `InternalSegment` | 레코드를 내부에 가진 구간 | `getRecords()` |
| `ExternalSegment` | 파일에 저장된 구간 | `getData()`(AAS `File`) |
| `LinkedSegment` | 외부 저장소에 있는 구간 | `getEndpoint()`, `getQuery()` |
| `SegmentState` | 구간 상태 enum | `IN_PREGRESS`, `COMPLETED` |
| `TimeSeriesOperationReference` | 연산 요소를 감싼 `MDTElementReference` | `read()`가 입력 `Timespan`(DATE\_TIME Range)으로 연산을 실행한다. 나머지는 미지원이거나 `null` 반환 |

구현은 `src/main/java/mdt/model/timeseries`의 `DefaultTimeSeries`, `DefaultMetadata`, `DefaultRecord(s)`, `DefaultSegment(s)`, `DefaultInternalSegment`, `DefaultLinkedSegment`다. `ExternalSegment`의 구현은 없다.

## 사용 예시

아래 예시는 인스턴스 id `line-01`을 가정하며, 검사 예외(`IOException`, `TimeoutException` 등) 처리는 생략했다. 메서드 이름과 시그니처는 현재 코드에서 확인했지만 예시 자체를 실행해 보지는 않았다.

### 1. 접속과 인스턴스 제어

```java
MDTManager mdt = HttpMDTManager.connectWithDefault();   // mdt_client_config.yaml 사용
MDTInstanceManager manager = mdt.getInstanceManager();

MDTInstance inst = manager.getInstance("line-01");
if ( !inst.isRunning() ) {
    // 1초 간격으로 상태를 확인하며 최대 1분 대기
    inst.start(Duration.ofSeconds(1), Duration.ofMinutes(1));
}

List<? extends MDTInstance> stopped
        = manager.getInstanceAllByFilter("instance.status = 'STOPPED'");
```

### 2. 표현식으로 파라미터 읽고 쓰기

```java
MDTElementReference ref = ElementReferences.parseExpr("param:line-01:Temperature");
ref.activate(manager);                       // 이 시점에 인스턴스·경로를 확정

Float temperature = ref.readAsFloat();       // xs:float 속성이어야 한다
ref.updateValue(ElementValues.parseExpr("25.3"));
```

### 3. 값과 JSON 변환

```java
ElementValue value = ref.readValue();
String full = value.toJsonString();          // {"@type":"mdt:value:float","value":25.3}
String valueOnly = value.toValueJsonString(); // 25.3

ElementValue restored = ElementValues.parseJsonString(full);           // 프로토타입 불필요
ElementValue parsed = ElementValues.parseValueJsonString("26.0", value); // value가 프로토타입
```

### 4. Data 서브모델을 엔티티로 읽기

```java
SubmodelService svc = inst.getSubmodelServiceByIdShort("Data")
                          .getOrThrow(() -> ResourceNotFoundException.ofIdShort("Submodel", "Data"));
Equipment eq = new DataSubmodelService(svc).getData().getDataInfo().getEquipment();
System.out.println(eq.getEquipmentId() + ": " + eq.getEquipmentName());
```

### 5. 시뮬레이션 연산 실행

```java
// HTTP 클라이언트는 Simulation 서브모델을 SimulationSubmodelService로 감싸서 돌려준다
SimulationSubmodelService sim = (SimulationSubmodelService)inst.getSubmodelServiceByIdShort("Simulation")
                                                                .getOrThrow(() -> ResourceNotFoundException.ofIdShort("Submodel", "Simulation"));
Property speed = new DefaultProperty.Builder()
                        .idShort("Speed").valueType(DataTypeDefXsd.FLOAT).value("1.5")
                        .build();
Map<String,ElementValue> outputs = sim.run(Map.of("Speed", speed),
                                           Duration.ofMinutes(10), Duration.ofSeconds(2));
```

## 설계상 특징과 주의사항

`api` 패키지는 공개 계약을 지향하지만 일부는 구현 코드에 직접 의존하고, 코드를 읽어 본 결과 동작 오류로 보이는 곳이 13군데 있다. 아래 항목은 모두 소스에서 확인했으며, 실행해서 재현하지는 않았다.

### 설계상 특징

- **참조와 값의 분리**: `ElementReference`는 "어디", `ElementValue`는 "무엇"을 담는다. 읽기는 `ref.readValue()`, 쓰기는 `ref.updateValue(value)`로 두 축이 만난다.
- **지연 연결**: 참조는 파싱·역직렬화 때 네트워크를 쓰지 않고 `activate`에서 비로소 인스턴스에 연결된다. 덕분에 워크플로나 CLI 인자로 참조를 문자열·JSON으로 주고받을 수 있다.
- **예외 정책**: 도메인 예외는 모두 unchecked(`MDTException` 계열)이고, 원격 입출력 경로만 `IOException`을 검사 예외로 드러낸다.
- **api↔java 분리는 불완전하다**: `OperationSubmodelService`와 `TimeSeriesOperationReference`는 `mdt.client.operation.AASOperationClient`를, `sm.ref`는 `mdt.model.sm.SubmodelUtils`를, `AI`·`Simulation`·`DataSubmodelService`는 `DefaultAI`·`DefaultSimulation`·`DefaultData`를 직접 쓴다. 따라서 `api`만 따로 빌드할 수는 없다.

### 동작상 주의사항

| 위치 | 내용 | 영향 |
| --- | --- | --- |
| `SubmodelService.runOperation` | 실행 상태가 FAILED여도 예외 없이 결과를 반환한다(예외 코드는 주석 처리) | 호출자가 결과의 실행 상태를 직접 확인해야 한다 |
| `AbstractElementReference.update(sme)` | Javadoc은 값 갱신이라고 하지만 `write(sme)`를 불러 요소 전체를 교체한다 | 메타데이터까지 덮어쓴다 |
| `DefaultSubmodelReference`(id 기준) `toStringExpr()` | `submodel:id=<id>`를 따옴표 없이 출력한다 | URL 형태 id는 다시 파싱되지 않는다 |
| `MDTParameterReference` 경로 분해 | 표현식을 `.`로 먼저 나눈다 | `Samples[0].x`가 파라미터 id `Samples[0]`로 조회된다 |
| `MDTArgumentKind.fromString` | `in`으로 시작하면 모두 INPUT으로 본다 | `inout`, `invalid`도 INPUT이 된다 |
| `sm.Metadata` 생성자 | SML 분기에서 `m_typeValueListElement` 대신 `m_valueType`을 검사한다 | SubmodelElementList마다 `IllegalStateException` |
| `AbstractSMEContainerEntity.toProperty` | 값이 `null`이고 `keepNullField=true`면 `value.getClass()`에서 NPE | `@PropertyField(keepNullField=true)`가 동작하지 않는다 |
| `SubmodelElementListEntity.updateFromAasModel` | `valueTypeListElement`를 SML에서 읽지 않고 자기 값을 다시 넣는다 | 원본 값이 반영되지 않는다 |
| `SubmodelEntity.updateFromAasModel` | `checkNotNullArgument(model != null, …)`에 boolean을 넘긴다 | null 검사가 동작하지 않는다 |
| `OperationSubmodelService` 생성자 | semanticId로 `switch`하는데 `null` 처리가 없다 | semanticId 없는 서브모델이면 NPE |
| 엔티티 쓰기(`updateAasModel`) | 자식을 `FStream.toMap()`(HashMap)으로 다시 모은다 | 원래 요소 순서가 유지되지 않는다 |
| `MDTInstanceManager` 필터 기본 메서드 | 값을 `'%s'`로 그대로 끼워 넣는다 | 작은따옴표가 든 id는 필터가 깨진다 |
| `ResourceNotFoundException.ofSemanticId` | `target` 인자를 무시하고 "Submodel"로 고정한다 | 메시지의 자원 종류가 틀릴 수 있다 |

### 미완성·미사용 코드와 명명 문제

- **구현 없음**: `Shape`/`ShapeInfo`, `ExternalSegment`.
- **연결되지 않은 타입**: `StatusChangedListener`는 등록 메서드가 어느 인터페이스에도 없다. `@ReferenceField`는 변환 코드가 없고, `@SMListField.orderRelevant`는 읽히지 않는다. `DependencyType`과 `NamedValueType`은 저장소들에서 참조하는 곳이 없다.
- **주석 처리된 필드**: `Equipment`(약 14개), `Operation`(시작·종료 시각), `Line`(BOM·품목·라우팅 목록), `AIInfo.getModel` 등.
- **명명**: `SegmentState.IN_PREGRESS`(오타), `Shape.getSimulationInfo()`가 `ShapeInfo`를 반환, `Segment.getState()`가 `SegmentState`가 아닌 `String`을 반환.
