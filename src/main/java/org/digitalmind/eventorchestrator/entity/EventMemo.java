package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.converter.JpaMapJsonConverter;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventMemoStatus;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
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
@ApiModel(value = "ProcessMemo", description = "Process memo")
@ToString(callSuper = true)
public class EventMemo extends ContextVersionableAuditModel implements ProcessAuditModel, IdModel<Long> {

    public static final String TABLE_NAME = "process_memo";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Unique id of the process activity", required = false)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty(value = "The name of the process", required = false)
    @Column(name = "process_name")
    private String processName;

    @ApiModelProperty(value = "The id of the process", required = false)
    @Column(name = "process_id")
    //@NonNull
    private Long processId;

    @ApiModelProperty(value = "The id of the parent memo (if applicable)", required = false)
    @Column(name = "parent_id")
    private Long parentId;

    @ApiModelProperty(value = "The process activity type", required = true)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityType type;

    @ApiModelProperty(value = "The activity code", required = false)
    @Column(name = "code")
    private String code;

    @ApiModelProperty(value = "The status of the memo (disqualified, success or error)", required = false)
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private EventMemoStatus status;

    @ApiModelProperty(value = "The status description of the memo (error message)", required = false)
    @Column(name = "status_description", length = 2000)
    private String statusDescription;

    @ApiModelProperty(value = "The system memo", required = false)
    @Column(name = "system_memo")
    private String systemMemo;

    @ApiModelProperty(value = "The entity name", required = false)
    @Column(name = "entity_name")
    private String entityName;

    @ApiModelProperty(value = "The entity id", required = false)
    @Column(name = "entity_id")
    private String entityId;

    @ApiModelProperty(value = "The process memo parameters", required = false)
    @Column(name = "parameters")
    @Singular
    @Convert(converter = JpaMapJsonConverter.class)
    @Lob
    private Map<String, Object> parameters;

    @ApiModelProperty(value = "The memo context", required = false)
    @Column(name = "context")
    @Convert(converter = JpaMapJsonConverter.class)
    @Lob
    private Map<String, Object> context;

    @ApiModelProperty(value = "The memo visibility", required = false)
    @Column(name = "visibility")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibility;

    @ApiModelProperty(value = "The privacy id", required = false)
    @Column(name = "privacy_id")
    private Long privacyId;

}
