package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.EventDirective;
import org.digitalmind.eventorchestrator.enumeration.EventDirectiveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventDirectiveRepository extends JpaRepository<EventDirective, Long> {

    public List<EventDirective> findByEntityNameAndTypeOrderByPriority(String entityName, EventDirectiveType type);

}

