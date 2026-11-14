# CLAUDE.md

Guidance for coding agents working in this repository.

## What this is

`mdt-client` is a Java 21 **library** (no main application) for the ETRI MDT (Manufacturing Digital Twin) system.
It talks HTTP to MDT Manager and to each MDTInstance's AAS services, and it also holds the model / reference /
value / workflow types shared by the other MDT projects. Changes to public types ripple into the sibling repos
under `/home/kwlee/development/mdt/` (`mdt-cli`, `mdt-manager`, `mdt-instance`, `mdt-workflow`,
`mdt-operation-http`, `mdt-samples`, `mdt-robot-client`).

User-facing docs (Korean): [README.md](README.md) (whole library),
[src/main/api/mdt/model/README.md](src/main/api/mdt/model/README.md) (public API by package),
[src/main/api/mdt/model/sm/ref/README.md](src/main/api/mdt/model/sm/ref/README.md) (references + MdtExpr grammar),
[src/main/api/mdt/model/sm/ref/CLAUDE.md](src/main/api/mdt/model/sm/ref/CLAUDE.md) (internals of `sm.ref` / `expr`).
Read the relevant README before changing behavior; update it in the same change when behavior changes.

## Build and test

Gradle is installed globally; there is **no wrapper** (`./gradlew` does not exist). `settings.gradle` includes the
sibling project `../../common/utils` as `:utils` (composite build).

```bash
gradle compileJava
gradle :test --tests 'mdt.model.sm.ref.*'       # scope tests; ':test' avoids also running :utils:test
gradle :test --tests mdt.aas.DataTypesTest
gradle :test                                    # full mdt-client suite (~340 tests); run it once, alone
gradle shadowJar                                # fat jar in build/libs (no fixed name / main class)
```

- `MDT_BUILD_VERSION` sets the project version (default `unknown`).
- JUnit 5 + Mockito; `--add-opens=java.base/java.lang=ALL-UNNAMED` is configured in `build.gradle`.
- Dependency versions live in `gradle/libs.versions.toml`.
- After changing a public signature, compile the downstream repos that use it, e.g.
  `(cd ../mdt-instance && gradle -q compileJava)`. Find users first with
  `grep -rn --include='*.java' '<name>' /home/kwlee/development/mdt | grep -v /build/`.
- Iterate with package-scoped test runs. Do not chain several full-suite runs in one command.

## Source layout

One main source set compiled from three roots, so the same package (`mdt.*`) is split across directories:

| Root | Contents |
| --- | --- |
| `src/main/api` | Public API: service interfaces, domain types, `ElementReference`/`ElementValue` hierarchies, workflow model. Contains real logic, not just interfaces. |
| `src/main/java` | HTTP clients (`mdt.client.*`), `Default*` implementations, `mdt.model.expr` (MdtExpr evaluation), `mdt.task.*`, `mdt.rt` (ROS2 conversion), utilities (`AASUtils`, `MDTModelSerDe`, `SubmodelUtils`, `DescriptorUtils`) |
| `src/main/antlr` | `MdtExpr.g4` + **generated** lexer/parser/visitor under `mdt/model/expr/`. Never hand-edit generated files; regenerate with `(cd src/main/antlr && bash antlr4_script.sh)`. |
| `src/main/java/mdt/test` | Manual programs that hit a live server. Not JUnit; don't run them as tests. |
| `src/test/java` | JUnit tests (`mdt.aas`, `mdt.client.*`, `mdt.model.*`, `mdt.task`, `mdt.workflow.model`) |

`bin/` and `build/` are IDE/Gradle output (git-ignored); ignore them when searching.

## Architecture map

- **Entry point**: `mdt.model.MDTManager` → `HttpMDTManager` (`connect(url)`, `connect(config)`,
  `connectWithConfigFile`, `connectWithDefault` via `MDT_CLIENT_HOME`/`mdt_client_config.yaml`/`MDT_URL`).
  Provides `MDTInstanceManager`, `SubmodelRegistry`, `WorkflowManager`; `getService(Class)` returns `null` for
  unsupported classes.
- **Instances** (`mdt.model.instance`, `mdt.client.instance`): `MDTInstanceManager` (lookup, single-condition
  filter `"<attr> = '<literal>'"` — build literals with `MDTInstanceManager.toFilterLiteral`), `MDTInstance`
  (`start/stop(pollInterval, timeout)`, AAS/Submodel services, descriptors, parameters). `HttpMDTInstanceClient`
  caches the descriptor for 3 s.
