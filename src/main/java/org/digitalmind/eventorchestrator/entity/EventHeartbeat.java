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

import static org.digitalmind.eventorchestrator.entity.EventHeartbeat.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME,
        indexes = {
                @Index(
                        name = TABLE_NAME + "_ux1",
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
@Schema(name = "EventHeartBeat", description = "Event heartbeat.")
@ToString(callSuper = true)
public class EventHeartbeat extends ContextAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "process_heartbeat";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(name = "Unique id of the heartbeat", required = false)
    @Column(name = "id")
    private Long id;

    @Schema(name = "The node processing the activity", required = false)
    @Column(name = "execution_node")
    @NotNull
    private String executionNode;

}

