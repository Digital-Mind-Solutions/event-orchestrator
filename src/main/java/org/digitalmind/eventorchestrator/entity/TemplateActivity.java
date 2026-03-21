package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionMode;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType;
import org.digitalmind.eventorchestrator.enumeration.EventActivityType;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
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

@Schema(description = "Activity templates defined in the signing process.")
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
    @Schema(description = "Unique id of the template")
    private Long id;

    @Schema(description = "The activity type", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityType type;

    @Schema(description = "The activity execution type", requiredMode = Schema.RequiredMode.REQUIRED)
    @Column(name = "execution_type", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityExecutionType executionType;

    @Schema(description = "The activity code")
    @Column(name = "code")
    private String code;

    @Schema(description = "The qualifier SPEL that must evaluate to boolean")
    @Column(name = "qualifier_expr2", length = 3000)
    private String qualifierExpr2;

    @Schema(description = "The executor HANDLEBARS that must give the activity parameters")
    @Column(name = "executor_expr2", length = 3000)
    private String executorExpr2;

    @Schema(description = "The sub status SPEL")
    @Column(name = "system_memo_expr2", length = 1000)
    private String systemMemoExpr2;

    @Schema(description = "The entity name SPEL")
    @Column(name = "entity_name_expr", length = 1000)
    private String entityNameExpr;

    @Schema(description = "The entity id SPEL")
    @Column(name = "entity_id_expr", length = 1000)
    private String entityIdExpr;

    @Schema(description = "The process activity parameters SPEL")
    @Column(name = "parameters_expr", length = 3000)
    private String parametersExpr;

    @OneToMany(targetEntity = TemplateActivityActivator.class, mappedBy = "templateId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Singular
    private List<TemplateActivityActivator> templateActivityActivators = new LinkedList<>();

    @Schema(description = "The memo success visibility")
    @Column(name = "visibility_success")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibilitySuccess;

    @Schema(description = "The memo default visibility")
    @Column(name = "visibility_default")
    @Enumerated(EnumType.ORDINAL)
    private EventVisibility visibilityDefault;

    @Schema(description = "The privacy id SPEL")
    @Column(name = "privacy_id_expr", length = 1000)
    private String privacyIdExpr;

    @Schema(description = "The priority id SPEL (lower is more important)")
    @Column(name = "priority_expr", length = 1000)
    private String priorityExpr;

    @Schema(description = "The event activity execution type")
    @Column(name = "execution_mode", length = 50)
    @Enumerated(EnumType.STRING)
    private EventActivityExecutionMode executionMode;


}
