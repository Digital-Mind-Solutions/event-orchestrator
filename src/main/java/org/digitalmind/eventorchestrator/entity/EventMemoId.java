package org.digitalmind.eventorchestrator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdModel;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor

public class EventMemoId implements PartitionedIdModel<Integer, Long> {

    @Column(name = "[partition_key]", nullable = false)
    private Integer partitionKey;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "[id]", nullable = false)
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
}