- **AAS access** (`mdt.aas`, `mdt.client.registry|repository|resource`): Registry = descriptors, Repository =
  collections, `SubmodelService` / `AssetAdministrationShellService` = one resource. Elements are addressed by
  idShortPath (`a.b[0].c`). HTTP `SubmodelService` rejects `Modifier.VALUE`.
- **Values** (`mdt.model.sm.value`): `ElementValue` = value part of a SubmodelElement. Two JSON forms:
  typed (`{"@type":"mdt:value:…","value":…}`, self-describing) and value-only (needs a prototype to parse).
  `ElementValues` is the façade (`getValue`, `update`, `parse*`, `updateFromValueJson`, `updateFromValueString`).
- **Types** (`mdt.aas.DataTypes`): XSD ↔ Java ↔ JSON ↔ JDBC conversion. `fromDataTypeName` is case-sensitive
  (`"INT"` or `"xs:int"`). JDBC durations are seconds. Conversions reject lossy input instead of coercing.
- **References** (`mdt.model.sm.ref`): `ElementReference` = handle to a location. Parse with
  `ElementReferences.parseExpr` / `parseJsonNode`; MDT references must be `activate(manager)`-ed before use
  (otherwise `IllegalStateException`); parsing never touches the network. New reference or value types must be
  registered in the `@type` registry of `ElementReferences` / `ElementValues`.
- **Entities** (`mdt.model.sm.entity`): annotation-driven mapping (`@PropertyField`, `@SMCollectionField`,
  `@SMListField`, …) between Java beans and AAS elements via `AbstractSMEContainerEntity` (reflection + public
  getters/setters, usually Lombok). Domain submodels: `sm.data`, `sm.info`, `sm.ai`, `sm.simulation`, `sm.shape`,
  `mdt.model.timeseries`.
- **Operations / tasks**: `SubmodelService.runOperation`, `AASOperationClient`, `mdt.task.*`
  (`AASOperationTask`, `OperationSubmodelTask`, `SetTask`, `RESTfulRpcTask`, `RESTfulAsyncRpcTask`). Adding a task
  type also needs `TaskDescriptors.CLASS_TO_SHORT_ID_MAP` and the Airflow side — see
  `../mdt-workflow/docs/adding-operator.md`.
- **Workflow** (`mdt.workflow`, `mdt.workflow.model`): `WorkflowModel`, `TaskDescriptor`, `ArgumentSpec`
  (spec, `mdt:task_arg:*`) vs `mdt.task.TaskArgument` (runtime, `mdt:argument:*`) — different types with similar
  nested class names. `HttpWorkflowManager` delegates to a remote workflow manager. `TaskDescriptor` JSON
  rejects unknown fields (a `@JsonAnySetter` throws, so this holds under Spring's lenient mapper too); a new
  descriptor field needs a getter/setter pair, not just a JSON key.

## Conventions

- Fields: `m_` prefix for instance fields, `s_` for static loggers etc. Lombok is common
  (`@Getter @Setter`, `@Accessors(prefix="m_")`, `@Delegate`).
- Argument checks: use `utils.Preconditions` (`checkNotNullArgument`, `checkArgument`, `checkState`) in new code,
  not Guava's. Its message templates take only `%s` placeholders.
- Exceptions: domain errors are unchecked (`MDTException` family: `ResourceNotFoundException`,
  `ResourceAlreadyExistsException`, `InvalidResourceStatusException`, …); remote I/O paths expose `IOException`.
  HTTP failures surface as `utils.http.RESTfulRemoteException` / `RESTfulIOException`.
- Lookups returning "maybe absent" use `utils.func.FOption` (not `java.util.Optional`).
- Comments and Javadoc are written in **Korean**; keep new ones in Korean. Javadoc/comment lines ≤ 110 columns
  (tab = 4). `@author Kang-Woo Lee (ETRI)`.
- Javadoc must pass doclint (`-Xdoclint:all`): every public type/member documented, `@param`/`@return`/`@throws`
  present and accurate.
- Docs and Javadoc describe current behavior only — no history sentences ("before 2026-11-01 …",
  "renamed from …"). History belongs in commit messages.
- Source files use LF line endings; preserve existing endings when editing.

## Working rules

- Modify code only when asked; questions get answers, not edits.
- Renames of public API are the user's decision: propose (with downstream usage counts), don't apply unasked.
- Commit or push only when asked. Commit messages are English, imperative subject.
- Review tracking documents go under `docs/reviews/` (local, not committed) and are deleted when all items are done.
