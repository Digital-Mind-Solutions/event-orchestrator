package org.digitalmind.eventorchestrator.entity;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.eventorchestrator.enumeration.EventDirectiveType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import static org.digitalmind.eventorchestrator.entity.EventDirective.TABLE_NAME;


@Entity
@Table(
        name = TABLE_NAME,
        uniqueConstraints = {
                @UniqueConstraint(
                        name = TABLE_NAME + "_ux1",
                        columnNames = {"entity_name", "type", "priority"}
                )
        }
)
@EntityListeners({AuditingEntityListener.class})

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

@Schema(name = "EventDirective", description = "Event directives for reacting programmatically to JPA evens.")
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "Unique id of the event configuration directive", required = false)
    @Column(name = "id")
    private Long id;

    @Schema(name = "The entity name", required = true)
    @Column(name = "entity_name", length = 500)
    @NonNull
    private String entityName;

    @Schema(name = "The configuration directive type", required = true)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    @NonNull
    private EventDirectiveType type;

    @Schema(name = "The qualification rule", required = true)
    @Column(name = "qualifier", length=4000)
    @NonNull
    private String qualifier;

    @Schema(name = "The execution rule", required = true)
    @Column(name = "executor", length=4000)
    @NonNull
    private String executor;

    @Schema(name = "The qualification rule description", required = true)
    @Column(name = "description", length=4000)
    private String description;

    @Schema(name = "The directive priority ", required = true)
    @Column(name = "priority")
    private int priority;
}
