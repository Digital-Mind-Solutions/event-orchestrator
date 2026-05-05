package org.digitalmind.eventorchestrator.plugin.impl;

import org.digitalmind.eventorchestrator.entity.*;
import org.digitalmind.eventorchestrator.repository.*;
import org.digitalmind.eventorchestrator.service.entity.EventMemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventOrchestratorInternalEntityPlugin extends EventOrchestratorAbstractEntityPlugin {

    public static final String EVENT_MEMO = "EventMemo";
    public static final String EVENT_ACTIVITY = "EventActivity";
    public static final String EVENT_DIRECTIVE = "EventDirective";
    public static final String TEMPLATE_ACTIVITY = "TemplateActivity";
    public static final String TEMPLATE_ACTIVITY_ACTIVATOR = "TemplateActivityActivator";
    public static final String TEMPLATE_FLOW = "TemplateFlow";

    private final EventMemoService eventMemoService;
    private final EventActivityRepository eventActivityRepository;
    private final EventDirectiveRepository eventDirectiveRepository;
    private final TemplateActivityRepository templateActivityRepository;
    private final TemplateActivityActivatorRepository templateActivityActivatorRepository;
    private final TemplateFlowRepository templateFlowRepository;

    @Autowired
    public EventOrchestratorInternalEntityPlugin(
            EventMemoService eventMemoService,
            EventActivityRepository eventActivityRepository,
            EventDirectiveRepository eventDirectiveRepository,
            TemplateActivityRepository templateActivityRepository,
            TemplateActivityActivatorRepository templateActivityActivatorRepository,
            TemplateFlowRepository templateFlowRepository
    ) {
        this.eventMemoService = eventMemoService;
        this.eventActivityRepository = eventActivityRepository;
        this.eventDirectiveRepository = eventDirectiveRepository;
        this.templateActivityRepository = templateActivityRepository;
        this.templateActivityActivatorRepository = templateActivityActivatorRepository;
        this.templateFlowRepository = templateFlowRepository;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }


    String normalizeClassName(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx + 1) : name;
    }

    @Override
    public boolean supportsInternal(String name) {

        switch (normalizeClassName(name)) {
            case EVENT_MEMO:
            case EVENT_ACTIVITY:
            case EVENT_DIRECTIVE:
            case TEMPLATE_ACTIVITY:
            case TEMPLATE_ACTIVITY_ACTIVATOR:
            case TEMPLATE_FLOW:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getEntityInternal(String name, String indentifier) {
        String entityName = normalizeClassName(name);
        switch (entityName) {

            case EVENT_MEMO: {
                EventMemoId key = EventMemoId.fromIdentifier(indentifier);
                EventMemo entity = eventMemoService.findById(key);
                return entity;
            }
            case EVENT_ACTIVITY: {
                Long id = Long.parseLong(indentifier);
                EventActivity entity = eventActivityRepository.findById(id).orElse(null);
                return entity;
            }
            case EVENT_DIRECTIVE: {
                Long id = Long.parseLong(indentifier);
                EventDirective entity = eventDirectiveRepository.findById(id).orElse(null);
                return entity;
            }
            case TEMPLATE_ACTIVITY: {
                Long id = Long.parseLong(indentifier);
                TemplateActivity entity = templateActivityRepository.findById(id).orElse(null);
                return entity;
            }
            case TEMPLATE_ACTIVITY_ACTIVATOR: {
                Long id = Long.parseLong(indentifier);
                TemplateActivityActivator entity = templateActivityActivatorRepository.findById(id).orElse(null);
                return entity;
            }
            case TEMPLATE_FLOW: {
                Long id = Long.parseLong(indentifier);
                TemplateFlow entity = templateFlowRepository.findById(id).orElse(null);
                return entity;
            }
            default:
                return null;
        }

    }

    @Override
    public String getEntityAlias(String name) {
        return normalizeClassName(getEntityAliasAsSimpleName(name));
    }
}
