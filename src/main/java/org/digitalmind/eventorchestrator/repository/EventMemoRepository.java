package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.EventMemo;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface EventMemoRepository extends JpaRepository<EventMemo, Long> {

    EventMemo getByContextId(String contextId);

    Page<EventMemo> findAllByProcessId(Long processId, Pageable pageRequest);

    @Query(
            "SELECT EM FROM EventMemo EM " +
                    "WHERE EM.processId = :processId " +
                    "  AND (:privacyId IS NULL OR EM.privacyId IS NULL OR EM.privacyId = :privacyId) " +
                    "  AND (EM.visibility IN :eventVisibilitySet)" +
                    "ORDER BY EM.createdAt DESC"
    )
    Page<EventMemo> findAllByProcessIdAndVisibleAndPrivacyId(
            @Param("processId") Long processId,
            @Param("eventVisibilitySet") Set<EventVisibility> eventVisibilitySet,
            @Param("privacyId") Long privacyId,
            Pageable pageable
    );
}
