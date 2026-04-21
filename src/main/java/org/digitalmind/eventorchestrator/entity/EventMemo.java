package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdModel;
import org.digitalmind.eventorchestrator.converter.JpaMapJsonConverter;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventMemoStatus;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Map;

import static org.digitalmind.eventorchestrator.entity.EventMemo.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME,
        indexes = {
                @Index(
                        name = TABLE_NAME + "_ix1",
                        columnList = "context_id",
                        unique = false
                ),
                @Index(
                        name = TABLE_NAME + "_ix_partition_process",
                        columnList = "partition_key, process_id",
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
                "id", "processId", "parentId", "type",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@Schema(description = "Process memo")
@ToString(callSuper = true)
public class EventMemo extends ContextVersionableAuditModel implements ProcessAuditModel, PartitionedIdModel<Integer, Long> {

    public static final String TABLE_NAME = "process_memo";

    @EmbeddedId
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private MemoId pk;

    @Schema(description = "P1 partition key")
    public Integer getPartitionKey() {
        return pk != null ? pk.getPartitionKey() : null;
    }

    public void setPartitionKey(Integer partitionKey) {
        if (pk == null) {
            pk = new MemoId();
        }
        pk.setPartitionKey(partitionKey);
    }

    @Schema(description = "Unique id of the process activity")
    @Override
    public Long getId() {
        return pk != null ? pk.getId() : null;
    }

    public void setId(Long id) {
        if (pk == null) {
            pk = new MemoId();
        }
        pk.setId(id);
    }

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

}
