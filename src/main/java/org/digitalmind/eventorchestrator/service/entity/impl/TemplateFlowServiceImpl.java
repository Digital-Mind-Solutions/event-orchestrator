package org.digitalmind.eventorchestrator.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.entity.TemplateFlow;
import org.digitalmind.eventorchestrator.repository.TemplateFlowRepository;
import org.digitalmind.eventorchestrator.service.entity.TemplateFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("templateFlowService")
@Slf4j
@Transactional
public class TemplateFlowServiceImpl implements TemplateFlowService {
    private final TemplateFlowRepository templateFlowRepository;

    @Autowired
    public TemplateFlowServiceImpl(TemplateFlowRepository templateFlowRepository) {
        this.templateFlowRepository = templateFlowRepository;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public List<TemplateFlow> findBySignTemplateOrderById(String signTemplate) {
        return templateFlowRepository.findBySignTemplateOrderById(signTemplate);
    }

}
