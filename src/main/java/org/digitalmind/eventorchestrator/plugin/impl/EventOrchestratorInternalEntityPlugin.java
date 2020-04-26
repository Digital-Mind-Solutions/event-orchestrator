package org.digitalmind.eventorchestrator.plugin.impl;

import org.digitalmind.eventorchestrator.entity.*;
import org.digitalmind.eventorchestrator.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventOrchestratorInternalEntityPlugin extends EventOrchestratorAbstractEntityPlugin {

    private final EventMemoRepository eventMemoRepository;
    private final EventActivityRepository eventActivityRepository;
    private final EventDirectiveRepository eventDirectiveRepository;
    private final TemplateActivityRepository templateActivityRepository;
    private final TemplateActivityActivatorRepository templateActivityActivatorRepository;
    private final TemplateFlowRepository templateFlowRepository;

    @Autowired
    public EventOrchestratorInternalEntityPlugin(
            EventMemoRepository eventMemoRepository,
            EventActivityRepository eventActivityRepository,
            EventDirectiveRepository eventDirectiveRepository,
            TemplateActivityRepository templateActivityRepository,
            TemplateActivityActivatorRepository templateActivityActivatorRepository,
            TemplateFlowRepository templateFlowRepository
    ) {
        this.eventMemoRepository = eventMemoRepository;
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

    @Override
    public boolean supportsInternal(String name) {

        if (EventMemo.class.getCanonicalName().equals(name) || EventMemo.class.getSimpleName().equals(name)) {
            return true;
        }

        if (EventActivity.class.getCanonicalName().equals(name) || EventActivity.class.getSimpleName().equals(name)) {
            return true;
        }

        if (EventDirective.class.getCanonicalName().equals(name) || EventDirective.class.getSimpleName().equals(name)) {
            return true;
        }

        if (TemplateActivity.class.getCanonicalName().equals(name) || TemplateActivity.class.getSimpleName().equals(name)) {
            return true;
        }

        if (TemplateActivityActivator.class.getCanonicalName().equals(name) || TemplateActivityActivator.class.getSimpleName().equals(name)) {
            return true;
        }

        if (TemplateFlow.class.getCanonicalName().equals(name) || TemplateFlow.class.getSimpleName().equals(name)) {
            return true;
        }

        return false;
    }

    @Override
    public Object getEntityInternal(String name, String id) {

        if (EventMemo.class.getCanonicalName().equals(name) || EventMemo.class.getSimpleName().equals(name)) {
            return eventMemoRepository.findById(Long.valueOf(id)).orElse(null);
        }

        if (EventActivity.class.getCanonicalName().equals(name) || EventActivity.class.getSimpleName().equals(name)) {
            return eventActivityRepository.findById(Long.valueOf(id)).orElse(null);
        }

        if (EventDirective.class.getCanonicalName().equals(name) || EventDirective.class.getSimpleName().equals(name)) {
            return eventDirectiveRepository.findById(Long.valueOf(id)).orElse(null);
        }

        if (TemplateActivity.class.getCanonicalName().equals(name) || TemplateActivity.class.getSimpleName().equals(name)) {
            return templateActivityRepository.findById(Long.valueOf(id)).orElse(null);
        }

        if (TemplateActivityActivator.class.getCanonicalName().equals(name) || TemplateActivityActivator.class.getSimpleName().equals(name)) {
            return templateActivityActivatorRepository.findById(Long.valueOf(id)).orElse(null);
        }

        if (TemplateFlow.class.getCanonicalName().equals(name) || TemplateFlow.class.getSimpleName().equals(name)) {
            return templateFlowRepository.findById(Long.valueOf(id)).orElse(null);
        }

        return null;
    }

    @Override
    public String getEntityAlias(String name) {
        return getEntityAliasAsSimpleName(name);
    }
}
