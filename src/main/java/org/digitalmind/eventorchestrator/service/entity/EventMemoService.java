package org.digitalmind.eventorchestrator.service.entity;

import org.digitalmind.eventorchestrator.entity.EventMemo;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.digitalmind.eventorchestrator.entity.MemoId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface EventMemoService {

    EventMemo save(EventMemo eventMemo);

    EventMemo findById(MemoId id);

    Page<EventMemo> findAllByProcessId(Long processId, Integer partitionKey, Pageable pageRequest);

    Page<EventMemo> findAllByProcessIdAndVisibleAndPrivacyId(
            Long processId,
            Integer partitionKey,
            Set<EventVisibility> eventVisibilitySet,
            Long privacyId,
            Pageable pageRequest
    );

    List<EventMemo> saveAll(Iterable<EventMemo> eventMemos);

}
