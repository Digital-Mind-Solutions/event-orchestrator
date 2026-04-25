package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import static org.digitalmind.eventorchestrator.entity.EventHeartbeat.*;

@Entity
@Table(name = TABLE_NAME,
        indexes = {
                @Index(name = TABLE_IX_CREATED_AT, columnList = "created_at", unique = false),
                @Index(name = TABLE_IX_UPDATED_AT, columnList = "updated_at", unique = false),
                @Index(
                        name = TABLE_SHORT_NAME + "_ux1",
                        columnList = "execution_node",
                        unique = true
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
                "id",
                "executionNode",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@Schema(description = "Event heartbeat.")
@ToString(callSuper = true)
public class EventHeartbeat extends ContextAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "process_heartbeat";
    static final String TABLE_SHORT_NAME = "prc_hb";
    static final String TABLE_IX_CREATED_AT = TABLE_SHORT_NAME + "_ixcreat";
    static final String TABLE_IX_UPDATED_AT = TABLE_SHORT_NAME + "_ixupdat";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique id of the heartbeat")
    @Column(name = "id")
    private Long id;

    @Schema(description = "The node processing the activity")
    @Column(name = "execution_node")
    @NotNull
    private String executionNode;

}

