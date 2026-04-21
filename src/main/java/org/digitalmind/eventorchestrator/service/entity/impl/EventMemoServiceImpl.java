package org.digitalmind.eventorchestrator.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.entity.EventMemo;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.digitalmind.eventorchestrator.entity.MemoId;
import org.digitalmind.eventorchestrator.repository.EventMemoRepository;
import org.digitalmind.eventorchestrator.service.entity.EventMemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service("processMemoService")
@Slf4j
@Transactional
public class EventMemoServiceImpl implements EventMemoService {

    private final EventMemoRepository eventMemoRepository;

    @Autowired
    public EventMemoServiceImpl(EventMemoRepository eventMemoRepository) {
        this.eventMemoRepository = eventMemoRepository;
    }

    @Override
    public EventMemo save(EventMemo eventMemo) {
        if (eventMemo.getPartitionKey() == null) {
            eventMemo.setPartitionKey(0);
        }
        return eventMemoRepository.save(eventMemo);
    }

    @Override
    public Page<EventMemo> findAllByProcessId(Long processId, Integer partitionKey, Pageable pageRequest) {
        return eventMemoRepository.findAllByProcessIdAndPartitionKey(processId, partitionKey, pageRequest);
    }

    @Override
    public Page<EventMemo> findAllByProcessIdAndVisibleAndPrivacyId(
            Long processId,
            Integer partitionKey,
            Set<EventVisibility> eventVisibilitySet,
            Long privacyId,
            Pageable pageRequest
    ) {
        return eventMemoRepository.findAllByProcessIdAndVisibleAndPrivacyId(
                processId, partitionKey, eventVisibilitySet, privacyId, pageRequest);
    }

    @Override
    public List<EventMemo> saveAll(Iterable<EventMemo> eventMemos) {
        for (EventMemo m : eventMemos) {
            if (m.getPartitionKey() == null) {
                m.setPartitionKey(0);
            }
        }
        return eventMemoRepository.saveAll(eventMemos);
    }

    @Override
    public EventMemo findById(MemoId id) {
        if (id == null || id.getId() == null || id.getPartitionKey() == null) {
            return null;
        }
        return eventMemoRepository.findById(id).orElse(null);
    }
}
