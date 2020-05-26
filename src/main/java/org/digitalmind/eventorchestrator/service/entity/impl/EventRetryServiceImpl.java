package org.digitalmind.eventorchestrator.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.entity.EventRetry;
import org.digitalmind.eventorchestrator.repository.EventRetryRepository;
import org.digitalmind.eventorchestrator.service.entity.EventRetryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("eventRetryService")
@Slf4j
@Transactional
public class EventRetryServiceImpl implements EventRetryService {

    private final EventRetryRepository eventRetryRepository;

    @Autowired
    public EventRetryServiceImpl(EventRetryRepository eventRetryRepository) {
        this.eventRetryRepository = eventRetryRepository;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<EventRetry> findByCodeOrderByFromValueAsc(String code) {
        return eventRetryRepository.findByCodeOrderByFromValueAsc(code);
    }

}
