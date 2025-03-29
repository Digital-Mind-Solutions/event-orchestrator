package org.digitalmind.eventorchestrator.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.digitalmind.buildingblocks.core.jpautils.entity.ContextVersionableAuditModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.IdModel;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.digitalmind.eventorchestrator.entity.TemplateActivityActivator.TABLE_NAME;


@Entity
@Table(name = TABLE_NAME)
@EntityListeners({AuditingEntityListener.class})

@Data
@NoArgsConstructor
//@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)

@Schema(name = "TemplateActivityActivator", description = "Template activator used to qualify the activity to a process execution.")
@JsonPropertyOrder(
        {
                "id", "type", "code", "statusExpr", "subStatusExpr",
                "qualifier_expr", "executor_expr", "system_memo_expr",
                "createdAt", "createdBy", "updatedAt", "updatedBy"
        }
)
public class TemplateActivityActivator extends ContextVersionableAuditModel implements IdModel<Long> {

    public static final String TABLE_NAME = "template_activator";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    @Schema(name = "Unique id of the template activator", required = false)
    private Long id;

    @Column(name = "template_id")
    @NonNull
    @Schema(name = "The id of the template activity", required = false)
    @Setter(AccessLevel.PROTECTED)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Long templateId;

    @JoinColumn(name = "template_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private TemplateActivity templateActivity;

    @Schema(name = "The parent process memo code that can activate this template", required = false)
    @Column(name = "parent_code")
    private String parentCode;

    @Schema(name = "The parent process memo status that can activate this template", required = false)
    @Column(name = "parent_status")
    private String parentStatus;

    @Schema(name = "The order for activating this template", required = false)
    @Column(name = "priority")
    private int priority;


//    @Schema(name = "The parent activity substatus that can activate this template", required = false)
//    @Column(name = "parent_sub_status")
//    @JsonIgnore
//    private String parentSubStatus;

    @Schema(name = "The usecase that can activate this template", required = false)
    @Column(name = "usecase")
    private String usecase;

    @Schema(name = "The date expression when the activity is supposed to be executed", required = false)
    @Column(name = "planned_date_expr")
    private String plannedDateExpr;

    @Schema(name = "The qualifier SPEL that must evaluate to boolean", required = false)
    @Column(name = "qualifier_expr")
    private String qualifierExpr;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public void setParentSubStatuses(String[] statuses) {
        if (statuses == null || statuses.length == 0) {
            this.setParentStatus(null);
        } else {
            this.setParentStatus(Arrays.stream(statuses).collect(Collectors.joining(",")));
        }
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public String[] getParentSubStatuses() {
        if (this.parentStatus == null || this.parentStatus.isEmpty()) {
            return null;
        } else {
            return this.parentStatus.split(",");
        }
    }
}
