package org.digitalmind.eventorchestrator.dto;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.dtobase.AuditDTO;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType;
import org.digitalmind.eventorchestrator.enumeration.EventActivityStatus;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;

import java.util.Date;
import java.util.Map;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@JsonPropertyOrder(
        {
                "id",
                "processId", "parentMemoId",
                "type", "code", "plannedDate",
                "status", "statusDescription", "retry",
                "qualifier", "executor", "systemMemo",
                "entityName", "entityId",
                "parameters",
                "executionNode", "executionType",
                "context", "contextId",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@Schema(description = "The process activity.")
public class EventActivityDTO extends AuditDTO {

    @Schema(description = "Unique id of the process activity")
    private Long id;

    @Schema(description = "The id of the process")
    private Long processId;

    @Schema(description = "The id of the parent memo (if applicable)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long parentMemoId;

    @Schema(description = "The activity type", requiredMode = Schema.RequiredMode.REQUIRED)
    private EventActivityType type;

    @Schema(description = "The activity code")
    private String code;

    @Schema(description = "The date when activity becomes effective")
    private Date plannedDate;

    @Schema(description = "The date when activity will be re-executed")
    private Date retryDate;

    @Schema(description = "The status of the activity (pending or executed)")
    private EventActivityStatus status;

    @Schema(description = "The status description of the activity (error message)")
    private String statusDescription;

    @Builder.Default
    @Schema(description = "The retry no of the activity")
    private int retry = 0;

    @Schema(description = "The qualification rule")
    private String qualifier;

    @Schema(description = "The execution rule")
    private String executor;

    @Schema(description = "The system memo")
    private String systemMemo;

    @Schema(description = "The entity name")
    private String entityName;

    @Schema(description = "The entity id")
    private String entityId;

    @Schema(description = "The process activity parameters")
    private Map<String, Object> parameters;

    @Schema(description = "The node processing the activity")
    private String executionNode;

    @Schema(description = "The node processing the activity")
    private EventActivityExecutionType executionType;

    @Schema(description = "The process activity context")
    private Map<String, Object> context;

    @Schema(description = "The memo context id")
    private String contextId;
}
