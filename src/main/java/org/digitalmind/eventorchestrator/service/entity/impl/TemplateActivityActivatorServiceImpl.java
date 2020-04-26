package org.digitalmind.eventorchestrator.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.entity.TemplateActivityActivator;
import org.digitalmind.eventorchestrator.repository.TemplateActivityActivatorRepository;
import org.digitalmind.eventorchestrator.service.entity.TemplateActivityActivatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("templateActivityActivatorService")
@Slf4j
@Transactional
public class TemplateActivityActivatorServiceImpl implements TemplateActivityActivatorService {

    private final TemplateActivityActivatorRepository templateActivityActivatorRepository;

    @Autowired
    public TemplateActivityActivatorServiceImpl(TemplateActivityActivatorRepository templateActivityActivatorRepository) {
        this.templateActivityActivatorRepository = templateActivityActivatorRepository;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<TemplateActivityActivator> findByParentCodeAndParentStatusOrderByPriorityAscIdAsc(String parentCode, String parentStatus) {
        return templateActivityActivatorRepository.findByParentCodeAndParentStatusOrderByPriorityAscIdAsc(parentCode, parentStatus);
    }

}
