package org.digitalmind.eventorchestrator.service.impl;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.time.DateUtils;
import org.digitalmind.buildingblocks.core.beanutils.service.SpringBeanUtil;
import org.digitalmind.buildingblocks.core.requestcontext.dto.RequestContext;
import org.digitalmind.buildingblocks.core.requestcontext.dto.impl.RequestContextBasic;
import org.digitalmind.buildingblocks.core.requestcontext.service.RequestContextService;
import org.digitalmind.buildingblocks.core.spel.service.SpelService;
import org.digitalmind.eventorchestrator.config.EventOrchestratorConfig;
import org.digitalmind.eventorchestrator.entity.*;
import org.digitalmind.eventorchestrator.enumeration.*;
import org.digitalmind.eventorchestrator.exception.EventOrchestratorException;
import org.digitalmind.eventorchestrator.exception.EventOrchestratorFatalException;
import org.digitalmind.eventorchestrator.plugin.EventOrchestratorEntityPlugin;
import org.digitalmind.eventorchestrator.repository.EventActivityRepository;
import org.digitalmind.eventorchestrator.repository.EventMemoRepository;
import org.digitalmind.eventorchestrator.sam.EventOrchestratorPoller;
import org.digitalmind.eventorchestrator.service.EventOrchestratorService;
import org.digitalmind.eventorchestrator.service.EventOrchestratorServiceDependsOn;
import org.digitalmind.eventorchestrator.service.entity.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.utils.EventOrchestratorExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.expression.EvaluationContext;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.beans.Introspector;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.digitalmind.eventorchestrator.config.EventOrchestratorModuleConfig.ENABLED;
import static org.digitalmind.eventorchestrator.config.EventOrchestratorModuleConfig.EVENT_ORCHESTRATOR_PLUGIN_REGISTRY;

@Service("eventOrchestratorService")
@DependsOn({
        EventOrchestratorServiceDependsOn.NAME
})
@ConditionalOnProperty(name = ENABLED, havingValue = "true")
@Slf4j
public class EventOrchestratorServiceImpl implements EventOrchestratorService {

    private EventOrchestratorService self;

    private final EventOrchestratorConfig config;
    private final PluginRegistry<EventOrchestratorEntityPlugin, String> eventOrchestratorPluginRegistry;

    private final SpringBeanUtil springBeanUtil;
    //private final EventMemoRepository eventMemoRepository;
    private final EventActivityService eventActivityService;
    private final EventMemoService eventMemoService;
    private final EventHeartbeatService eventHeartbeatService;

    private final RequestContextService requestContextService;
    private final TemplateActivityService tas;
    private final TemplateActivityActivatorService taas;
    private final TemplateFlowService tfs;


    private final SpelService spelService;

    private final CacheLoader<TaaKey, List<TemplateActivityActivator>> taaCacheLoader;
    private final LoadingCache<TaaKey, List<TemplateActivityActivator>> taaCache;

    private final CacheLoader<String, List<String>> tfCacheLoader;
    private final LoadingCache<String, List<String>> tfCache;

    private final CacheLoader<Long, TemplateActivity> taCacheLoader;
    private final LoadingCache<Long, TemplateActivity> taCache;

    private final EventOrchestratorPoller[] eventOrchestratorPoller;
    private final AsyncTaskExecutor[] eventOrchestratorTaskExecutor;
    private final EventOrchestratorConfig.EventOrchestratorPollConfiguration[] eventOrchestratorTaskExecutorConfiguration;
    private final ScheduledExecutorService[] scheduler;

    private final ConcurrentLinkedQueue<EventActivity> errorEventActivityQueue;


    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    public static class TaaKey {
        private String code;
        private String status;
    }

