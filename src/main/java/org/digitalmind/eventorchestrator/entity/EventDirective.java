package org.digitalmind.eventorchestrator.entity;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.eventorchestrator.enumeration.EventDirectiveType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import static org.digitalmind.eventorchestrator.entity.EventDirective.*;

@Entity
@Table(
        name = TABLE_NAME,
        indexes = {
                @Index(name = TABLE_IX_CREATED_AT, columnList = "created_at", unique = false),
                @Index(name = TABLE_IX_UPDATED_AT, columnList = "updated_at", unique = false),
                @Index(
                        name = TABLE_SHORT_NAME + "_ux01",
                        columnList = "entity_name, type, priority",
                        unique = true
                )
        }
)

@EntityListeners({AuditingEntityListener.class})

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

@Schema(description = "Event directives for reacting programmatically to JPA evens.")
@JsonPropertyOrder(
        {
                "id", "entityName", "type",
                "qualifier", "executor", "description",
                "priority",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
public class EventDirective extends ContextVersionableAuditModel {
    public static final String TABLE_NAME = "configuration_directive";
    static final String TABLE_SHORT_NAME = "conf_dir";
    static final String TABLE_IX_CREATED_AT = TABLE_SHORT_NAME + "_ixcreat";
    static final String TABLE_IX_UPDATED_AT = TABLE_SHORT_NAME + "_ixupdat";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique id of the event configuration directive")
    @Column(name = "id")
    private Long id;

    @Schema(description = "The entity name", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "entity_name", length = 500)
    @NonNull
    private String entityName;

    @Schema(description = "The configuration directive type", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    @NonNull
    private EventDirectiveType type;

    @Schema(description = "The qualification rule", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "qualifier", length=4000)
    @NonNull
    private String qualifier;

    @Schema(description = "The execution rule", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "executor", length=4000)
    @NonNull
    private String executor;

    @Schema(description = "The qualification rule description", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "description", length=4000)
    private String description;

    @Schema(description = "The directive priority ", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "priority")
    private int priority;
}
