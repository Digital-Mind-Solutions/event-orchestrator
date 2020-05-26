package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.enumeration.EventRetryDelayType;
import org.digitalmind.eventorchestrator.enumeration.ExceptionType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

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
@ApiModel(value = "EventRetry", description = "Event retry")
@ToString(callSuper = true)
public class EventRetry extends ContextVersionableAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "process_retry";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "Unique id of the event retry definition", required = false)
    @Column(name = "id")
    private Long id;

    @ApiModelProperty(value = "The event code", required = true)
    @Column(name = "code")
    private String code;

    @ApiModelProperty(value = "The interval that applies for the policy", required = true)
    @Column(name = "from_value")
    private Integer fromValue;

    @ApiModelProperty(value = "The interval that applies for the policy", required = true)
    @Column(name = "to_value")
    private Integer toValue;

    @ApiModelProperty(value = "The exception type", required = true)
    @Column(name = "exception_type", length = 50)
    @Enumerated(EnumType.STRING)
    private ExceptionType exceptionType;

    @ApiModelProperty(value = "The exception delay type", required = true)
    @Column(name = "delay_type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventRetryDelayType delayType;

    @ApiModelProperty(value = "The retry delay in seconds", required = true)
    @Column(name = "delay")
    private Integer delay;

}
