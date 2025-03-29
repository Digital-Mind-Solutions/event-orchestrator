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
@Schema(name = "ProcessActivityDTO", description = "The process activity.")
public class EventActivityDTO extends AuditDTO {

    @Schema(name = "Unique id of the process activity", required = false)
    private Long id;

    @Schema(name = "The id of the process", required = false)
    private Long processId;

    @Schema(name = "The id of the parent memo (if applicable)", required = true)
    private Long parentMemoId;

    @Schema(name = "The activity type", required = true)
    private EventActivityType type;

    @Schema(name = "The activity code", required = false)
    private String code;

    @Schema(name = "The date when activity becomes effective", required = false)
    private Date plannedDate;

    @Schema(name = "The date when activity will be re-executed", required = false)
    private Date retryDate;

    @Schema(name = "The status of the activity (pending or executed)", required = false)
    private EventActivityStatus status;

    @Schema(name = "The status description of the activity (error message)", required = false)
    private String statusDescription;

    @Builder.Default
    @Schema(name = "The retry no of the activity", required = false)
    private int retry = 0;

    @Schema(name = "The qualification rule", required = false)
    private String qualifier;

    @Schema(name = "The execution rule", required = false)
    private String executor;

    @Schema(name = "The system memo", required = false)
    private String systemMemo;

    @Schema(name = "The entity name", required = false)
    private String entityName;

    @Schema(name = "The entity id", required = false)
    private String entityId;

    @Schema(name = "The process activity parameters", required = false)
    private Map<String, Object> parameters;

    @Schema(name = "The node processing the activity", required = false)
    private String executionNode;

    @Schema(name = "The node processing the activity", required = false)
    private EventActivityExecutionType executionType;

    @Schema(name = "The process activity context", required = false)
    private Map<String, Object> context;

    @Schema(name = "The memo context id", required = false)
    private String contextId;
}
