package org.digitalmind.eventorchestrator.entity.extension;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType;
import org.digitalmind.eventorchestrator.enumeration.EventActivityStatus;

import java.util.Date;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class EventActivityResult extends ContextVersionableAuditModel implements IdModel<Long> {

    private Long id;
    private Long processId;
    private String code;
    private Date plannedDate;
    private EventActivityStatus status;
    private String statusDescription;
    private Date retryDate;
    @Builder.Default
    private int retry = 0;
    private String executionNode;
    @Builder.Default
    private EventActivityExecutionType executionType = EventActivityExecutionType.SERIAL_ENTITY;

}
