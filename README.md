# event-orchestrator

Activity / memo orchestration for process workflows: template-driven SpEL activators, pollers (`PARALLEL` / `SERIAL_PROCESS` / `SERIAL_ENTITY`), retry policy, and entity plugins.

| | |
|--|--|
| Artifact | `org.digitalmind:event-orchestrator` |
| Module | `event-orchestrator` (single jar) |
| Current line | **4.0.1** (`spring-4.0.1`) |
| Full contracts | [`releases/`](releases/) |
| Stack | Spring Boot **4.0.3**, Java **25** |
| Depends on | `dtobase`, `beanutils`, `spelutils`, `networkutils`, `requestcontext`, `jpautils`, `templating-core` (all **4.0.0**) |
| Licence | **DMSAL** (`dmsal_license_version` in [`gradle.properties`](gradle.properties)) — [canonical `@v1.0`](https://github.com/Digital-Mind-Solutions/dmsal-license/blob/v1.0/v1.0/LICENSE.md) |

Consumer owns schema/migrations for orchestrator tables (`EventActivity` / process activities, memos, retries, template activities, …), poller thread pools, and host `EventOrchestratorProcess` / entity plugins.

## Licence

Licensed under the **Digital Mind Source-Available License (DMSAL)**. Public Source Code is for inspection only; Use requires Articles 5, 6, or 9 of the licence.

| | |
|--|--|
| Pin | [`gradle.properties`](gradle.properties) → **`dmsal_license_version=1.0`** |
| Git tag + text | [`v1.0` / `v1.0/LICENSE.md`](https://github.com/Digital-Mind-Solutions/dmsal-license/blob/v1.0/v1.0/LICENSE.md) — pattern `blob/v{ver}/v{ver}/LICENSE.md` |
| Plugin | `org.digitalmind.dmsal-license` version **`${dmsal_license_version}`** (mavenLocal / Maven) |

No `LICENSE.md` in this repo. Publish the matching plugin from [`dmsal-license`](https://github.com/Digital-Mind-Solutions/dmsal-license) (`./gradlew publishToMavenLocal`), then build this project.

## Overview

| Need | Use |
|------|-----|
| Trigger activities from a memo / code | `EventOrchestratorService.triggerEventActivities(...)` |
| Execute one activity | `EventOrchestratorService.executeEventActivity(...)` |
| SpEL executor / qualifier / system memo | `TemplateActivity` expressions; evaluated in `EventOrchestratorServiceImpl` |
| Retry vs fatal vs final | `ExceptionType` + `EventRetry` policy; `EventOrchestratorExceptionUtils` |
| Load host entities for SpEL `#entity` | `EventOrchestratorEntityPlugin` registry |

```text
event-orchestrator/
  README.md                  # this index
  gradle.properties          # dmsal_license_version
  releases/README-<version>.md
  src/main/java/org/digitalmind/eventorchestrator/
```

Config prefix: **`application.modules.common.eventorchestrator`** (`EventOrchestratorModuleConfig.PREFIX`).  
Cache name: **`eventorchestrator-cache`**.

## Usage (sketch)

```yaml
application.modules.common.eventorchestrator:
  enabled: true
  api:
    enabled: false
  thread-pool-executor:
    orphan:
      recycle:
        # default: disabled
        old-queue-duration: 0s
```

```xml
<dependency>
  <groupId>org.digitalmind</groupId>
  <artifactId>event-orchestrator</artifactId>
  <version>4.0.1</version>
</dependency>
```

Also: JPA packages from `EventOrchestratorModuleConfig` (`ENTITY_PACKAGE`, `REPOSITORY_PACKAGE`), component-scan `CONFIG_PACKAGE`, host poller wiring, and plugins for process/entity load.

Release contract: [`releases/README-4.0.1.md`](releases/README-4.0.1.md).

## Versions

| Version | Branch | Notes | Full README |
|---------|--------|-------|-------------|
| **4.0.1** | `spring-4.0.1` | Per-poller error queues; OOLE-safe requeue drain; optional old-queued orphan (`old-queue-duration`, default **0s** / off) | [`releases/README-4.0.1.md`](releases/README-4.0.1.md) |
| **4.0.0** | `spring-4.0.0` | Partitioned process model; SpEL activity execution | [`releases/README-4.0.0.md`](releases/README-4.0.0.md) |
| **4.1.0** | `spring-4.1.0` | Executor exception typing (RETRY/FINAL/FATAL preserve) | [`releases/README-4.1.0.md`](releases/README-4.1.0.md) |
