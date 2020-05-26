package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.EventRetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRetryRepository extends JpaRepository<EventRetry, Long> {

    List<EventRetry> findByCodeOrderByFromValueAsc(String code);

}
