package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.EventActivity;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface EventActivityRepository extends JpaRepository<EventActivity, Long> {

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query(
            "SELECT P FROM EventActivity P " +
                    "WHERE P.executionType = org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.PARALLEL " +
                    "  AND P.status IN ( " +
                    " org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING, " +
                    " org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING_RETRY " +
                    ") " +
                    "  AND P.plannedDate < :plannedDate " +
                    "  AND (P.retryDate IS NULL OR P.retryDate < :retryDate) " +
                    "ORDER BY P.priority, P.plannedDate, P.retryDate NULLS FIRST, P.id"
    )
        //PARALLEL              //NO DEPENDENCY ON EXECUTION
    List<EventActivity> findAllWithExecutionTypeParallel(@Param("plannedDate") Date plannedDate, @Param("retryDate") Date retryDate, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query(
            "SELECT P FROM EventActivity P " +
                    "WHERE P.executionType =  org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.SERIAL_PROCESS " +
                    "  AND P.status IN ( " +
                    "       org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING, " +
                    "       org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING_RETRY " +
                    "  ) " +
                    "  AND P.plannedDate < :plannedDate " +
                    "  AND (P.retryDate IS NULL OR P.retryDate < :retryDate) " +
                    "  AND NOT EXISTS(" +
                    " " +
                    "           SELECT C FROM EventActivity C " +
                    "           WHERE C.executionType = org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.SERIAL_PROCESS " +
                    "             AND C.plannedDate < P.plannedDate " +
                    "             AND C.processId = P.processId " +
                    ") " +
                    "ORDER BY P.priority, P.retryDate NULLS FIRST, P.id"
    )
        //SERIAL_PROCESS        //SERIALIZE PA WITH THE SAME PROCESS ID
    List<EventActivity> findAllWithExecutionTypeSerialProcess(@Param("plannedDate") Date plannedDate, @Param("retryDate") Date retryDate, Pageable pageable);


    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query(
            "SELECT P FROM EventActivity P " +
                    "WHERE P.executionType = org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.SERIAL_ENTITY " +
                    "  AND P.status IN ( " +
                    "       org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING, " +
                    "       org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING_RETRY " +
                    "  ) " +
                    "  AND P.plannedDate < :plannedDate " +
                    "  AND (P.retryDate IS NULL OR P.retryDate < :retryDate) " +
                    "  AND NOT EXISTS(" +
                    " " +
                    "           SELECT C FROM EventActivity C " +
                    "           WHERE C.executionType = org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.SERIAL_ENTITY " +
                    "             AND C.plannedDate < P.plannedDate " +
                    "             AND C.processId = P.processId " +
                    "             AND C.entityId = P.entityId " +
                    "             AND C.entityName = P.entityName " +
                    ") " +
                    "ORDER BY P.priority, P.retryDate NULLS FIRST, P.id"
    )
        //SERIAL_ENTITY,        //SERIALIZE PA WITH THE SAME PROCESS ID, ENTITY ID AND ENTITY NAME
    List<EventActivity> findAllWithExecutionTypeSerialEntity(@Param("plannedDate") Date plannedDate, @Param("retryDate") Date retryDate, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query(
            "SELECT P FROM EventActivity P " +
                    "WHERE P.status IN ( " +
                    "       org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.QUEUED " +
                    "  ) " +
                    "  AND NOT EXISTS(" +
                    " " +
                    "           SELECT H FROM EventHeartbeat H " +
                    "           WHERE H.executionNode = P.executionNode " +
                    ") " +
                    "ORDER BY P.priority, P.plannedDate, P.retryDate NULLS FIRST, P.id"
    )
        //SERIAL_ENTITY,        //SERIALIZE PA WITH THE SAME PROCESS ID, ENTITY ID AND ENTITY NAME
    List<EventActivity> findOrphanQueuedEntity(Pageable pageable);


    Page<EventActivity> findAllByProcessId(Long processId, Pageable pageRequest);

    @Query(
            "SELECT EA FROM EventActivity EA " +
                    "WHERE EA.processId = :processId " +
                    "  AND (:privacyId IS NULL OR EA.privacyId IS NULL OR EA.privacyId = :privacyId) " +
                    "  AND (EA.visibility IN (:eventVisibilitySet))" +
                    "ORDER BY COALESCE (EA.retryDate, EA.plannedDate)"
    )
    Page<EventActivity> findAllByProcessIdAndVisibleAndPrivacyId(
            @Param("processId") Long processId,
            @Param("eventVisibilitySet") Set<EventVisibility> eventVisibilitySet,
            @Param("privacyId") Long privacyId,
            Pageable pageable
    );

}
