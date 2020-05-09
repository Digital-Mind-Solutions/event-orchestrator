package org.digitalmind.eventorchestrator.service.entity;

import lombok.extern.slf4j.Slf4j;
import org.digitalmind.buildingblocks.core.beanutils.service.IService;
import org.digitalmind.buildingblocks.core.networkutils.service.HostUtilService;
import org.digitalmind.buildingblocks.core.spel.service.impl.SpelServiceImpl;
import org.digitalmind.eventorchestrator.entity.EventActivity;
import org.digitalmind.eventorchestrator.enumeration.EventActivityStatus;
import org.digitalmind.eventorchestrator.enumeration.EventVisibility;
import org.digitalmind.eventorchestrator.repository.EventActivityRepository;
import org.digitalmind.eventorchestrator.repository.TemplateActivityActivatorRepository;
import org.digitalmind.eventorchestrator.repository.TemplateActivityRepository;
import org.digitalmind.eventorchestrator.repository.TemplateFlowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class EventActivityService implements IService {

    private final TemplateActivityRepository tar;
    private final TemplateActivityActivatorRepository taar;
    private final TemplateFlowRepository tfr;
    private final SpelServiceImpl spelService;
    private final EventActivityRepository eventActivityRepository;
    private final HostUtilService hostUtilService;

    @Autowired
    public EventActivityService(TemplateActivityRepository tar,
                                TemplateActivityActivatorRepository taar,
                                TemplateFlowRepository tfr,
                                SpelServiceImpl spelService,
                                EventActivityRepository eventActivityRepository,
                                HostUtilService hostUtilService) {
        this.tar = tar;
        this.taar = taar;
        this.tfr = tfr;
        this.spelService = spelService;
        this.eventActivityRepository = eventActivityRepository;
        this.hostUtilService = hostUtilService;
    }

//    public List<EventActivity> getProcessActivitySubList(EventActivity eventActivity) throws ExecutionException {
//        List<EventActivity> childEventActivityList = new ArrayList<EventActivity>();
//
//        //Process process = processActivity.getProcess();
//        Process process = null;
//
//
//        //get template activator list that is related to the parent code and parent status
//        List<TemplateActivityActivator> templateActivityActivatorList = null; //taar.getBy
//        //processActivity.getCode()
//        //processActivity.getStatus()
//        //processActivity.getSubStatus()
//
//        if (!templateActivityActivatorList.isEmpty()) {
//            ConcurrentHashMap<String, Object> taaContextMap = new ConcurrentHashMap<>();
//            taaContextMap.put("process", process);
//            taaContextMap.put("eventActivity", eventActivity);
//            EvaluationContext taaContext = spelService.getContext(null, taaContextMap);
//
//            for (TemplateActivityActivator templateActivityActivator : templateActivityActivatorList) {
//                //test if the activator SPEL qualifies to activating the activity
//                Expression taaQualifier = spelService.getExpression(templateActivityActivator.getQualifierExpr());
//                Boolean taaQualified = (Boolean) spelService.getValue(taaQualifier, taaContext);
//                if (taaQualified) {
//                    TemplateActivity templateActivity = tar.getOne(templateActivityActivator.getTemplateId());
//                    ConcurrentHashMap<String, Object> taContextMap = new ConcurrentHashMap<>();
//                    taContextMap.put("process", process);
//                    taContextMap.put("eventActivity", eventActivity);
//                    taContextMap.put("templateActivityActivator", templateActivityActivator);
//                    taContextMap.put("templateActivity", templateActivity);
//                    EvaluationContext taContext = spelService.getContext(null, taContextMap);
//
//                    Expression taQualifier = spelService.getExpression(templateActivity.getQualifierExpr2());
//                    Boolean taQualified = (Boolean) spelService.getValue(taQualifier, taContext);
//
//                    if (taQualified) {
//                        //template qualified and must applied
//                        EventActivity childEventActivity = processActivityDispatcher.createFromTemplate(
//                                process, eventActivity,
//                                templateActivity, taContext,
//                                templateActivityActivator, taaContext);
//                        childEventActivityList.add(childEventActivity);
//                    }
//                }
//            }
//        }
//        return childEventActivityList;
//    }

//    public void activateProcessActivitySubList(EventActivity eventActivity) throws ExecutionException {
//        List<EventActivity> eventActivitySubList = getProcessActivitySubList(eventActivity);
//        eventActivityRepository.saveAll(eventActivitySubList);
//    }

    public void deleteById(long id) {
        eventActivityRepository.deleteById(id);
    }

    public EventActivity save(EventActivity eventActivity) {
        return eventActivityRepository.save(eventActivity);
    }

    public List<EventActivity> saveAll(Iterable<EventActivity> eventActivities) {
        return eventActivityRepository.saveAll(eventActivities);
    }

    @Transactional
    public List<EventActivity> findAllWithExecutionTypeParallel(Date plannedDate, Date retryDate, Pageable pageable) {
        List<EventActivity> eventActivityList = new ArrayList<>();
        try {
            eventActivityList = eventActivityRepository.findAllWithExecutionTypeParallel(plannedDate, retryDate, pageable);
            eventActivityList.forEach(eventActivity -> {
                eventActivity.setStatus(EventActivityStatus.QUEUED);
                eventActivity.setExecutionNode(hostUtilService.getHostname());
            });
        } catch (PessimisticLockException e) {
            //ignore PessimisticLockException
            log.info("findAllWithExecutionTypeParallel exception={}, exceptionMessage={}", e.getClass().getSimpleName(), e.getMessage());
        } catch (LockTimeoutException e) {
            //ignore LockTimeoutException
            log.info("findAllWithExecutionTypeParallel exception={}, exceptionMessage={}", e.getClass().getSimpleName(), e.getMessage());
        }
        return eventActivityList;
    }

    @Transactional
    public List<EventActivity> findAllWithExecutionTypeSerialProcess(Date plannedDate, Date retryDate, Pageable pageable) {
        List<EventActivity> eventActivityList = new ArrayList<>();
        try {
            eventActivityList = eventActivityRepository.findAllWithExecutionTypeSerialProcess(plannedDate, retryDate, pageable);
            eventActivityList.forEach(eventActivity -> {
                eventActivity.setStatus(EventActivityStatus.QUEUED);
                eventActivity.setExecutionNode(hostUtilService.getHostname());
            });
        } catch (PessimisticLockException e) {
            //ignore PessimisticLockException
            log.info("findAllWithExecutionTypeSerialProcess exception={}, exceptionMessage={}", e.getClass().getSimpleName(), e.getMessage());
        } catch (LockTimeoutException e) {
            //ignore LockTimeoutException
            log.info("findAllWithExecutionTypeSerialProcess exception={}, exceptionMessage={}", e.getClass().getSimpleName(), e.getMessage());
        }
        return eventActivityList;
    }

    @Transactional
    public List<EventActivity> findAllWithExecutionTypeSerialEntity(Date plannedDate, Date retryDate, Pageable pageable) {
        List<EventActivity> eventActivityList = new ArrayList<>();
        try {
            eventActivityList = eventActivityRepository.findAllWithExecutionTypeSerialEntity(plannedDate, retryDate, pageable);
            eventActivityList.forEach(eventActivity -> {
                eventActivity.setStatus(EventActivityStatus.QUEUED);
                eventActivity.setExecutionNode(hostUtilService.getHostname());
            });
        } catch (PessimisticLockException e) {
            //ignore PessimisticLockException
            log.info("findAllWithExecutionTypeSerialEntity exception={}, exceptionMessage={}", e.getClass().getSimpleName(), e.getMessage());
        } catch (LockTimeoutException e) {
            //ignore LockTimeoutException
            log.info("findAllWithExecutionTypeSerialEntity exception={}, exceptionMessage={}", e.getClass().getSimpleName(), e.getMessage());
        }
        return eventActivityList;
    }

    public Page<EventActivity> findAllByProcessId(Long processId, Pageable pageRequest) {
        return eventActivityRepository.findAllByProcessId(processId, pageRequest);
    }

    public Page<EventActivity> findAllByProcessIdAndVisibleAndPrivacyId(
            Long processId, Set<EventVisibility> eventVisibilitySet, Long privacyId, Pageable pageRequest
    ) {
        return eventActivityRepository.findAllByProcessIdAndVisibleAndPrivacyId(processId, eventVisibilitySet, privacyId, pageRequest);
    }

    @Transactional
    public void requeue(List<EventActivity> eventActivityList, Date date) {
        eventActivityList.forEach(eventActivity -> {
            eventActivity.setStatus(EventActivityStatus.PENDING_RETRY);
            eventActivity.setRetryDate(date);
            eventActivity.setExecutionNode(hostUtilService.getHostname());
        });
        eventActivityRepository.saveAll(eventActivityList);
    }

    @Transactional
    public void requeue(List<EventActivity> eventActivityList) {
        requeue(eventActivityList, new Date());
    }

    @Transactional
    public void requeue(EventActivity eventActivity, Date date) {
        eventActivity.setStatus(EventActivityStatus.PENDING_RETRY);
        eventActivity.setRetryDate(date);
        eventActivity.setExecutionNode(hostUtilService.getHostname());
        eventActivityRepository.save(eventActivity);
    }

    @Transactional
    public void requeue(EventActivity eventActivity) {
        requeue(eventActivity, org.apache.commons.lang3.time.DateUtils.addSeconds(new Date(), 60));
    }

    @Transactional
    public void requeueOrphans(int size) {
        Pageable pageable = PageRequest.of(0, size);
        List<EventActivity> eventActivityList = eventActivityRepository.findOrphanQueuedEntity(pageable);
        eventActivityList.forEach(eventActivity -> {
            eventActivity.setStatus(EventActivityStatus.PENDING_RETRY);
            eventActivity.setRetryDate(new Date());
            eventActivity.setExecutionNode(null);
        });
        eventActivityRepository.saveAll(eventActivityList);
    }

}
