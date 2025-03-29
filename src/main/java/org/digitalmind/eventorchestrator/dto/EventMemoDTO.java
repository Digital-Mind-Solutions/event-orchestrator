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
@Schema(name = "ProcessMemoDTO", description = "The process memo.")
public class EventMemoDTO extends AuditDTO {

    @Schema(name = "Unique id of the process memo", required = false)
    private Long id;

    @Schema(name = "The id of the process", required = false)
    private Long processId;

    @Schema(name = "The id of the parent memo (if applicable)", required = false)
    private Long parentId;

    @Schema(name = "The process activity id", required = true)
    private Long activityId;

    @Schema(name = "The process activity type", required = true)
    private EventActivityType type;

    @Schema(name = "The memo code", required = false)
    private String code;

    @Schema(name = "The status of the activity (disqualified, success or error)", required = false)
    private EventMemoStatus status;

    @Schema(name = "The status description of the memo (error message)", required = false)
    private String statusDescription;

    @Schema(name = "The system memo", required = false)
    private String systemMemo;

    @Schema(name = "The entity name", required = false)
    private String entityName;

    @Schema(name = "The entity id", required = false)
    private String entityId;

    @Schema(name = "The process memo parameters", required = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> parameters;

    @Schema(name = "The memo context", required = false)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> context;

    @Schema(name = "The memo context id", required = false)
    private String contextId;

}
