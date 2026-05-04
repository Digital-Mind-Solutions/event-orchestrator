package org.digitalmind.eventorchestrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdCreateModel;
import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdModel;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMemoId implements PartitionedIdModel<Integer, Long>, PartitionedIdCreateModel<Integer, Long, EventMemoId>, Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "partition_key", nullable = false)
    private Integer partitionKey;

    @Column(name = "id", nullable = false)
    private Long id;

    public static EventMemoId of(Integer partitionKey, Long id) {
        return new EventMemoId(partitionKey, id);
    }

    public static EventMemoId fromIdentifier(String identifier) {
        return PartitionedIdModel.fromString(
                identifier,
                (partitionKey, id) ->
                        new EventMemoId(Integer.parseInt(partitionKey), Long.parseLong(id)
                        )
        );
    }

    @Override
    public EventMemoId createKey(Integer partitionKey, Long id) {
        return of(partitionKey, id);
    }

}
