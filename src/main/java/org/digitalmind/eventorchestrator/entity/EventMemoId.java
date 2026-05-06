package org.digitalmind.eventorchestrator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.digitalmind.buildingblocks.core.jpautils.entity.PartitionedIdModel;

import java.io.Serializable;

/**
 * Valoare de identificare logică (parsare din string {@code partitionKey~id}, delimiter {@link PartitionedIdModel#PARTITION_KEY_DELIMITER}); nu {@code @IdClass}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMemoId implements Serializable, PartitionedIdModel<Integer, Long> {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Integer partitionKey;

    public static EventMemoId of(Integer partitionKey, Long id) {
        return new EventMemoId(id, partitionKey);
    }

    public static EventMemoId fromIdentifier(String identifier) {
        return PartitionedIdModel.fromString(
                identifier,
                (pk, memoId) ->
                        new EventMemoId(Long.parseLong(memoId), Integer.parseInt(pk))
        );
    }
}
