package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.TemplateActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateActivityRepository extends JpaRepository<TemplateActivity, Long> {
}
