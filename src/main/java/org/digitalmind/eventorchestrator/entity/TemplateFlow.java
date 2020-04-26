package org.digitalmind.eventorchestrator.entity;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static org.digitalmind.eventorchestrator.entity.TemplateFlow.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME)
@EntityListeners({AuditingEntityListener.class})

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

@ApiModel(value = "TemplateFlow", description = "Process template flows defined in the signing process.")
@JsonPropertyOrder(
        {
                "id", "flowTemplate", "usecase",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
public class TemplateFlow extends ContextVersionableAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "template_flow";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    @ApiModelProperty(value = "Unique id of the template signFlow configuration", required = false)
    private Long id;

    @NotNull
    @Column(name = "flow_template")
    @ApiModelProperty(value = "Process flow template. Based on this different attribute the process flow will be executed differently ", required = true)
    private String flowTemplate;

    @ApiModelProperty(value = "The signFlow code", required = false)
    @Column(name = "usecase")
    private String usecase;

}
