package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.converter.JpaMapStringObjectJsonConverter;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType;
import org.digitalmind.eventorchestrator.enumeration.EventActivityStatus;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Map;

import static org.digitalmind.eventorchestrator.entity.EventActivity.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME,
        indexes = {
                @Index(
                        name = TABLE_NAME + "_ix1",
                        columnList = "planned_date,status,execution_node,retry_date",
                        unique = false
                ),
                @Index(
                        name = TABLE_NAME + "_ix2",
                        columnList = "process_id,entity_id,type",
                        unique = false
                )
        }

)
@EntityListeners({AuditingEntityListener.class})

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder(
        {
                "id", "processId", "parentMemoId", "type",
                "code", "plannedDate", "status", "statusDescription",
                "retry", "qualifier", "executor",
                "systemMemo", "entityName", "entityId", "parameters",
                "context", "contextId",
                "executionNode", "executionType",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@Schema(name = "ProcessActivity", description = "Process activity.")
@ToString(callSuper = true)
public class EventActivity extends ContextVersionableAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "process_activity";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "Unique id of the process activity", required = false)
    @Column(name = "id")
    private Long id;

    @Schema(name = "The name of the process", required = false)
    @Column(name = "process_name")
    private String processName;

    @Column(name = "process_id")
    //@NonNull
    @Schema(name = "The id of the process", required = false)
    private Long processId;

    @Schema(name = "The id of the parent memo (if applicable)", required = true)
    @Column(name = "parent_memo_id")
    private Long parentMemoId;

    @Schema(name = "The activity type", required = true)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityType type;

    @Schema(name = "The activity code", required = false)
    @Column(name = "code")
    private String code;

    @Schema(name = "The date when activity becomes effective", required = false)
    @Column(name = "planned_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date plannedDate;

    @Schema(name = "The status of the activity (pending or executed)", required = false)
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityStatus status;

    @Schema(name = "The status description of the activity (error message)", required = false)
    @Column(name = "status_description", length = 4000)
    private String statusDescription;

    @Schema(name = "The date when activity becomes effective for retry", required = false)
    @Column(name = "retry_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date retryDate;

    @Schema(name = "The retry no of the activity", required = false)
    @Column(name = "retry")
    @Builder.Default
    private int retry = 0;

    @Schema(name = "The qualification rule", required = false)
    @Column(name = "qualifier", length = 4000)
    private String qualifier;

    @Schema(name = "The execution rule", required = false)
    @Column(name = "executor", length = 4000)
    private String executor;

    @Schema(name = "The system memo", required = false)
    @Column(name = "system_memo", length = 500)
    private String systemMemo;

    @Schema(name = "The entity name", required = false)
    @Column(name = "entity_name", length = 500)
    private String entityName;

    @Schema(name = "The entity id", required = false)
    @Column(name = "entity_id")
    private String entityId;

    @Schema(name = "The process activity parameters", required = false)
    @Column(name = "parameters")
    @Singular
    @Convert(converter = JpaMapStringObjectJsonConverter.class)
    @Lob
    private Map<String, Object> parameters;

    @Schema(name = "The node processing the activity", required = false)
    @Column(name = "execution_node")
    private String executionNode;

    @Schema(name = "The node processing the activity", required = false)
    @Column(name = "execution_type", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventActivityExecutionType executionType = EventActivityExecutionType.SERIAL_ENTITY;

    @Schema(name = "The process activity context", required = false)
    @Column(name = "context")
    @Singular("context")
    @Convert(converter = JpaMapStringObjectJsonConverter.class)
    @Lob
    private Map<String, Object> context;

    @Schema(name = "The activity visibility", required = false)
    @Column(name = "visibility")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibility;

    @Schema(name = "The memo success visibility", required = false)
    @Column(name = "visibility_success")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibilitySuccess;

    @Schema(name = "The privacy id", required = false)
    @Column(name = "privacy_id")
    private Long privacyId;

    @Schema(name = "The priority id (lower is more important)", required = false)
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 5000;
}

