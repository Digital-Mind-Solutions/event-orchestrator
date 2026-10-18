package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.EventActivity;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.hibernate.Timeouts;
import org.hibernate.jpa.SpecHints;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface EventActivityRepository extends JpaRepository<EventActivity, Long> {

    /**
     * Value for {@link SpecHints#HINT_SPEC_LOCK_TIMEOUT} ({@code jakarta.persistence.lock.timeout})
     * that asks Hibernate to emit {@code FOR UPDATE SKIP LOCKED} on MySQL.
     * <p>
     * Equals {@link Timeouts#SKIP_LOCKED_MILLI} ({@code -2}). Same semantics as {@link Timeouts#SKIP_LOCKED}.
     * Used only by orphan recirculation queries (not by poll / claim queries).
     */
    String LOCK_TIMEOUT_SKIP_LOCKED = "" + Timeouts.SKIP_LOCKED_MILLI;

    @Lock(LockModeType.PESSIMISTIC_FORCE_INCREMENT)
    @Query(
            "SELECT P FROM EventActivity P " +
                    "WHERE P.executionType = org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.PARALLEL " +
                    "  AND P.status IN ( " +
                    " org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING, " +
                    " org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.PENDING_RETRY " +
                    ") " +
                    "  AND P.plannedDate < :plannedDate " +
                    //"  AND (P.retryDate IS NULL OR P.retryDate < :retryDate) " +
                    "  AND P.retryDate < :retryDate " +
                    "ORDER BY P.priority, P.plannedDate, P.retryDate, P.id"
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
                    //"  AND (P.retryDate IS NULL OR P.retryDate < :retryDate) " +
                    "  AND P.retryDate < :retryDate " +
                    "  AND NOT EXISTS(" +
                    " " +
                    "           SELECT C FROM EventActivity C " +
                    "           WHERE C.executionType = org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.SERIAL_PROCESS " +
                    "             AND (C.plannedDate < P.plannedDate OR (C.plannedDate = P.plannedDate AND C.id < P.id))" +
                    "             AND C.processId = P.processId " +
                    ") " +
                    "ORDER BY P.priority, P.retryDate, P.id"
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
                    //"  AND (P.retryDate IS NULL OR P.retryDate < :retryDate) " +
                    "  AND P.retryDate < :retryDate " +
                    "  AND NOT EXISTS(" +
                    " " +
                    "           SELECT C FROM EventActivity C " +
                    "           WHERE C.executionType = org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionType.SERIAL_ENTITY " +
                    "             AND C.plannedDate < P.plannedDate " +
                    "             AND C.processId = P.processId " +
                    "             AND C.entityId = P.entityId " +
                    "             AND C.entityName = P.entityName " +
                    ") " +
                    "ORDER BY P.priority, P.retryDate, P.id"
    )
    //SERIAL_ENTITY,        //SERIALIZE PA WITH THE SAME PROCESS ID, ENTITY ID AND ENTITY NAME
    List<EventActivity> findAllWithExecutionTypeSerialEntity(@Param("plannedDate") Date plannedDate, @Param("retryDate") Date retryDate, Pageable pageable);

    /**
     * Orphan recirculation — QUEUED on a <strong>dead</strong> execution node (no row in {@code EventHeartbeat}).
     *
     * <h3>How this query is executed</h3>
     * <ol>
     *   <li>{@link LockModeType#PESSIMISTIC_WRITE} → Hibernate selects with a row lock
     *       ({@code SELECT … FOR UPDATE} on MySQL).</li>
     *   <li>{@link SpecHints#HINT_SPEC_LOCK_TIMEOUT} = {@link #LOCK_TIMEOUT_SKIP_LOCKED} ({@code -2})
     *       → MySQL dialect appends {@code SKIP LOCKED}.</li>
     *   <li>Resulting SQL shape: {@code SELECT … FROM event_activity … FOR UPDATE SKIP LOCKED}
     *       (plus {@code LIMIT} from {@link Pageable}).</li>
     *   <li>Rows already locked by another pod are <strong>skipped</strong> — this pod continues on the
     *       next unlocked candidates instead of waiting or deadlocking on the same top-N.</li>
     * </ol>
     *
     * <p>Poll / claim methods above keep {@link LockModeType#PESSIMISTIC_FORCE_INCREMENT} (version bump)
     * and do <strong>not</strong> use SKIP LOCKED — they intentionally wait for the lock.
     *
     * <p>Disjoint from {@link #findOrphanOldQueuedWithActiveNode}: here the node has <em>no</em> heartbeat.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = SpecHints.HINT_SPEC_LOCK_TIMEOUT, value = LOCK_TIMEOUT_SKIP_LOCKED))
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
                    "ORDER BY P.priority, P.plannedDate, P.retryDate, P.id"
    )
    List<EventActivity> findOrphanQueuedWithDeadNode(Pageable pageable);


    /**
     * Orphan recirculation — QUEUED that is <strong>stale</strong> ({@code updatedAt < updatedAtBefore})
     * while the execution node still has an active {@code EventHeartbeat}.
     *
     * <h3>How this query is executed</h3>
     * Same locking contract as {@link #findOrphanQueuedWithDeadNode}:
     * {@link LockModeType#PESSIMISTIC_WRITE} + {@link #LOCK_TIMEOUT_SKIP_LOCKED}
     * → MySQL {@code FOR UPDATE SKIP LOCKED}, so concurrent pods do not deadlock on the same ordered top-N.
     *
     * <p>Disjoint from {@link #findOrphanQueuedWithDeadNode}: here the node <em>still has</em> a heartbeat
     * (activity stuck in QUEUED longer than the configured old-queue threshold).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = SpecHints.HINT_SPEC_LOCK_TIMEOUT, value = LOCK_TIMEOUT_SKIP_LOCKED))
    @Query(
            "SELECT P FROM EventActivity P " +
                    "WHERE P.status IN ( " +
                    "       org.digitalmind.eventorchestrator.enumeration.EventActivityStatus.QUEUED " +
                    "  ) " +
                    "  AND P.updatedAt < :updatedAtBefore " +
                    "  AND EXISTS(" +
                    "           SELECT H FROM EventHeartbeat H " +
                    "           WHERE H.executionNode = P.executionNode " +
                    "  ) " +
                    "ORDER BY P.priority, P.plannedDate, P.retryDate, P.id"
    )
    List<EventActivity> findOrphanOldQueuedWithActiveNode(@Param("updatedAtBefore") Date updatedAtBefore, Pageable pageable);


    Page<EventActivity> findAllByProcessId(Long processId, Pageable pageRequest);

    @Query(
            "SELECT EA FROM EventActivity EA " +
                    "WHERE EA.processId = :processId " +
                    "  AND (:privacyId IS NULL OR EA.privacyId IS NULL OR EA.privacyId = :privacyId) " +
                    "  AND (EA.visibility IN (:eventVisibilitySet))" +
                    "ORDER BY EA.retryDate"
    )
    Page<EventActivity> findAllByProcessIdAndVisibleAndPrivacyId(
            @Param("processId") Long processId,
            @Param("eventVisibilitySet") Set<EventVisibility> eventVisibilitySet,
            @Param("privacyId") Long privacyId,
            Pageable pageable
    );

}
