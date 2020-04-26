package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.TemplateActivityActivator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateActivityActivatorRepository extends JpaRepository<TemplateActivityActivator, Long> {

    List<TemplateActivityActivator> findByParentCodeAndParentStatusOrderByPriorityAscIdAsc(String parentCode, String parentStatus);

}
