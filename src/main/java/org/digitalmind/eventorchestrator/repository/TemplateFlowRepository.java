package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.TemplateFlow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateFlowRepository extends JpaRepository<TemplateFlow, Long> {

    List<TemplateFlow> findByFlowTemplateOrderById(String flowTemplate);

}
