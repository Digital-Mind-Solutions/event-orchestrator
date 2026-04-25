package org.digitalmind.eventorchestrator.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
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
                "partitionKey",
                "id",
                "processId", "parentId",
                "activityId", "type", "code",
                "status", "statusDescription",
                "systemMemo",
                "entityName", "entityId",
                "parameters",
                "context", "contextId",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@Schema(description = "The process memo.")
public class EventMemoDTO extends AuditDTO {

    @Schema(description = "Partition key of the process memo")
    private Integer partitionKey;

    @Schema(description = "Unique id of the process memo")
    private Long id;

    @Schema(description = "The id of the process")
    private Long processId;

    @Schema(description = "The id of the parent memo (if applicable)")
    private Long parentId;

    @Schema(description = "The process activity id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long activityId;

    @Schema(description = "The process activity type", requiredMode = Schema.RequiredMode.REQUIRED)
    private EventActivityType type;

    @Schema(description = "The memo code")
    private String code;

    @Schema(description = "The status of the activity (disqualified, success or error)")
    private EventMemoStatus status;

    @Schema(description = "The status description of the memo (error message)")
    private String statusDescription;

    @Schema(description = "The system memo")
    private String systemMemo;

    @Schema(description = "The entity name")
    private String entityName;

    @Schema(description = "The entity id")
    private String entityId;

    @Schema(description = "The process memo parameters")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> parameters;

    @Schema(description = "The memo context")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> context;

    @Schema(description = "The memo context id")
    private String contextId;

}
