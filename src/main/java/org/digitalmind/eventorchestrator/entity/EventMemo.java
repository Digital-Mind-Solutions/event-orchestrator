package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.generator.PartitionAwareIdModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.generator.PartitionAwareTableId;
import org.digitalmind.eventorchestrator.converter.JpaMapJsonConverter;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventMemoStatus;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

import static org.digitalmind.eventorchestrator.entity.EventMemo.*;

@Entity
@IdClass(EventMemoId.class)
@Table(
        name = TABLE_NAME,
        indexes = {
                @Index(name = TABLE_IX_CREATED_AT, columnList = "created_at", unique = false),
                @Index(name = TABLE_IX_UPDATED_AT, columnList = "updated_at", unique = false),
                @Index(name = TABLE_IX_PARTITION_PROCESS_ID, columnList = "partition_key, process_id, id", unique = false),
                @Index(name = TABLE_IX_PARTITION_CONTEXT_ID, columnList = "partition_key, context_id", unique = false),
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
                "partitionKey",
                "id", "processId", "parentId", "type",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@Schema(description = "Process memo")
@ToString(callSuper = true)
public class EventMemo extends ContextVersionableAuditModel implements ProcessAuditModel, PartitionedIdModel<Integer, Long>, Persistable<Long>, PartitionAwareIdModel<Integer, Long> {

    static final String TABLE_NAME = "process_memo";
    static final String TABLE_SHORT_NAME = "pr_memo";
    static final String TABLE_IX_CREATED_AT = TABLE_SHORT_NAME + "_ixcreat";
    static final String TABLE_IX_UPDATED_AT = TABLE_SHORT_NAME + "_ixupdat";
    static final String TABLE_IX_EXTERNAL_IDENTIFIER = TABLE_SHORT_NAME + "_ixextidf";
    static final String TABLE_IX_PARTITION_PROCESS_ID = TABLE_SHORT_NAME + "_ixpkprcid";
    static final String TABLE_IX_PARTITION_CONTEXT_ID = TABLE_SHORT_NAME + "_ixpkctxid";

    @Id
    @Column(name = "partition_key", nullable = false)
    private Integer partitionKey;

    @Id
    @PartitionAwareTableId(
            table = "seq_" + TABLE_NAME,
            pkColumnName = "sequence_name",
            valueColumnName = "next_val",
            pkColumnValue = "seq_" + TABLE_NAME,
            allocationSize = 50,
            initialValue = 1
    )
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Schema(description = "The name of the process")
    @Column(name = "process_name", length = 500)
    private String processName;

    @Schema(description = "The id of the process")
    @Column(name = "process_id")
    //@NonNull
    private Long processId;

    @Schema(description = "The id of the parent memo (if applicable)")
    @Column(name = "parent_id")
    private Long parentId;

    @Schema(description = "The id of the activity (if applicable)")
    @Column(name = "activity_id")
    private Long activityId;

    @Schema(description = "The process activity type", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityType type;

    @Schema(description = "The activity code")
    @Column(name = "code")
    private String code;

    @Schema(description = "The status of the memo (disqualified, success or error)")
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private EventMemoStatus status;

    @Schema(description = "The status description of the memo (error message)")
    @Column(name = "status_description", length = 4000)
    private String statusDescription;

    @Schema(description = "The system memo")
    @Column(name = "system_memo", length = 500)
    private String systemMemo;

    @Schema(description = "The entity name")
    @Column(name = "entity_name", length = 500)
    private String entityName;

    @Schema(description = "The entity id")
    @Column(name = "entity_id")
    private String entityId;

    @Schema(description = "The process memo parameters")
    @JdbcTypeCode(SqlTypes.CLOB)
    @Column(name = "parameters", columnDefinition = "LONGTEXT")
    @Singular
    @Convert(converter = JpaMapJsonConverter.class)
    private Map<String, Object> parameters;

    @Schema(description = "The memo context")
    @JdbcTypeCode(SqlTypes.CLOB)
    @Column(name = "context", columnDefinition = "LONGTEXT")
    @Convert(converter = JpaMapJsonConverter.class)
    private Map<String, Object> context;

    @Schema(description = "The memo visibility")
    @Column(name = "visibility")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibility;

    @Schema(description = "The privacy id")
    @Column(name = "privacy_id")
    private Long privacyId;

    @Override
    @Transient
    @JsonIgnore
    public boolean isNew() {
        return getId() == null;
    }

    @Override
    public Integer calcPartitionKey(Long id) {
        return this.getPartitionKey();
    }

}
