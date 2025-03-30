package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.converter.JpaMapStringObjectJsonConverter;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventMemoStatus;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

import static org.digitalmind.eventorchestrator.entity.EventMemo.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME,
        indexes = {
                @Index(
                        name = TABLE_NAME + "_ix1",
                        columnList = "context_id",
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
@Schema(name = "ProcessMemo", description = "Process memo")
@ToString(callSuper = true)
public class EventMemo extends ContextVersionableAuditModel implements ProcessAuditModel, IdModel<Long> {

    public static final String TABLE_NAME = "process_memo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "Unique id of the process activity", required = false)
    @Column(name = "id")
    private Long id;

    @Schema(name = "The name of the process", required = false)
    @Column(name = "process_name", length = 500)
    private String processName;

    @Schema(name = "The id of the process", required = false)
    @Column(name = "process_id")
    //@NonNull
    private Long processId;

    @Schema(name = "The id of the parent memo (if applicable)", required = false)
    @Column(name = "parent_id")
    private Long parentId;

    @Schema(name = "The id of the activity (if applicable)", required = false)
    @Column(name = "activity_id")
    private Long activityId;

    @Schema(name = "The process activity type", required = true)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityType type;

    @Schema(name = "The activity code", required = false)
    @Column(name = "code")
    private String code;

    @Schema(name = "The status of the memo (disqualified, success or error)", required = false)
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private EventMemoStatus status;

    @Schema(name = "The status description of the memo (error message)", required = false)
    @Column(name = "status_description", length = 4000)
    private String statusDescription;

    @Schema(name = "The system memo", required = false)
    @Column(name = "system_memo", length = 500)
    private String systemMemo;

    @Schema(name = "The entity name", required = false)
    @Column(name = "entity_name", length = 500)
    private String entityName;

    @Schema(name = "The entity id", required = false)
    @Column(name = "entity_id")
    private String entityId;

    @Schema(name = "The process memo parameters", required = false)
    @Column(name = "parameters", columnDefinition = "oid")
    @Singular
    @Convert(converter = JpaMapStringObjectJsonConverter.class)
    @Lob
    private Map<String, Object> parameters;

    @Schema(name = "The memo context", required = false)
    @Column(name = "context", columnDefinition = "oid")
    @Convert(converter = JpaMapStringObjectJsonConverter.class)
    @Lob
    private Map<String, Object> context;

    @Schema(name = "The memo visibility", required = false)
    @Column(name = "visibility")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibility;

    @Schema(name = "The privacy id", required = false)
    @Column(name = "privacy_id")
    private Long privacyId;

}
