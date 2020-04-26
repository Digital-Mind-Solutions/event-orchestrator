package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.digitalmind.buildingblocks.core.jpaauditor.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpaauditor.entity.IdModel;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionMode;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

import static org.digitalmind.eventorchestrator.entity.TemplateActivity.TABLE_NAME;

@Entity
@Table(name = TABLE_NAME)
@EntityListeners({AuditingEntityListener.class})

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

@ApiModel(value = "TemplateActivity", description = "Activity templates defined in the signing process.")
@JsonPropertyOrder(
        {
                "id", "type", "code", "statusExpr", "subStatusExpr",
                "qualifier_expr", "executor_expr", "system_memo_expr",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
public class TemplateActivity extends ContextVersionableAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "template_activity";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    @ApiModelProperty(value = "Unique id of the template", required = false)
    private Long id;

    @ApiModelProperty(value = "The activity type", required = true)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityType type;

    @ApiModelProperty(value = "The activity execution type", required = true)
    @Column(name = "execution_type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityExecutionType executionType;

    @ApiModelProperty(value = "The activity code", required = false)
    @Column(name = "code")
    private String code;

    @ApiModelProperty(value = "The qualifier SPEL that must evaluate to boolean", required = false)
    @Column(name = "qualifier_expr2", length = 5000)
    private String qualifierExpr2;

    @ApiModelProperty(value = "The executor HANDLEBARS that must give the activity parameters", required = false)
    @Column(name = "executor_expr2", length = 5000)
    private String executorExpr2;

    @ApiModelProperty(value = "The sub status SPEL", required = false)
    @Column(name = "system_memo_expr2", length = 5000)
    private String systemMemoExpr2;

    @ApiModelProperty(value = "The entity name SPEL", required = false)
    @Column(name = "entity_name_expr", length = 5000)
    private String entityNameExpr;

    @ApiModelProperty(value = "The entity id SPEL", required = false)
    @Column(name = "entity_id_expr", length = 1000)
    private String entityIdExpr;

    @ApiModelProperty(value = "The process activity parameters SPEL", required = false)
    @Column(name = "parameters_expr", length = 10000)
    private String parametersExpr;

    @OneToMany(targetEntity = TemplateActivityActivator.class, mappedBy = "templateId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Singular
    private List<TemplateActivityActivator> templateActivityActivators = new LinkedList<>();

    @ApiModelProperty(value = "The memo success visibility", required = false)
    @Column(name = "visibility_success")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibilitySuccess;

    @ApiModelProperty(value = "The memo default visibility", required = false)
    @Column(name = "visibility_default")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibilityDefault;

    @ApiModelProperty(value = "The privacy id SPEL", required = false)
    @Column(name = "privacy_id_expr", length = 10000)
    private String privacyIdExpr;

    @ApiModelProperty(value = "The priority id SPEL (lower is more important)", required = false)
    @Column(name = "priority_expr", length = 10000)
    private String priorityExpr;

    @ApiModelProperty(value = "The event activity execution type", required = false)
    @Column(name = "execution_mode", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityExecutionMode executionMode;


}
