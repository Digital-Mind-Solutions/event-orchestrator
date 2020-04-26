package org.digitalmind.eventorchestrator.repository;

import org.digitalmind.eventorchestrator.entity.EventHeartbeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface EventHeartbeatRepository extends JpaRepository<EventHeartbeat, Long> {

    void deleteByUpdatedAtBefore(Date updatedAt);

    EventHeartbeat getByExecutionNode(String executionNode);

}
