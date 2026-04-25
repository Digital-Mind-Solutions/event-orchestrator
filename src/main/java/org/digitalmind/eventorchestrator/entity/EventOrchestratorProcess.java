package org.digitalmind.eventorchestrator.entity;

import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdModel;

public interface EventOrchestratorProcess extends PartitionedIdModel<Integer, Long> {

    String getFlowTemplate();

    boolean setFatalCause(Throwable throwable);
}
