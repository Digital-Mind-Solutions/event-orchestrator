package org.digitalmind.eventorchestrator.service.entity.impl;

import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.entity.TemplateActivity;
import org.digitalmind.eventorchestrator.repository.TemplateActivityRepository;
import org.digitalmind.eventorchestrator.service.entity.TemplateActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("templateActivityService")
@Slf4j
@Transactional
public class TemplateActivityServiceImpl implements TemplateActivityService {

    private final TemplateActivityRepository templateActivityRepository;

    @Autowired
    public TemplateActivityServiceImpl(TemplateActivityRepository templateActivityRepository) {
        this.templateActivityRepository = templateActivityRepository;
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.NOT_SUPPORTED)
    public TemplateActivity getById(Long id) {
        return templateActivityRepository.findById(id).orElse(null);
    }

}
