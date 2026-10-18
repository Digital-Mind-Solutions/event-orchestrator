package org.digitalmind.eventorchestrator.spi;

/**
 * Default {@link OrphanMonitorLock}: always executes the orphan tick (no cluster leadership).
 */
public class AlwaysRunOrphanMonitorLock implements OrphanMonitorLock {

    @Override
    public boolean tryExecute(Runnable work) {
        work.run();
        return true;
    }
}
