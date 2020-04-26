package org.digitalmind.eventorchestrator.sam;

import org.digitalmind.eventorchestrator.entity.EventActivity;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

public interface EventOrchestratorPoller {

    List<EventActivity> poll(Date plannedDate, Date retryDate, Pageable pageable);

    void requeue(EventActivity eventActivity);

    //void requeue(List<EventActivity> eventActivityList);


}
