package org.digitalmind.eventorchestrator.service.entity;

import org.digitalmind.eventorchestrator.entity.EventRetry;
import org.digitalmind.eventorchestrator.entity.TemplateActivityActivator;

import java.util.List;

public interface EventRetryService {

    List<EventRetry> findByCodeOrderByFromValueAsc(String code);

}
