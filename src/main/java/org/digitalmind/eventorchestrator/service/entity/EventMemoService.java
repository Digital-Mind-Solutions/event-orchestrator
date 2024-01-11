package org.digitalmind.eventorchestrator.service.entity;

import org.digitalmind.eventorchestrator.entity.EventMemo;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface EventMemoService {

    EventMemo save(EventMemo eventMemo);

    EventMemo findById(Long memoId);

    Page<EventMemo> findAllByProcessId(Long processId, Pageable pageRequest);

    Page<EventMemo> findAllByProcessIdAndVisibleAndPrivacyId(
            Long processId, Set<EventVisibility> eventVisibilitySet, Long privacyId, Pageable pageRequest
    );

    List<EventMemo> saveAll(Iterable<EventMemo> eventMemos);

}
