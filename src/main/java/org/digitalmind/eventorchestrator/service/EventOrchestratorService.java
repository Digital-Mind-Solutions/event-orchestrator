package org.digitalmind.eventorchestrator.service;


import org.digitalmind.buildingblocks.core.beanutils.service.IService;
import org.digitalmind.buildingblocks.core.requestcontext.dto.RequestContext;
import org.digitalmind.eventorchestrator.entity.EventActivity;
import org.digitalmind.eventorchestrator.entity.EventMemo;
import org.digitalmind.eventorchestrator.enumeration.EventActivityExecutionMode;

import java.util.Map;

public interface EventOrchestratorService extends IService {

    EventMemo createEventMemo(RequestContext requestContext, EventMemo eventMemoRequest);

    EventActivity createEventActivity(RequestContext requestContext, EventActivity eventActivityRequest);

    void triggerEventActivities(RequestContext requestContext, Long processId, String processName, Long parentMemoId, String code, String status, Object trigger);

    EventMemo executeEventActivity(RequestContext requestContext, EventActivity eventActivity, EventActivityExecutionMode executionMode);

    //void createMemoBasedActivities(RequestContext requestContext, ProcessMemo processMemo);

    public Map<String, Object> createParameters(Object... items);

    Object getEntity(String name, Object id);

    String getEntityAlias(String name);

}
