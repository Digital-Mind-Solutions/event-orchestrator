package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
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

import java.util.Date;
import java.util.Map;

import static org.digitalmind.eventorchestrator.entity.EventActivity.*;

@Entity
@Table(name = TABLE_NAME,
        indexes = {
                // audit / debug / eventual purge / reporting
                @Index(
                        name = TABLE_IX_CREATED_AT,
                        columnList = "created_at"
                ),

                // audit / debug / incremental scans
                @Index(
                        name = TABLE_IX_UPDATED_AT,
                        columnList = "updated_at"
                ),

                // POLL PARALLEL
                // folosit de: findAllWithExecutionTypeParallel
                // filtre: execution_type, status
                // range: planned_date, retry_date
                // order: priority, planned_date, retry_date, id
                // scop: index range scan ordonat + LIMIT early stop + lock minim
                @Index(
                        name = TABLE_IX_POLL_PARALLEL,
                        columnList = "execution_type,status,priority,planned_date,retry_date,id"
                ),

                // POLL SERIAL (OUTER)
                // folosit de:
                // - findAllWithExecutionTypeSerialProcess (outer)
                // - findAllWithExecutionTypeSerialEntity (outer)
                // filtre: execution_type, status
                // order: priority, retry_date, id
                // NOTĂ: planned_date este filtru suplimentar aplicat după scan-ul ordonat;
                // nu îl punem în index pentru că ar rupe ordinea necesară pentru ORDER BY priority,retry_date,id
                @Index(
                        name = TABLE_IX_POLL_SERIAL,
                        columnList = "execution_type,status,priority,retry_date,id"
                ),

                // SERIAL_PROCESS SUBQUERY
                // folosit de: subquery din findAllWithExecutionTypeSerialProcess
                // condiții:
                //   process_id = ?
                //   execution_type = ?
                //   planned_date < ?
                // scop:
                //   - process_id first (cheia de serializare → selectivitate mare)
                //   - index seek rapid per row
                //   - elimină nested scan costisitor
                @Index(
                        name = TABLE_IX_SERIAL_PROCESS,
                        columnList = "process_id,execution_type,planned_date,id"
                ),

                // SERIAL_ENTITY SUBQUERY
                // folosit de: subquery din findAllWithExecutionTypeSerialEntity
                // condiții:
                //   process_id = ?
                //   entity_id = ?
                //   entity_name = ?
                //   planned_date < ?
                // scop:
                //   - cheia completă de serializare
                //   - index seek rapid
                //   - evită scan mare în NOT EXISTS
                @Index(
                        name = TABLE_IX_SERIAL_ENTITY,
                        columnList = "process_id,entity_id,entity_name,execution_type,planned_date,id"
                ),

                // ORPHAN QUEUED
                // folosit de: findOrphanQueuedEntity
                // filtre: status
                // join logic: execution_node (cu heartbeat)
                // order: priority, planned_date, retry_date, id
                // scop: scan mic + fără filesort
                @Index(
                        name = TABLE_IX_ORPHAN_QUEUED,
                        columnList = "status,execution_node,priority,planned_date,retry_date,id"
                ),

                // PROCESS VISIBLE PAGE
                // folosit de: findAllByProcessIdAndVisibleAndPrivacyId
                // filtre: process_id, visibility, privacy_id
                // order: retry_date
                // scop: paging eficient fără filesort
                @Index(
                        name = TABLE_IX_PROCESS_VISIBLE,
                        columnList = "process_id,visibility,privacy_id,retry_date,planned_date,id"
                )
        }

)

@EntityListeners({AuditingEntityListener.class})

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
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
    private static final int STATUS_DESCRIPTION_MAX_LENGTH = 4000;

    public static final String TABLE_NAME = "process_activity";
    static final String TABLE_SHORT_NAME = "pr_actv";
    static final String TABLE_IX_CREATED_AT = TABLE_SHORT_NAME + "_ixcreat";
    static final String TABLE_IX_UPDATED_AT = TABLE_SHORT_NAME + "_ixupdat";

    static final String TABLE_IX_POLL_PARALLEL = TABLE_SHORT_NAME + "_ixpollprl";
    static final String TABLE_IX_POLL_SERIAL = TABLE_SHORT_NAME + "_ixpollser";

    static final String TABLE_IX_SERIAL_PROCESS = TABLE_SHORT_NAME + "_ixserprc";
    static final String TABLE_IX_SERIAL_ENTITY = TABLE_SHORT_NAME + "_ixserent";

    static final String TABLE_IX_ORPHAN_QUEUED = TABLE_SHORT_NAME + "_ixorphan";
    static final String TABLE_IX_PROCESS_VISIBLE = TABLE_SHORT_NAME + "_ixprcvis";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique id of the process activity")
    @Column(name = "id")
    private Long id;

    @Schema(description = "The name of the process")
    @Column(name = "process_name")
    private String processName;

    @Column(name = "process_partition_key")
    //@NonNull
    @Schema(description = "The key of the process partition")
    private Integer processPartitionKey;

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


    public void setRetryDate(Date retryDate) {
        this.retryDate = (retryDate != null)
                ? retryDate
                : this.plannedDate;
    }

    public void setPlannedDate(Date plannedDate) {
        this.plannedDate = plannedDate;

        if (this.retryDate == null) {
            this.retryDate = plannedDate;
        }
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = truncateStatusDescription(statusDescription);
    }

    private static String truncateStatusDescription(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > STATUS_DESCRIPTION_MAX_LENGTH
                ? value.substring(0, STATUS_DESCRIPTION_MAX_LENGTH)
                : value;
    }

    @PrePersist
    public void onPrePersist() {
        if (this.retryDate == null) {
            this.retryDate = this.plannedDate;
        }
    }

}