    @Autowired
    public EventOrchestratorServiceImpl(
            EventOrchestratorConfig config,
            @Qualifier(EVENT_ORCHESTRATOR_PLUGIN_REGISTRY) PluginRegistry<EventOrchestratorEntityPlugin, String>
                    eventOrchestratorPluginRegistry,
            PluginRegistry<EventOrchestratorEntityPlugin, String> eventOrchestratorPluginRegistry1, SpringBeanUtil springBeanUtil,
            EventMemoRepository eventMemoRepository,
            EventActivityRepository eventActivityRepository,
            EventHeartbeatService eventHeartbeatService,
            RequestContextService requestContextService,
            TemplateActivityService tas,
            TemplateActivityActivatorService taas,
            TemplateFlowService tfs,
            SpelService spelService,
            EventActivityService eventActivityService,
            EventMemoService eventMemoService
    ) {
        this.config = config;

        this.eventOrchestratorPluginRegistry = eventOrchestratorPluginRegistry;
        this.springBeanUtil = springBeanUtil;
        this.eventHeartbeatService = eventHeartbeatService;
        this.eventMemoService = eventMemoService;
        //this.eventMemoRepository = eventMemoRepository;
        this.eventActivityService = eventActivityService;
        //this.eventActivityRepository = eventActivityRepository;
        this.requestContextService = requestContextService;
        this.tas = tas;
        this.taas = taas;
        this.tfs = tfs;
        this.spelService = spelService;

        this.taaCacheLoader = new CacheLoader<TaaKey, List<TemplateActivityActivator>>() {
            @Override
            public List<TemplateActivityActivator> load(TaaKey key) {
                if (key == null) return null;
                List<TemplateActivityActivator> templateActivityActivatorList = taas.findByParentCodeAndParentStatusOrderByPriorityAscIdAsc(
                        key.getCode(),
                        key.getStatus()
                );
                return templateActivityActivatorList;
            }
        };

        this.taaCache = CacheBuilder
                .from(CacheBuilderSpec.parse(config.getCache().getTemplateActivityActivator()))
                .build(taaCacheLoader);

        tfCacheLoader = new CacheLoader<String, List<String>>() {
            @Override
            public List<String> load(String key) {
                if (key == null) return null;
                List<TemplateFlow> templateFlowList = tfs.findByFlowTemplateOrderById(key);
                List<String> usecases = templateFlowList.stream().map(templateFlow -> templateFlow.getUsecase()).collect(Collectors.toList());
                usecases.add("*");
                return usecases;
            }
        };

        this.tfCache = CacheBuilder
                .from(CacheBuilderSpec.parse(config.getCache().getTemplateFlow()))
                .build(tfCacheLoader);

        this.taCacheLoader = new CacheLoader<Long, TemplateActivity>() {
            @Override
            public TemplateActivity load(Long id) {
                if (id == null) return null;
                TemplateActivity templateActivity = tas.getById(id);
                return templateActivity;
            }
        };

        this.taCache = CacheBuilder
                .from(CacheBuilderSpec.parse(config.getCache().getTemplateActivity()))
                .build(taCacheLoader);


        this.eventOrchestratorPoller = new EventOrchestratorPoller[3];
        this.eventOrchestratorTaskExecutor = new AsyncTaskExecutor[3];
        this.eventOrchestratorTaskExecutorConfiguration = new EventOrchestratorConfig.EventOrchestratorPollConfiguration[3];
        this.scheduler = new ScheduledExecutorService[5];
        this.errorEventActivityQueue = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < 3; i++) {
            switch (i) {
                case 0:
                    //PARALLEL
                    this.eventOrchestratorPoller[i] = new EventOrchestratorPoller() {
                        @Override
                        public List<EventActivity> poll(Date plannedDate, Date retryDate, Pageable pageable) {
                            return eventActivityService.findAllWithExecutionTypeParallel(plannedDate, retryDate, pageable);
                        }

                        @Override
                        public void requeue(EventActivity eventActivity) {
                            eventActivityService.requeue(eventActivity);
                        }
                    };
                    this.eventOrchestratorTaskExecutor[i] = (AsyncTaskExecutor) springBeanUtil.getBean(config.getThreadPoolExecutor().getParallel().getThreadPoolName());
                    this.eventOrchestratorTaskExecutorConfiguration[i] = config.getThreadPoolExecutor().getParallel();
                    break;
                case 1:
                    //SERIAL PROCESS
                    this.eventOrchestratorPoller[i] = new EventOrchestratorPoller() {
                        @Override
                        public List<EventActivity> poll(Date plannedDate, Date retryDate, Pageable pageable) {
                            return eventActivityService.findAllWithExecutionTypeSerialProcess(plannedDate, retryDate, pageable);
                        }

                        @Override
                        public void requeue(EventActivity eventActivity) {
                            eventActivityService.requeue(eventActivity);
                        }
                    };
                    this.eventOrchestratorTaskExecutor[i] = (AsyncTaskExecutor) springBeanUtil.getBean(config.getThreadPoolExecutor().getSerialProcess().getThreadPoolName());
                    this.eventOrchestratorTaskExecutorConfiguration[i] = config.getThreadPoolExecutor().getSerialProcess();
                    break;
                case 2:
                    //SERIAL ENTITY
                    this.eventOrchestratorPoller[i] = new EventOrchestratorPoller() {
                        @Override
                        public List<EventActivity> poll(Date plannedDate, Date retryDate, Pageable pageable) {
                            return eventActivityService.findAllWithExecutionTypeSerialEntity(plannedDate, retryDate, pageable);
                        }

                        @Override
                        public void requeue(EventActivity eventActivity) {
                            eventActivityService.requeue(eventActivity);
                        }
                    };
                    this.eventOrchestratorTaskExecutor[i] = (AsyncTaskExecutor) springBeanUtil.getBean(config.getThreadPoolExecutor().getSerialEntity().getThreadPoolName());
                    this.eventOrchestratorTaskExecutorConfiguration[i] = config.getThreadPoolExecutor().getSerialEntity();
                    break;
            }
            this.scheduler[i] = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, this.getClass().getName() + "-Monitor");
                t.setDaemon(true);
                return t;
            });
            final int index = i;
            this.scheduler[i].scheduleAtFixedRate(() -> {
                        log.trace("Start scheduler [{}] db monitor", index);
                        try {
                            int remainingCapacity = this.getRemainingQueueCapacity(this.eventOrchestratorTaskExecutor[index]);
                            if (remainingCapacity == 0) {
                                return;
                            }
                            Pageable pageable = PageRequest.of(0, Math.min(remainingCapacity, this.eventOrchestratorTaskExecutorConfiguration[index].getSchedulerCapacityThreshold()));
                            Date plannedDate = new Date();
                            Date retryDate = plannedDate;
                            List<EventActivity> eventActivityList = this.eventOrchestratorPoller[index].poll(plannedDate, retryDate, pageable);
                            for (EventActivity eventActivity : eventActivityList) {
                                RequestContext requestContext = requestContextService.create();
                                try {
                                    log.info("adding eventActivity id={}, code={}, active core={}, remaining capacity={}", eventActivity.getId(), eventActivity.getCode(), getActiveCount(this.eventOrchestratorTaskExecutor[index]), this.getRemainingQueueCapacity(this.eventOrchestratorTaskExecutor[index]));
                                    CompletableFuture<EventMemo> eventMemoCompletableFuture = CompletableFuture
                                            .supplyAsync(() -> self.executeEventActivity(requestContext, eventActivity, EventActivityExecutionMode.ASYNC), this.eventOrchestratorTaskExecutor[index])
                                            .exceptionally(throwable -> {
                                                eventActivity.setStatusDescription(throwable.getMessage());
                                                errorEventActivityQueue.add(eventActivity);
                                                return null;
                                            });
                                } catch (Exception e) {
                                    eventActivity.setStatusDescription(e.getMessage());
                                    errorEventActivityQueue.add(eventActivity);
                                }
                            }
                            if (errorEventActivityQueue.size() > 0) {
                                EventActivity errorEventActivity = errorEventActivityQueue.peek();
                                while (errorEventActivity != null) {
                                    log.info("requeue eventActivity id={}, code={}, error={}", errorEventActivity.getId(), errorEventActivity.getCode(), errorEventActivity.getStatusDescription());
                                    this.eventOrchestratorPoller[index].requeue(errorEventActivity);
                                    boolean removed = errorEventActivityQueue.remove(errorEventActivity);
                                    errorEventActivity = errorEventActivityQueue.peek();
                                }
                            }
                            log.trace("End scheduler [{}] db monitor", index);
                        } catch (Exception e) {
                            log.error("Error scheduler [{}] db monitor. {}", index, Throwables.getStackTraceAsString(e), e);
                        }

                    },
                    this.eventOrchestratorTaskExecutorConfiguration[i].getSchedulerInitDelay(),
                    this.eventOrchestratorTaskExecutorConfiguration[i].getSchedulerPeriod(),
                    this.eventOrchestratorTaskExecutorConfiguration[i].getSchedulerUnit()
            );
        }

        final int heartbeatIndex = 3;
        this.scheduler[heartbeatIndex] = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, this.getClass().getName() + "-Heartbeat");
            t.setDaemon(true);
            return t;
        });
        int frequencyFactor = this.config.getThreadPoolExecutor().getHeartbeat().getSchedulerCapacityThreshold();
        int frequencySeconds = (int) TimeUnit.SECONDS.convert(this.config.getThreadPoolExecutor().getHeartbeat().getSchedulerPeriod(), this.config.getThreadPoolExecutor().getHeartbeat().getSchedulerUnit());
        final int clearOfferSeconds = frequencyFactor * frequencySeconds;
        this.scheduler[heartbeatIndex].scheduleAtFixedRate(() -> {
                    log.trace("Start scheduler [{}] heartbeat monitor", heartbeatIndex);
                    try {
                        Date clearOlderThanDate = DateUtils.addSeconds(new Date(), -clearOfferSeconds);
                        eventHeartbeatService.beat(clearOlderThanDate);
                        log.trace("End scheduler [{}] heartbeat monitor", heartbeatIndex);
                    } catch (Exception e) {
                        log.error("Error scheduler [{}] heartbeat monitor. {}", heartbeatIndex, Throwables.getStackTraceAsString(e), e);
                    }

                },
                this.config.getThreadPoolExecutor().getHeartbeat().getSchedulerInitDelay(),
                this.config.getThreadPoolExecutor().getHeartbeat().getSchedulerPeriod(),
                this.config.getThreadPoolExecutor().getHeartbeat().getSchedulerUnit()
        );

        final int orphanIndex = 4;
        this.scheduler[orphanIndex] = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, this.getClass().getName() + "-Orphan");
            t.setDaemon(true);
            return t;
        });
        int size = this.config.getThreadPoolExecutor().getOrphan().getSchedulerCapacityThreshold();
        this.scheduler[heartbeatIndex].scheduleAtFixedRate(() -> {
                    log.trace("Start scheduler [{}] orphan monitor", heartbeatIndex);
                    try {
                        eventActivityService.requeueOrphans(size);
                        log.trace("End scheduler [{}] orphan monitor", heartbeatIndex);
                    } catch (Exception e) {
                        log.error("Error scheduler [{}] orphan monitor. {}", heartbeatIndex, Throwables.getStackTraceAsString(e), e);
                    }

                },
                this.config.getThreadPoolExecutor().getOrphan().getSchedulerInitDelay(),
                this.config.getThreadPoolExecutor().getOrphan().getSchedulerPeriod(),
                this.config.getThreadPoolExecutor().getOrphan().getSchedulerUnit()
        );
    }

    @PostConstruct
    private void init() {
        this.self = (EventOrchestratorService) springBeanUtil.getBean(EventOrchestratorService.class);
    }

    public int getActiveCount(AsyncTaskExecutor asyncTaskExecutor) {
        if (asyncTaskExecutor == null) {
            return 0;
        }
        AsyncTaskExecutor executor = asyncTaskExecutor;
        if (executor instanceof DelegatingSecurityContextAsyncTaskExecutorWrapper) {
            executor = ((DelegatingSecurityContextAsyncTaskExecutorWrapper) asyncTaskExecutor).getDelegate();
        }
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getActiveCount();
        }
        if (executor instanceof ThreadPoolTaskExecutor) {
            return ((ThreadPoolTaskExecutor) executor).getActiveCount();
        }
        return 0;
    }

    public BlockingQueue getBlockingQueue(AsyncTaskExecutor asyncTaskExecutor) {
        if (asyncTaskExecutor == null) {
            return null;
        }
        AsyncTaskExecutor executor = asyncTaskExecutor;

        if (executor instanceof DelegatingSecurityContextAsyncTaskExecutorWrapper) {
            executor = ((DelegatingSecurityContextAsyncTaskExecutorWrapper) asyncTaskExecutor).getDelegate();
        }
        if (executor instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) executor).getQueue();
        }
        if (executor instanceof ThreadPoolTaskExecutor) {
            return ((ThreadPoolTaskExecutor) executor).getThreadPoolExecutor().getQueue();
        }
        return null;
    }

    public int getRemainingQueueCapacity(AsyncTaskExecutor asyncTaskExecutor) {
        BlockingQueue blockingQueue = getBlockingQueue(asyncTaskExecutor);
        if (blockingQueue != null) {
            return blockingQueue.remainingCapacity();
        }
        return 0;
    }

    public RequestContext getOrDefault(RequestContext requestContext) {
        if (requestContext == null) {
            return RequestContextBasic.builder().build();
        }
        return requestContext;
    }

    @Override
    public EventMemo createEventMemo(RequestContext requestContext, EventMemo eventMemoRequest) {
        requestContext = getOrDefault(requestContext);
        eventMemoRequest.setContextId(requestContext.getId());
        String entityAlias = getEntityAlias(eventMemoRequest.getEntityName());
        eventMemoRequest.setEntityName(entityAlias);
        String processAlias = getEntityAlias(eventMemoRequest.getProcessName());
        eventMemoRequest.setProcessName(processAlias);
        Map<String, Object> context = eventMemoRequest.getContext();
        context.putAll(requestContext.getDetails());
        eventMemoRequest.setContext(context);
        eventMemoRequest.setVisibility(eventMemoRequest.getVisibility() != null ? eventMemoRequest.getVisibility() : EventVisibility.ADMIN);
        return eventMemoService.save(eventMemoRequest);
    }

    @Override
    public EventActivity createEventActivity(RequestContext requestContext, EventActivity eventActivityRequest) {
        requestContext = getOrDefault(requestContext);
        eventActivityRequest.setContextId(requestContext.getId());
        Map<String, Object> context = eventActivityRequest.getContext();
        String entityAlias = getEntityAlias(eventActivityRequest.getEntityName());
        eventActivityRequest.setEntityName(entityAlias);
        String processAlias = getEntityAlias(eventActivityRequest.getProcessName());
        eventActivityRequest.setProcessName(processAlias);
        context.putAll(requestContext.getDetails());
        eventActivityRequest.setContext(context);
        eventActivityRequest.setVisibility(eventActivityRequest.getVisibility() != null ? eventActivityRequest.getVisibility() : EventVisibility.ADMIN);
        return eventActivityService.save(eventActivityRequest);
    }

    public void triggerEventActivities(RequestContext requestContext, Long processId, String processName, Long parentMemoId, String code, String status, Object trigger) {
        requestContext = getOrDefault(requestContext);
        if (trigger != null && trigger instanceof Collection) {
            RequestContext finalRequestContext = requestContext;
            String finalCode = code;
            ((Collection) trigger).forEach(triggerItem -> triggerEventActivities(finalRequestContext, processId, processName, parentMemoId, finalCode, status, triggerItem));
            return;
        }
        log.info("triggerEventActivities requestContext={}, processId={}, processName={}, parentMemoId={}, code={}, status={}, trigger={}", requestContext, processId, processName, parentMemoId, code, status, trigger.getClass().getSimpleName());
        List<EventActivity> asyncEventActivityList = new ArrayList<EventActivity>();
        List<EventActivity> syncEventActivityList = new ArrayList<EventActivity>();
        ConcurrentHashMap<String, Object> taaContextMap = new ConcurrentHashMap<>();
        EvaluationContext taaContext = null;
        if (code.startsWith("$")) {
            //we have a dynamic calculated code, need a spel to calculate it
            if (trigger != null) {
                String triggerBeanName = Introspector.decapitalize(trigger.getClass().getSimpleName());
                taaContextMap.put(triggerBeanName, trigger);
                taaContextMap.put("trigger", trigger);
            }
            taaContext = spelService.getContext(null, taaContextMap);
            try {
                String codeInstance = (String) spelService.getValue(code.substring(1), taaContext);
                code = codeInstance;
            } catch (ExecutionException | RuntimeException e) {
                throw new EventOrchestratorFatalException("Unable to get code instance from template <" + code + ">", e);
            }
        }

        //need a fast mechanism to identify if there are required process activities
        List<TemplateActivityActivator> templateActivityActivatorList = null;
        try {
            templateActivityActivatorList = taaCache.get(TaaKey.builder().code(code).status(status).build());
            if (templateActivityActivatorList == null || templateActivityActivatorList.size() == 0) {
                return;
            }
        } catch (ExecutionException | RuntimeException e) {
            throw new EventOrchestratorFatalException("Unable to retrieve template activity activator list", e);
        }

        //get into the slow code
        EventOrchestratorProcess process = null;
        if (trigger instanceof EventOrchestratorProcess && processId == ((EventOrchestratorProcess) trigger).getId()) {
            process = (EventOrchestratorProcess) trigger;
        } else {
            process = (EventOrchestratorProcess) getEntity(processName, processId);
        }
        String flowTemplate = (process != null) ? process.getFlowTemplate() : "N/A";

        if (flowTemplate == null) {
            return;
        }

        List<String> useCaseList = null;
        try {
            useCaseList = tfCache.get(flowTemplate);
            if (useCaseList == null || useCaseList.size() == 0) {
                return;
            }
        } catch (ExecutionException | RuntimeException e) {
            throw new EventOrchestratorFatalException("Unable to retrieve usecase list", e);
        }

        List<String> finalUseCaseList = useCaseList;
        templateActivityActivatorList = templateActivityActivatorList.stream().filter(templateActivityActivator -> finalUseCaseList.contains(templateActivityActivator.getUsecase())).collect(Collectors.toList());
        if (templateActivityActivatorList == null || templateActivityActivatorList.size() == 0) {
            return;
        }

        //create process activity list
        //Object entity = getEntity(processMemo.getEntityName(), processMemo.getEntityId());
        if (requestContext != null) {
            taaContextMap.put("requestContext", requestContext);
        }
        if (process != null) {
            taaContextMap.put("process", process);
        }

        if (trigger != null) {
            String triggerBeanName = Introspector.decapitalize(trigger.getClass().getSimpleName());
            taaContextMap.put(triggerBeanName, trigger);
            taaContextMap.put("trigger", trigger);
        }
        if (processId != null) {
            taaContextMap.put("processId", processId);
        }
        if (processName != null) {
            taaContextMap.put("processName", processName);
        }

        if (parentMemoId != null) {
            taaContextMap.put("parentMemoId", parentMemoId);
        }
        if (code != null) {
            taaContextMap.put("code", code);
        }
        if (status != null) {
            taaContextMap.put("status", status);
        }

        taaContext = spelService.getContext(null, taaContextMap);

        for (TemplateActivityActivator templateActivityActivator : templateActivityActivatorList) {
            //test if the activator SPEL qualifies to activating the activity
            Boolean taaQualified = null;
            try {
                taaQualified = (Boolean) spelService.getValue(templateActivityActivator.getQualifierExpr(), taaContext);
            } catch (ExecutionException | RuntimeException e) {
                throw new EventOrchestratorFatalException("Unable to get qualifier expression for TemplateActivityActivator with id " + templateActivityActivator.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
            }

            if (taaQualified) {
                Date taaPlannedDate = null;
                try {
                    taaPlannedDate = (Date) spelService.getValue(templateActivityActivator.getPlannedDateExpr(), taaContext);
                } catch (ExecutionException | RuntimeException e) {
                    throw new EventOrchestratorFatalException("Unable to get planned date expression for TemplateActivityActivator with id " + templateActivityActivator.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
                }

                TemplateActivity templateActivity = null;
                try {
                    templateActivity = taCache.get(templateActivityActivator.getTemplateId());
                } catch (ExecutionException | RuntimeException e) {
                    throw new EventOrchestratorFatalException("Unable to retrieve TemplateActivity for id = " + templateActivityActivator.getTemplateId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
                }

                ConcurrentHashMap<String, Object> taContextMap = new ConcurrentHashMap<>();
                taContextMap.putAll(taaContextMap);
                taContextMap.put("templateActivityActivator", templateActivityActivator);
                taContextMap.put("templateActivity", templateActivity);

                EvaluationContext taContext = spelService.getContext(null, taContextMap);

                String entityName = null;
                try {
                    entityName = String.valueOf(spelService.getValue(templateActivity.getEntityNameExpr(), taContext));
                } catch (ExecutionException | RuntimeException e) {
                    throw new EventOrchestratorFatalException("Unable to get entity name expr for TemplateActivity with id " + templateActivity.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
                }

                List<String> entityIds = null;
                try {
                    Object entityIdValue = spelService.getValue(templateActivity.getEntityIdExpr(), taContext);
                    if (entityIdValue != null && entityIdValue.getClass().isArray()) {
                        entityIds = Arrays.asList((Object[]) entityIdValue).stream().map(entityId -> String.valueOf(entityId)).collect(Collectors.toList());
                    } else if (entityIdValue != null && entityIdValue instanceof Collection) {
                        entityIds = ((List<Object>) entityIdValue).stream().map(entityId -> String.valueOf(entityId)).collect(Collectors.toList());
                    } else {
                        entityIds = Arrays.asList(String.valueOf(entityIdValue));
                    }
                } catch (ExecutionException | RuntimeException e) {
                    throw new EventOrchestratorFatalException("Unable to get entity id expr for TemplateActivity with id " + templateActivity.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
                }

                for (String entityId : entityIds) {
                    taContextMap.put("templateEntityId", entityId);
                    taContextMap.put("templateEntityName", entityName);
                    taContext = spelService.getContext(null, taContextMap);

                    Map<String, Object> parameters = new HashMap<>();
                    Long privacyId = null;
                    Integer priority = 5000;
                    try {
                        if (templateActivity.getParametersExpr() != null && templateActivity.getParametersExpr().trim().length() > 0) {
                            parameters = (Map<String, Object>) spelService.getValue(templateActivity.getParametersExpr(), taContext);
                        }
                    } catch (ExecutionException | RuntimeException e) {
                        log.error("unable to get parameters expr for TemplateActivity id={}, processId={}, message={}", templateActivity.getId(), ((process != null) ? String.valueOf(process.getId()) : "null"), e.getLocalizedMessage());
                        throw new EventOrchestratorFatalException("Unable to get parameters expr for TemplateActivity with id " + templateActivity.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null") + ", message: " + e.getLocalizedMessage(), e);
                    }
                    try {
                        if (templateActivity.getPrivacyIdExpr() != null && templateActivity.getPrivacyIdExpr().trim().length() > 0) {
                            privacyId = (Long) spelService.getValue(templateActivity.getPrivacyIdExpr(), taContext);
                        }
                        if (templateActivity.getPriorityExpr() != null && templateActivity.getPriorityExpr().trim().length() > 0) {
                            priority = (Integer) spelService.getValue(templateActivity.getPriorityExpr(), taContext);
                        }
                    } catch (ExecutionException | RuntimeException e) {
                        throw new EventOrchestratorFatalException("Unable to get privacyId expr for TemplateActivity with id " + templateActivity.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
                    }
                    EventActivity.EventActivityBuilder processActivityBuilder = EventActivity.builder()
                            .contextId(requestContext.getId())
                            .processId((process != null) ? process.getId() : null)
                            .processName((process != null) ? getEntityAlias(process.getClass().getName()) : null)
                            .parentMemoId(parentMemoId)
                            .type(templateActivity.getType())
                            .executionType(templateActivity.getExecutionType())
                            .code(templateActivity.getCode())
                            .plannedDate(taaPlannedDate)
                            .status(EventActivityStatus.PENDING)
                            .retry(0)
                            .qualifier(templateActivity.getQualifierExpr2())
                            .executor(templateActivity.getExecutorExpr2())
                            .systemMemo(templateActivity.getSystemMemoExpr2())
                            .entityName(entityName)
                            .entityId(entityId)
                            .parameters(parameters)
                            .context(requestContext.getDetails())
                            .visibility(templateActivity.getVisibilityDefault() != null ? templateActivity.getVisibilityDefault() : EventVisibility.ADMIN)
                            .visibilitySuccess(templateActivity.getVisibilitySuccess() != null ? templateActivity.getVisibilitySuccess() : EventVisibility.ADMIN)
                            .priority(priority)
                            .privacyId(privacyId);
                    if (EventActivityExecutionMode.ASYNC.equals(templateActivity.getExecutionMode())) {
                        asyncEventActivityList.add(processActivityBuilder.build());
                    } else {
                        syncEventActivityList.add(processActivityBuilder.build());
                    }
                }
            }
        }
        eventActivityService.saveAll(asyncEventActivityList);
        RequestContext finalRequestContext1 = requestContext;
        List<EventMemo> eventMemoList = syncEventActivityList.stream()
                .map(eventActivity -> {
                    EventMemo eventMemo = executeEventActivity(finalRequestContext1, eventActivity, EventActivityExecutionMode.SYNC);
                    if (EventMemoStatus.ERROR.equals(eventMemo.getStatus())) {
                        throw new EventOrchestratorFatalException("Error saving sync generated memo for event activity " + eventActivity.getId() + " and process with id " + eventActivity.getProcessId());
                    }
                    eventMemo.setVisibility(eventActivity.getVisibilitySuccess());
                    return eventMemo;
                })
                .collect(Collectors.toList());
        eventMemoService.saveAll(eventMemoList);
    }

    @Override
    @Transactional
    public EventMemo executeEventActivity(RequestContext requestContext, EventActivity eventActivity, EventActivityExecutionMode executionMode) {
        log.info("start execute EventActivity id={}, code={}, processId={}, executionMode={}, requestContext={}", eventActivity.getId(), eventActivity.getCode(), eventActivity.getProcessId(), executionMode, requestContext.getId());
        EventMemo eventMemoResult = null;
        EventMemo.EventMemoBuilder processMemoBuilder = EventMemo.builder();
        EventOrchestratorProcess process = null;
        try {
            requestContext = getOrDefault(requestContext);

            processMemoBuilder
                    .parentId(eventActivity.getParentMemoId())
                    .processName(eventActivity.getProcessName())
                    .processId(eventActivity.getProcessId())
                    .type(eventActivity.getType())
                    .code(eventActivity.getCode())
                    .entityName(eventActivity.getEntityName())
                    .entityId(eventActivity.getEntityId())
                    .parameters(eventActivity.getParameters())
                    .visibility(eventActivity.getVisibility() != null ? eventActivity.getVisibility() : EventVisibility.ADMIN)
                    .privacyId(eventActivity.getPrivacyId())
                    .context(eventActivity.getContext())
                    .contextId(eventActivity.getContextId())
            ;

            process = (EventOrchestratorProcess) getEntity(eventActivity.getProcessName(), eventActivity.getProcessId());

            ConcurrentHashMap<String, Object> paContextMap = new ConcurrentHashMap<>();
            if (requestContext != null) {
                paContextMap.put("requestContext", requestContext);
            }
            if (process != null) {
                paContextMap.put("process", process);
            }

            Object entity = getEntity(eventActivity.getEntityName(), eventActivity.getEntityId());
            if (entity != null) {
                paContextMap.put("entity", entity);
            }
            paContextMap.put("eventActivity", eventActivity);

            EvaluationContext paContext = spelService.getContext(null, paContextMap);

            Boolean paQualified = null;
            try {
                paQualified = (Boolean) spelService.getValue(eventActivity.getQualifier(), paContext);
            } catch (ExecutionException | RuntimeException e) {
                throw new EventOrchestratorFatalException("Unable to get qualifier expression for ProcessActivity with id " + eventActivity.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
            }

            String systemMemo = null;
            try {
                systemMemo = String.valueOf(spelService.getValue(eventActivity.getSystemMemo(), paContext));
            } catch (ExecutionException | RuntimeException e) {
                throw new EventOrchestratorFatalException("Unable to get system memo expr for ProcessActivity with id " + eventActivity.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
            }

            processMemoBuilder.systemMemo(systemMemo);

            if (paQualified) {
                Object paExecutor = null;
                try {
                    if (eventActivity.getExecutor() != null && eventActivity.getExecutor().trim().length() > 0) {
                        paExecutor = spelService.getValue(eventActivity.getExecutor(), paContext);
                    }
                } catch (ExecutionException | RuntimeException e) {
                    throw new EventOrchestratorFatalException("Unable to get executor expr for ProcessActivity with id " + eventActivity.getId() + " and process with id " + ((process != null) ? String.valueOf(process.getId()) : "null"), e);
                }
                processMemoBuilder.status(EventMemoStatus.SUCCESS);
                processMemoBuilder.visibility(eventActivity.getVisibilitySuccess());
            } else {
                processMemoBuilder.status(EventMemoStatus.DISQUALIFIED);
            }
            if (EventActivityExecutionMode.ASYNC.equals(executionMode)) {
                eventActivityService.deleteById(eventActivity.getId());
            }
            log.info("executed EventActivity id={}, code={}, processId={}, executionMode={}, requestContext={}", eventActivity.getId(), eventActivity.getCode(), eventActivity.getProcessId(), executionMode, requestContext.getId());
        } catch (Exception e) {
            ExceptionType exceptionType = EventOrchestratorExceptionUtils.getExceptionType(e);
            log.error("unable to execute EventActivity id={}, code={}, processId={}, executionMode={}, requestContext={}, exceptionType={}, error={}, cause={}", eventActivity.getId(), eventActivity.getCode(), eventActivity.getProcessId(), executionMode, requestContext.getId(), exceptionType.name(), e.getMessage(), EventOrchestratorExceptionUtils.getExceptionCause(e).getMessage());
            String statusDescription = "";
            statusDescription = statusDescription + e.getLocalizedMessage();
            statusDescription = statusDescription + "; " + EventOrchestratorExceptionUtils.getExceptionCause(e).getMessage();

            processMemoBuilder.status(EventMemoStatus.ERROR);
            processMemoBuilder.statusDescription(statusDescription);


            eventActivity.setStatusDescription(statusDescription);
            switch (exceptionType) {

                case RETRY:
                    eventActivity.setStatus(EventActivityStatus.PENDING_RETRY);
                    eventActivity.setRetry(eventActivity.getRetry() + 1);
                    eventActivity.setRetryDate(new Date());
                    break;

                case FINAL:
                    eventActivity.setStatus(EventActivityStatus.ERROR);
                    break;

                case FATAL:
                    eventActivity.setStatus(EventActivityStatus.ERROR);
                    if (process != null) {
                        process.setFatalException(e);
                    }

            }

            if (EventActivityExecutionMode.ASYNC.equals(executionMode)) {
                if (EventActivityStatus.PENDING_RETRY.equals(eventActivity.getStatus())) {
                    eventActivityService.save(eventActivity);
                } else {
                    eventActivityService.deleteById(eventActivity.getId());
                }
            }

        }
        if (EventActivityExecutionMode.ASYNC.equals(executionMode)) {
            eventMemoResult = eventMemoService.save(processMemoBuilder.build());
        } else {
            eventMemoResult = processMemoBuilder.build();
        }

        return eventMemoResult;
    }

    @Override
    public Map<String, Object> createParameters(Object... items) {
        Map<String, Object> parameters = new HashMap<>();
        for (int index = 0; index < items.length / 2; index++) {
            parameters.put(String.valueOf(items[index * 2]), items[index * 2 + 1]);
        }
        return parameters;
    }

    @Override
    public Object getEntity(String name, Object id) {
        String entityId = (id != null) ? String.valueOf(id) : null;
        return eventOrchestratorPluginRegistry.getPluginFor(name).getEntity(name, entityId);
    }

    @Override
    public String getEntityAlias(String name) {
        if (name == null) {
            return null;
        }
        return eventOrchestratorPluginRegistry.getPluginFor(name).getEntityAlias(name);
    }

}
