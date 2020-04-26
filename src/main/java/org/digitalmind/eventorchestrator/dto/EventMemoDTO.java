package org.digitalmind.eventorchestrator.dto;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.dtobase.AuditDTO;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventMemoStatus;

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
                "processId", "parentId",
                "type", "code",
                "status", "statusDescription",
                "systemMemo",
                "entityName", "entityId",
                "parameters",
                "context", "contextId",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@ApiModel(value = "ProcessMemoDTO", description = "The process memo.")
public class EventMemoDTO extends AuditDTO {

    @ApiModelProperty(value = "Unique id of the process memo", required = false)
    private Long id;

    @ApiModelProperty(value = "The id of the process", required = false)
    private Long processId;

    @ApiModelProperty(value = "The id of the parent memo (if applicable)", required = false)
    private Long parentId;

    @ApiModelProperty(value = "The process activity type", required = true)
    private EventActivityType type;

    @ApiModelProperty(value = "The memo code", required = false)
    private String code;

    @ApiModelProperty(value = "The status of the activity (disqualified, success or error)", required = false)
    private EventMemoStatus status;

    @ApiModelProperty(value = "The status description of the memo (error message)", required = false)
    private String statusDescription;

    @ApiModelProperty(value = "The system memo", required = false)
    private String systemMemo;

    @ApiModelProperty(value = "The entity name", required = false)
    private String entityName;

    @ApiModelProperty(value = "The entity id", required = false)
    private String entityId;

    @ApiModelProperty(value = "The process memo parameters", required = false)
    private Map<String, Object> parameters;

    @ApiModelProperty(value = "The memo context", required = false)
    private Map<String, Object> context;

    @ApiModelProperty(value = "The memo context id", required = false)
    private String contextId;

}
