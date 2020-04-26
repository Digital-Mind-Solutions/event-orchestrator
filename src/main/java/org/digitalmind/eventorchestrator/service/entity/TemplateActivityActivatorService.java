package org.digitalmind.eventorchestrator.service.entity;


import org.digitalmind.eventorchestrator.entity.TemplateActivityActivator;

import java.util.List;

public interface TemplateActivityActivatorService {

    List<TemplateActivityActivator> findByParentCodeAndParentStatusOrderByPriorityAscIdAsc(String parentCode, String parentStatus);

}
