package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.converter.JpaMapJsonConverter;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType;
import org.digitalmind.eventorchestrator.enumeration.EventActivityStatus;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
@Schema(description = "Process activity.")
@ToString(callSuper = true)
public class EventActivity extends ContextVersionableAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "process_activity";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique id of the process activity")
    @Column(name = "id")
    private Long id;

    @Schema(description = "The name of the process")
    @Column(name = "process_name")
    private String processName;

    @Column(name = "process_id")
    //@NonNull
    @Schema(description = "The id of the process")
    private Long processId;

    @Schema(description = "The id of the parent memo (if applicable)", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "parent_memo_id")
    private Long parentMemoId;

    @Schema(description = "The activity type", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityType type;

    @Schema(description = "The activity code")
    @Column(name = "code")
    private String code;

    @Schema(description = "The date when activity becomes effective")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "planned_date")
    private Date plannedDate;

    @Schema(description = "The status of the activity (pending or executed)")
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityStatus status;

    @Schema(description = "The status description of the activity (error message)")
    @Column(name = "status_description", length = 4000)
    private String statusDescription;

    @Schema(description = "The date when activity becomes effective for retry")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    @Column(name = "retry_date")
    private Date retryDate;

    @Schema(description = "The retry no of the activity")
    @Column(name = "retry")
    @Builder.Default
    private int retry = 0;

    @Schema(description = "The qualification rule")
    @Column(name = "qualifier", length = 4000)
    private String qualifier;

    @Schema(description = "The execution rule")
    @Column(name = "executor", length = 4000)
    private String executor;

    @Schema(description = "The system memo")
    @Column(name = "system_memo", length = 500)
    private String systemMemo;

    @Schema(description = "The entity name")
    @Column(name = "entity_name", length = 500)
    private String entityName;

    @Schema(description = "The entity id")
    @Column(name = "entity_id")
    private String entityId;

    @Schema(description = "The process activity parameters")
    @JdbcTypeCode(SqlTypes.CLOB)
    @Column(name = "parameters", columnDefinition = "LONGTEXT")
    @Singular
    @Convert(converter = JpaMapJsonConverter.class)
    private Map<String, Object> parameters;

    @Schema(description = "The node processing the activity")
    @Column(name = "execution_node")
    private String executionNode;

    @Schema(description = "The node processing the activity")
    @Column(name = "execution_type", length = 50)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EventActivityExecutionType executionType = EventActivityExecutionType.SERIAL_ENTITY;

    @Schema(description = "The process activity context")
    @JdbcTypeCode(SqlTypes.CLOB)
    @Column(name = "context", columnDefinition = "LONGTEXT")
    @Singular("context")
    @Convert(converter = JpaMapJsonConverter.class)
    private Map<String, Object> context;

    @Schema(description = "The activity visibility")
    @Column(name = "visibility")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibility;

    @Schema(description = "The memo success visibility")
    @Column(name = "visibility_success")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibilitySuccess;

    @Schema(description = "The privacy id")
    @Column(name = "privacy_id")
    private Long privacyId;

    @Schema(description = "The priority id (lower is more important)")
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 5000;
}

