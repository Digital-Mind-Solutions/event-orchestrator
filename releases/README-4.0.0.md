# event-orchestrator — release 4.0.0

Library for **process activity orchestration**: template activators (SpEL), pollers, memos, retry policy, and host entity plugins.

| | |
|--|--|
| Artifact | `org.digitalmind:event-orchestrator` |
| Module | `event-orchestrator` |
| Branch | `spring-4.0.0` |
| Version | `4.0.0` (Gradle root `version`) |
| Stack | Spring Boot **4.0.3**, Java **25** |
| Depends on | `dtobase`, `beanutils`, `spelutils`, `networkutils`, `requestcontext`, `jpautils`, `templating-core` (**4.0.0**) |
| Licence | **DMSAL** — `dmsal_license_version` in [`../gradle.properties`](../gradle.properties); Git tag `v{ver}` + `v{ver}/LICENSE.md`; plugin `org.digitalmind.dmsal-license:{ver}` → `META-INF/LICENSE.md` |
| Doc | [`releases/README-4.0.0.md`](README-4.0.0.md) |

This document is the **full contract** for version **4.0.0** as shipped on this branch. Overview / index: [`../README.md`](../README.md).

## Purpose

| Need | Use |
|------|-----|
| Create / trigger activities | `EventOrchestratorService.triggerEventActivities(...)` |
| Execute activity (sync/async) | `EventOrchestratorService.executeEventActivity(...)` |
| Retry policy lookup | `EventOrchestratorService.getRetryPolicy(code, retry)` |
| Host entity for SpEL | `getEntity` / `EventOrchestratorEntityPlugin` |

## Layout

```text
event-orchestrator/
  README.md                  # overview index
  gradle.properties          # dmsal_license_version
  releases/                  # per-version full contracts
  src/main/java/org/digitalmind/eventorchestrator/
```

### Main packages

| Package / path | Role |
|----------------|------|
| `...config` | `EventOrchestratorModuleConfig`, `EventOrchestratorConfig`, `EventDirectiveConfig` |
| `...entity` | `EventActivity`, `EventMemo`, `EventRetry`, `TemplateActivity*`, `EventDirective`, … |
| `...repository` | Spring Data repositories |
| `...service` | `EventOrchestratorService`, entity services, pollers |
| `...service.impl` | `EventOrchestratorServiceImpl`, `EventDirectiveServiceImpl` |
| `...plugin` | `EventOrchestratorEntityPlugin` (+ internal / no-support) |
| `...exception` | `EventOrchestratorRetryException` / `Final` / `Fatal` |
| `...utils` | `EventOrchestratorExceptionUtils` |
| `...enumeration` | `ExceptionType`, `EventActivityStatus`, execution type/mode, … |
| `...api` / `...dto` | Optional REST / DTOs |

## Config

Prefix: **`application.modules.common.eventorchestrator`** (`EventOrchestratorModuleConfig.PREFIX`).

```yaml
application.modules.common.eventorchestrator:
  enabled: true
  api:
    enabled: false
  cache:
    eventRetry: …
    templateFlow: …
    templateActivity: …
    templateActivityActivator: …
  threadPoolExecutor:
    parallel: { … }
    serialProcess: { … }
    serialEntity: { … }
    heartbeat: { … }
    orphan: { … }
```

| Constant | Value |
|----------|--------|
| `ENABLED` | `application.modules.common.eventorchestrator.enabled` |
| `API_ENABLED` | `application.modules.common.eventorchestrator.api.enabled` |
| `CACHE_NAME` | `eventorchestrator-cache` |

Also required:

1. JPA: `ENTITY_PACKAGE` + `REPOSITORY_PACKAGE` on the persistence unit that owns orchestrator tables.
2. Component-scan / import `CONFIG_PACKAGE`.
3. Host poller executors + `EventOrchestratorEntityPlugin` beans for process/entity types used in SpEL.

## Execution model

| `EventActivityExecutionType` | Meaning |
|------------------------------|---------|
| `PARALLEL` | No serialization vs other activities of the process |
| `SERIAL_PROCESS` | Serialized with other `SERIAL_PROCESS` activities for the same process id |
| `SERIAL_ENTITY` | Serialized by process + entity |

Statuses include `PENDING`, `QUEUED`, `PENDING_RETRY`, `SUCCESS`, `ERROR`, `DISQUALIFIED`, `PARKED`.

### SpEL activity fields (`TemplateActivity`)

Typical expressions evaluated at execute time: **qualifier**, **system memo**, **executor** (plus entity name/id / parameters at create time). Context includes `#requestContext`, `#process`, `#entity`, `#eventActivity`, …

## Exception handling

| Type | Exception | Effect (via `getExceptionType`) |
|------|-----------|----------------------------------|
| `RETRY` | `EventOrchestratorRetryException` | Activity may go `PENDING_RETRY` per `EventRetry` policy |
| `FINAL` | `EventOrchestratorFinalException` | Activity `ERROR` |
| `FATAL` | `EventOrchestratorFatalException` | Activity `ERROR`; may set process fatal cause |

Classification: `EventOrchestratorExceptionUtils.getExceptionType` / `getExceptionCause`.  
Policy: `EventRetry` / `getRetryPolicy(code, retry)`.

## `EventOrchestratorService` API

| Method | Role |
|--------|------|
| `createEventMemo` / `createEventActivity` | Persist memo / activity |
| `triggerEventActivities(...)` | Activators → create activities from template flow |
| `executeEventActivity(...)` | Qualifier → executor SpEL → memo status; retry/fatal handling |
| `getEntity` / `getEntityAlias` | Plugin-backed entity load for SpEL |
| `getRetryPolicy(code, retry)` | Policy for activity code + attempt |
| `createParameters(...)` | Helper map for SpEL / params |

## Publish / consume

1. Publish `event-orchestrator` `4.0.0` to your Maven repository.
2. Consumer: dependency + JPA packages + `enabled: true` + poller / plugin wiring.
3. Optionally enable `api`.
