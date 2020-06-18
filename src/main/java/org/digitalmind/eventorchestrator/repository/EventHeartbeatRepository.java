package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.EventHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface EventHeartbeatRepository extends JpaRepository<EventHeartbeat, Long> {

    void deleteByUpdatedAtBefore(Date updatedAt);

    EventHeartbeat getByExecutionNode(String executionNode);

    @Modifying
    @Query(
            "UPDATE EventHeartbeat H " +
                    "SET H.updatedAt = :updatedAt " +
                    "WHERE H.executionNode = :executionNode")
    int updateBeatDateByExecutionNode(@Param("executionNode") String executionNode, Date updatedAt);

}
