# event-orchestrator — release 4.0.1

Library for **process activity orchestration**: template activators (SpEL), pollers, memos, retry policy, and host entity plugins.

| | |
|--|--|
| Artifact | `org.digitalmind:event-orchestrator` |
| Module | `event-orchestrator` |
| Branch | `spring-4.0.1` |
| Version | `4.0.1` (Gradle root `version`) |
| Stack | Spring Boot **4.0.3**, Java **25** |
| Depends on | `dtobase`, `beanutils`, `spelutils`, `networkutils`, `requestcontext`, `jpautils`, `templating-core` (**4.0.0**) |
| Licence | **DMSAL** — `dmsal_license_version` in [`../gradle.properties`](../gradle.properties); Git tag `v{ver}` + `v{ver}/LICENSE.md`; plugin `org.digitalmind.dmsal-license:{ver}` → `META-INF/LICENSE.md` |
| Doc | [`releases/README-4.0.1.md`](README-4.0.1.md) |
| Prior line | [`README-4.0.0.md`](README-4.0.0.md) |

Overview / index: [`../README.md`](../README.md).

## Differences from 4.0.0

| Area | **4.0.0** | **4.0.1** |
|------|-----------|-----------|
| Artifact version | `4.0.0` | `4.0.1` |
| In-memory error queue | One shared `ConcurrentLinkedQueue` for all pollers | One queue per poller index (`PARALLEL` / `SERIAL_PROCESS` / `SERIAL_ENTITY`) |
| Requeue drain + optimistic lock | Unhandled `ObjectOptimisticLockingFailureException` could stick the queue head and stop the tick | Catch OOLE → `findByIdReadOnly`; skip if deleted / already pending / not requeueable; on still-`QUEUED` skip requeue (orphan may recycle) |
| Pool full | `remainingCapacity == 0` returned before drain | Still no claim/submit when full; **drain still runs** |
| Orphan | Single query: `QUEUED` without heartbeat | Two TX steps: dead-node orphans, then optional old-queued on **active** node (remaining batch size) |
| Old-queued recycle | — | `orphan.recycle.oldQueueDuration`; **default `0s` = disabled** |
| Index (consumer DB) | `pr_actv_ixorphan` | + `pr_actv_ixoldq` `(status, priority, planned_date, retry_date, id)` for old-queued order |
| Activity info logs | id / code | + `version`, `processId`, `processPartitionKey` where relevant |

Baseline API / layout / exception types: same as [`README-4.0.0.md`](README-4.0.0.md) unless noted below.

## Purpose

| Need | Use |
|------|-----|
| Create / trigger activities | `EventOrchestratorService.triggerEventActivities(...)` |
| Execute activity (sync/async) | `EventOrchestratorService.executeEventActivity(...)` |
| Retry policy lookup | `EventOrchestratorService.getRetryPolicy(code, retry)` |
| Host entity for SpEL | `getEntity` / `EventOrchestratorEntityPlugin` |

## Config

Prefix: **`application.modules.common.eventorchestrator`**.

```yaml
application.modules.common.eventorchestrator:
  enabled: true
  thread-pool-executor:
    parallel: { … }
    serial-process: { … }
    serial-entity: { … }
    heartbeat: { … }
    orphan:
      scheduler-init-delay: 0
      scheduler-period: 120
      scheduler-unit: SECONDS
      scheduler-capacity-threshold: 10
      recycle:
        # default / shipped recommendation: disabled
        old-queue-duration: 0s
        # enable stuck QUEUED on live nodes, e.g.:
        # old-queue-duration: 60m
```

| Property | Default | Meaning |
|----------|---------|---------|
| `orphan.recycle.oldQueueDuration` | **`0s` (disabled)** | If `> 0`, second orphan step requeues `QUEUED` with `updatedAt` older than this duration **and** heartbeat still present |

Java default on `EventOrchestratorRecycleConfiguration.oldQueueDuration` is `Duration.ZERO`.

## Poller error queue (4.0.1)

1. Claim → `QUEUED` → submit to pool.
2. Reject / Future failure → activity on `errorEventActivityQueue[pollerIndex]`.
3. Drain: `requeue` → `PENDING_RETRY` (retry +60s, `executionNode` = hostname).
4. On `ObjectOptimisticLockingFailureException`: read-only reload; if missing or not requeueable, drop from memory queue; if still `QUEUED`, do **not** requeue here (optional best-effort persist of `statusDescription`); recycle via orphan when enabled.
5. Other exceptions on requeue: error log with activity fields, then rethrow (only that poller’s tick fails).

## Orphan (4.0.1)

| Step | Method | Selection | Update |
|------|--------|-----------|--------|
| 1 (always) | `requeueOrphanQueuedWithDeadNode(size)` | `QUEUED` + no heartbeat | `PENDING_RETRY`, `retryDate=now`, `executionNode=null` |
| 2 (if duration > 0 and remaining > 0) | `requeueOrphanOldQueuedWithActiveNode(remaining, before)` | `QUEUED` + `updatedAt < before` + heartbeat exists | same |

Both steps use ORDER BY `priority, plannedDate, retryDate, id` (same business order as poll). Each step is its own `@Transactional`.

### Consumer DB

Add index (Liquibase / DDL on host schema), e.g. esign:

```text
pr_actv_ixoldq (status, priority, planned_date, retry_date, id)
```

Without it, the old-queued query can filesort on large `QUEUED` stock.

## `EventActivityService` (additions)

| Method | Role |
|--------|------|
| `findByIdReadOnly(id)` | `@Transactional(readOnly=true)` existence / fresh read after OOLE |
| `requeueOrphanQueuedWithDeadNode(size)` | returns rows processed |
| `requeueOrphanOldQueuedWithActiveNode(size, oldQueuedBefore)` | returns rows processed |

## Publish / consume

1. Publish `event-orchestrator` `4.0.1`.
2. Consumer: dependency `4.0.1` + JPA packages + `enabled: true` + poller / plugin wiring.
3. Keep `orphan.recycle.old-queue-duration: 0s` unless you explicitly enable old-queued recycle.
4. Apply `pr_actv_ixoldq` (or equivalent) before enabling a positive `old-queue-duration` on large tables.
5. Restart pods so in-memory error queues pick up the new drain logic.
