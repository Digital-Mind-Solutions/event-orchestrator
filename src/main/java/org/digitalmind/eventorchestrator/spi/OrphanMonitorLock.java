package org.digitalmind.eventorchestrator.spi;

/**
 * Cluster leadership gate for the orphan recirculation monitor.
 * <p>
 * The orchestrator does not know about ShedLock (or any other lock store). Host applications
 * may provide a bean that elects a single leader per tick; otherwise the default always runs.
 */
@FunctionalInterface
public interface OrphanMonitorLock {

    /**
     * Runs {@code work} only if this node acquired leadership for the current orphan tick.
     *
     * @return {@code true} if {@code work} was executed; {@code false} if skipped
     */
    boolean tryExecute(Runnable work);
}
