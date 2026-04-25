package org.digitalmind.eventorchestrator.service.entity;

import org.digitalmind.eventorchestrator.entity.EventMemo;
import org.digitalmind.eventorchestrator.entity.EventMemoId;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventMemoService {


    EventMemo findById(EventMemoId eventMemoId);

    Page<EventMemo> findAllByKey_PartitionKeyAndProcessIdOrderByKey_IdDesc(Integer partitionKey, Long processId, Pageable pageRequest);

    Page<EventMemo> findAllByKey_PartitionKeyAndProcessIdAndVisibleAndPrivacyId(
            Integer partitionKey, Long processId, Set<EventVisibility> eventVisibilitySet, Long privacyId, Pageable pageRequest
    );

    EventMemo save(EventMemo eventMemo);

    List<EventMemo> saveAll(Iterable<EventMemo> eventMemos);

}
