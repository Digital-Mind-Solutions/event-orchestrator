package org.digitalmind.eventorchestrator.dto;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel(value = "ProcessActivityDTO", description = "The process activity.")
public class EventActivityDTO extends AuditDTO {

    @ApiModelProperty(value = "Unique id of the process activity", required = false)
    private Long id;

    @ApiModelProperty(value = "The id of the process", required = false)
    private Long processId;

    @ApiModelProperty(value = "The id of the parent memo (if applicable)", required = true)
    private Long parentMemoId;

    @ApiModelProperty(value = "The activity type", required = true)
    private EventActivityType type;

    @ApiModelProperty(value = "The activity code", required = false)
    private String code;

    @ApiModelProperty(value = "The date when activity becomes effective", required = false)
    private Date plannedDate;

    @ApiModelProperty(value = "The date when activity will be re-executed", required = false)
    private Date retryDate;

    @ApiModelProperty(value = "The status of the activity (pending or executed)", required = false)
    private EventActivityStatus status;

    @ApiModelProperty(value = "The status description of the activity (error message)", required = false)
    private String statusDescription;

    @Builder.Default
    @ApiModelProperty(value = "The retry no of the activity", required = false)
    private int retry = 0;

    @ApiModelProperty(value = "The qualification rule", required = false)
    private String qualifier;

    @ApiModelProperty(value = "The execution rule", required = false)
    private String executor;

    @ApiModelProperty(value = "The system memo", required = false)
    private String systemMemo;

    @ApiModelProperty(value = "The entity name", required = false)
    private String entityName;

    @ApiModelProperty(value = "The entity id", required = false)
    private String entityId;

    @ApiModelProperty(value = "The process activity parameters", required = false)
    private Map<String, Object> parameters;

    @ApiModelProperty(value = "The node processing the activity", required = false)
    private String executionNode;

    @ApiModelProperty(value = "The node processing the activity", required = false)
    private EventActivityExecutionType executionType;

    @ApiModelProperty(value = "The process activity context", required = false)
    private Map<String, Object> context;

    @ApiModelProperty(value = "The memo context id", required = false)
    private String contextId;
}
