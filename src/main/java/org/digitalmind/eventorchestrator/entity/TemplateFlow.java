package org.digitalmind.eventorchestrator.entity;


import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import static org.digitalmind.eventorchestrator.entity.TemplateFlow.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME)
@EntityListeners({AuditingEntityListener.class})

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

@Schema(name = "TemplateFlow", description = "Process template flows defined in the signing process.")
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
    @Schema(name = "Unique id of the template signFlow configuration", required = false)
    private Long id;

    @NotNull
    @Column(name = "flow_template")
    @Schema(name = "Process flow template. Based on this different attribute the process flow will be executed differently ", required = true)
    private String flowTemplate;

    @Schema(name = "The signFlow code", required = false)
    @Column(name = "usecase")
    private String usecase;

}
