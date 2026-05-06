package org.digitalmind.eventorchestrator.entity;

public interface ProcessAuditModel {

    Long getProcessId();

    /**
     * Partition key of the owning process (when the audited entity is partition-scoped).
     */
    Integer getPartitionKey();
}
