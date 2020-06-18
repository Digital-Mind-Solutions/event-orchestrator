package org.digitalmind.eventorchestrator.service.entity;

import groovy.util.logging.Slf4j;
import org.digitalmind.buildingblocks.core.networkutils.service.HostUtilService;
import org.digitalmind.buildingblocks.core.requestcontext.service.RequestContextService;
import org.digitalmind.eventorchestrator.entity.EventHeartbeat;
import org.digitalmind.eventorchestrator.repository.EventHeartbeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class EventHeartbeatService {
    private final EventHeartbeatRepository eventHeartbeatRepository;
    private final HostUtilService hostUtilService;
    private final RequestContextService requestContextService;
    private int index;
    private int count;

    public EventHeartbeatService(
            EventHeartbeatRepository eventHeartbeatRepository,
            HostUtilService hostUtilService,
            RequestContextService requestContextService) {
        this.eventHeartbeatRepository = eventHeartbeatRepository;
        this.hostUtilService = hostUtilService;
        this.requestContextService = requestContextService;
        this.index = 0;
        this.count = 100;
    }

    public void deleteByUpdatedAtBefore(Date updatedAt) {
        this.eventHeartbeatRepository.deleteByUpdatedAtBefore(updatedAt);
    }

    public EventHeartbeat getByExecutionNode(String executionNode) {
        return this.eventHeartbeatRepository.getByExecutionNode(executionNode);
    }

    @Transactional
    public void beatNode(Date updatedAt) {
        String hostName = hostUtilService.getHostname();
        int updateCount = this.eventHeartbeatRepository.updateBeatDateByExecutionNode(hostName, updatedAt);
        if (updateCount == 0) {
            EventHeartbeat eventHeartbeat = EventHeartbeat.builder().executionNode(hostName).updatedAt(updatedAt).build();
            eventHeartbeatRepository.save(eventHeartbeat);
        }
    }

}
