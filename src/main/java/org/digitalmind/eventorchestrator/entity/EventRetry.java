package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.enumeration.EventRetryDelayType;
import org.digitalmind.eventorchestrator.enumeration.ExceptionType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;

import static org.digitalmind.eventorchestrator.entity.EventRetry.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME,
        indexes = {
                @Index(
                        name = TABLE_NAME + "_ix1",
                        columnList = "code,from_value,to_value",
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
                "id", "code", "fromValue", "toValue",
                "exceptionType",
                "delayType", "delay",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
@Schema(description = "Event retry")
@ToString(callSuper = true)
public class EventRetry extends ContextVersionableAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "process_retry";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique id of the event retry definition")
    @Column(name = "id")
    private Long id;

    @Schema(description = "The event code", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "code")
    private String code;

    @Schema(description = "The interval that applies for the policy", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "from_value")
    private Integer fromValue;

    @Schema(description = "The interval that applies for the policy", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "to_value")
    private Integer toValue;

    @Schema(description = "The exception type", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "exception_type", length = 50)
    @Enumerated(EnumType.STRING)
    private ExceptionType exceptionType;

    @Schema(description = "The exception delay type", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "delay_type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventRetryDelayType delayType;

    @Schema(description = "The retry delay in seconds", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "delay")
    private Integer delay;

}
