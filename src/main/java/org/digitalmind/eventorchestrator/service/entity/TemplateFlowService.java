package org.digitalmind.eventorchestrator.service.entity;

import org.digitalmind.eventorchestrator.entity.TemplateFlow;

import java.util.List;

public interface TemplateFlowService {

    List<TemplateFlow> findByFlowTemplateOrderById(String signTemplate);

}
