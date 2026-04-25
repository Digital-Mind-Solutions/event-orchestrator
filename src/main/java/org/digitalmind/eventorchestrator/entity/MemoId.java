package org.digitalmind.eventorchestrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdModel;

/**
 * Embedded composite PK for {@code process_memo} ({@code partition_key}, then {@code id}).
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemoId implements PartitionedIdModel<Integer, Long> {
    private static final long serialVersionUID = 1L;

    @Column(name = "partition_key", nullable = false)
    private Integer partitionKey;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    public static MemoId of(Integer partitionKey, Long id) {
        return new MemoId(partitionKey, id);
    }
}
