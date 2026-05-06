package org.digitalmind.eventorchestrator.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.digitalmind.buildingblocks.core.requestcontext.dto.RequestContext;
import org.digitalmind.buildingblocks.core.requestcontext.service.RequestContextService;
import org.digitalmind.buildingblocks.core.spel.service.SpelService;
import org.digitalmind.eventorchestrator.config.EventDirectiveConfig;
import org.digitalmind.eventorchestrator.entity.EventDirective;
import org.digitalmind.eventorchestrator.enumeration.EventDirectiveType;
import org.digitalmind.eventorchestrator.exception.EventDirectiveFinalException;
import org.digitalmind.eventorchestrator.repository.EventDirectiveRepository;
import org.digitalmind.eventorchestrator.service.EventDirectiveService;
import org.hibernate.event.spi.*;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@Service("eventDirectiveService")
@Slf4j
@Transactional
public class EventDirectiveServiceImpl implements EventDirectiveService, Runnable {

    private final EventDirectiveConfig eventDirectiveConfig;
    private final EventDirectiveRepository eventDirectiveRepository;
    private final SpelService spelService;
    private final CacheLoader<ConfigurationDirectiveKey, List<EventDirective>> cacheLoader;
    private final LoadingCache<ConfigurationDirectiveKey, List<EventDirective>> cache;
    private final List<String> exceptionList;
    private final RequestContextService requestContextService;

    @Override
    public void run() {
        try {
            this.cache.putAll(this.cacheLoader.loadAll(null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ConfigurationDirectiveKey {
        public ConfigurationDirectiveKey(String entityName, EventDirectiveType type) {
            this.entityName = entityName;
            this.type = type;
        }

        private String entityName;
        private EventDirectiveType type;

        public String getEntityName() {
            return entityName;
        }

        public EventDirectiveType getType() {
            return type;
        }

        @Override
        public String toString() {
            return "ConfigurationDirectiveKey{" +
                    "entityName='" + entityName + '\'' +
                    ", type=" + type +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigurationDirectiveKey that = (ConfigurationDirectiveKey) o;
            return Objects.equals(entityName, that.entityName) &&
                    type == that.type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityName, type);
        }
    }


    @Autowired
    public EventDirectiveServiceImpl(
            EventDirectiveConfig eventDirectiveConfig,
            EventDirectiveRepository eventDirectiveRepository,
            SpelService spelService,
            RequestContextService requestContextService) {
        this.eventDirectiveConfig = eventDirectiveConfig;
        this.eventDirectiveRepository = eventDirectiveRepository;
        this.spelService = spelService;
        this.cacheLoader = new CacheLoader<ConfigurationDirectiveKey, List<EventDirective>>() {
            @Override
            public List<EventDirective> load(ConfigurationDirectiveKey key) {
                return eventDirectiveRepository.findByEntityNameAndTypeOrderByPriority(
                        key.getEntityName(),
                        key.getType()
                );
            }

            @Override
            public Map<ConfigurationDirectiveKey, List<EventDirective>> loadAll(Iterable<? extends ConfigurationDirectiveKey> keys) throws Exception {
                Map<ConfigurationDirectiveKey, List<EventDirective>> cacheList = new HashMap<>();
                //Sort sortBy = new Sort(Sort.Direction.ASC, "entityName", "type", "priority");
                Sort sortBy = Sort.by(Sort.Direction.DESC,"entityName", "type", "priority");
                List<EventDirective> eventDirectiveList = eventDirectiveRepository.findAll(sortBy);
                ConfigurationDirectiveKey key = null;
                ConfigurationDirectiveKey keyPrevious = null;
                List<EventDirective> eventDirective4Key = null;
                for (EventDirective eventDirective : eventDirectiveList) {
                    key = new ConfigurationDirectiveKey(eventDirective.getEntityName(), eventDirective.getType());
                    if (key.equals(keyPrevious)) {
                        eventDirective4Key.add(eventDirective);
                    } else {
                        if (eventDirective4Key != null) {
                            cacheList.put(keyPrevious, eventDirective4Key);
                        }
                        eventDirective4Key = new ArrayList<>();
                        eventDirective4Key.add(eventDirective);
                    }
                    keyPrevious = key;
                }
                if (eventDirective4Key != null) {
                    cacheList.put(keyPrevious, eventDirective4Key);
                }
                return cacheList;
            }
        };
        this.cache = CacheBuilder
                .from(CacheBuilderSpec.parse(eventDirectiveConfig.getCacheBuilderSpecification()))
                .build(cacheLoader);
        this.requestContextService = requestContextService;
        this.exceptionList = new ArrayList<String>() {{
            add(EventDirective.class.getName());
        }};

        this.run();

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
    }

    private RequestContext getRequestContext(Object event, EvaluationContext context) {
        //if the entity has context_id attribute reuse the context with that id else create a new one
        RequestContext requestContext = null;
        try {
            EntityPersister persister = (EntityPersister) spelService.getValue("#event.persister", context);
            int index = getPropertyIndex(persister, "contextId");
            String contextId = null;
            if (index >= 0) {
                Object[] state = (Object[]) spelService.getValue("#event.state", context);
                contextId = (String) state[index];
            }
            if (contextId != null) {
                requestContext = requestContextService.create(contextId);
            } else {
                requestContext = requestContextService.create();
            }
        } catch (Exception e) {
            requestContext = requestContextService.create();
        }
        return requestContext;
    }

    private void applyDirectives(ConfigurationDirectiveKey key, Object event) {
        String rcaTraceId = UUID.randomUUID().toString().substring(0, 8);
        Object eventId = extractEventId(event);
        if (this.exceptionList.contains(key.getEntityName())) {
            log.debug("[RCA-EO] trace={} skip directives for exception entityName={}, type={}", rcaTraceId, key.getEntityName(), key.getType());
            return;
        }

        log.debug(
                "[RCA-EO] trace={} applyDirectives start entityName={}, type={}, eventClass={}, eventId={}",
                rcaTraceId,
                key.getEntityName(),
                key.getType(),
                event != null ? event.getClass().getName() : null,
                eventId
        );
        List<EventDirective> eventDirectiveList = cache.getIfPresent(key);
        log.debug(
                "[RCA-EO] trace={} directives fetched count={}",
                rcaTraceId,
                eventDirectiveList != null ? eventDirectiveList.size() : 0
        );
        log.debug(
                "[RCA-EO] trace={} applyDirectives eventEntity fingerprint={}",
                rcaTraceId,
                describeObjectIdentity(extractEventEntity(event))
        );

        if (eventDirectiveList != null && !eventDirectiveList.isEmpty()) {
            EvaluationContext context = spelService.getContext(
                    null,
                    new HashMap<String, Object>() {{
                        put("event", event);
                    }}
            );
            RequestContext requestContext = getRequestContext(event, context);
            if (requestContext != null) {
                context.setVariable("requestContext", requestContext);
            }
            log.debug(
                    "[RCA-EO] trace={} directiveContext entityName={} type={} requestContextId={}",
                    rcaTraceId,
                    key.getEntityName(),
                    key.getType(),
                    requestContext != null ? requestContext.getId() : null
            );
            logEventStateSnapshot(rcaTraceId, event);
            log.debug(
                    "[RCA-EO] trace={} eventEntity fingerprint={}",
                    rcaTraceId,
                    describeObjectIdentity(extractEventEntity(event))
            );


            for (EventDirective eventDirective : eventDirectiveList) {
                boolean qualify = false;
                try {
                    long qualifierStartNanos = System.nanoTime();
                    qualify = (boolean) spelService.getValue(eventDirective.getQualifier(), context);
                    long qualifierElapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - qualifierStartNanos);
                    log.debug(
                            "[RCA-EO] trace={} directiveId={} priority={} qualifierResult={} qualifierElapsedMs={} qualifierExpr={}",
                            rcaTraceId,
                            eventDirective.getId(),
                            eventDirective.getPriority(),
                            qualify,
                            qualifierElapsedMs,
                            eventDirective.getQualifier()
                    );
                    if (qualify) {
                        log.debug(
                                "[RCA-EO] trace={} directiveId={} executorInput eventEntityFingerprint={}",
                                rcaTraceId,
                                eventDirective.getId(),
                                describeObjectIdentity(extractEventEntity(event))
                        );
                        long executorStartNanos = System.nanoTime();
                        spelService.getValue(eventDirective.getExecutor(), context);
                        long executorElapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executorStartNanos);
                        log.debug(
                                "[RCA-EO] trace={} directiveId={} executorOk=true executorElapsedMs={} executorExpr={} eventEntityFingerprintAfter={}",
                                rcaTraceId,
                                eventDirective.getId(),
                                executorElapsedMs,
                                eventDirective.getExecutor(),
                                describeObjectIdentity(extractEventEntity(event))
                        );
                    } else {
                        log.debug(
                                "[RCA-EO] trace={} directiveId={} skipped executor because qualifier=false",
                                rcaTraceId,
                                eventDirective.getId()
                        );
                    }
                } catch (Exception e) {
                    Throwable rootCause = e;
                    while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                        rootCause = rootCause.getCause();
                    }
                    log.error(
                            "[RCA-EO] trace={} directiveId={} executionErrorClass={} message={} rootCauseClass={} rootCauseMessage={} qualifierExpr={} executorExpr={}",
                            rcaTraceId,
                            eventDirective.getId(),
                            e.getClass().getName(),
                            e.getLocalizedMessage(),
                            rootCause.getClass().getName(),
                            rootCause.getLocalizedMessage(),
                            eventDirective.getQualifier(),
                            eventDirective.getExecutor(),
                            e
                    );
                    throw new EventDirectiveFinalException(e);
                }
            }
        }
    }

    private void logEventStateSnapshot(String rcaTraceId, Object event) {
        EntityPersister persister = null;
        Object[] state = null;
        Object eventId = null;

        if (event instanceof PreInsertEvent) {
            PreInsertEvent e = (PreInsertEvent) event;
            persister = e.getPersister();
            state = e.getState();
            eventId = e.getId();
        } else if (event instanceof PostInsertEvent) {
            PostInsertEvent e = (PostInsertEvent) event;
            persister = e.getPersister();
            state = e.getState();
            eventId = e.getId();
        } else if (event instanceof PreUpdateEvent) {
            PreUpdateEvent e = (PreUpdateEvent) event;
            persister = e.getPersister();
            state = e.getState();
            eventId = e.getId();
        } else if (event instanceof PostUpdateEvent) {
            PostUpdateEvent e = (PostUpdateEvent) event;
            persister = e.getPersister();
            state = e.getState();
            eventId = e.getId();
        } else if (event instanceof PreDeleteEvent) {
            PreDeleteEvent e = (PreDeleteEvent) event;
            persister = e.getPersister();
            state = e.getDeletedState();
            eventId = e.getId();
        } else if (event instanceof PostDeleteEvent) {
            PostDeleteEvent e = (PostDeleteEvent) event;
            persister = e.getPersister();
            state = e.getDeletedState();
            eventId = e.getId();
        } else if (event instanceof PreLoadEvent) {
            PreLoadEvent e = (PreLoadEvent) event;
            persister = e.getPersister();
            state = e.getState();
            eventId = e.getId();
        } else if (event instanceof PostLoadEvent) {
            PostLoadEvent e = (PostLoadEvent) event;
            persister = e.getPersister();
            eventId = e.getId();
        } else {
            log.debug("[RCA-EO] trace={} event snapshot unavailable unsupportedEventClass={}", rcaTraceId, event != null ? event.getClass().getName() : null);
            return;
        }

        if (persister == null || state == null) {
            log.debug(
                    "[RCA-EO] trace={} event snapshot unavailable eventClass={} eventId={} persisterNull={} stateNull={}",
                    rcaTraceId,
                    event != null ? event.getClass().getName() : null,
                    eventId,
                    persister == null,
                    state == null
            );
            return;
        }
        String[] propertyNames = persister.getPropertyNames();
        log.debug(
                "[RCA-EO] trace={} event snapshot entityName={} id={} stateSize={}",
                rcaTraceId,
                persister.getEntityName(),
                eventId,
                state.length
        );
        logStateField(rcaTraceId, "id", propertyNames, state);
        logStateField(rcaTraceId, "partitionKey", propertyNames, state);
        logStateField(rcaTraceId, "key", propertyNames, state);
        logStateField(rcaTraceId, "processId", propertyNames, state);
        logStateField(rcaTraceId, "processPartitionKey", propertyNames, state);
        logStateField(rcaTraceId, "contextId", propertyNames, state);
    }

    private Object extractEventId(Object event) {
        if (event instanceof PreInsertEvent) {
            return ((PreInsertEvent) event).getId();
        } else if (event instanceof PostInsertEvent) {
            return ((PostInsertEvent) event).getId();
        } else if (event instanceof PreUpdateEvent) {
            return ((PreUpdateEvent) event).getId();
        } else if (event instanceof PostUpdateEvent) {
            return ((PostUpdateEvent) event).getId();
        } else if (event instanceof PreDeleteEvent) {
            return ((PreDeleteEvent) event).getId();
        } else if (event instanceof PostDeleteEvent) {
            return ((PostDeleteEvent) event).getId();
        } else if (event instanceof PreLoadEvent) {
            return ((PreLoadEvent) event).getId();
        } else if (event instanceof PostLoadEvent) {
            return ((PostLoadEvent) event).getId();
        }
        return null;
    }

    private Object extractEventEntity(Object event) {
        if (event instanceof PreInsertEvent) {
            return ((PreInsertEvent) event).getEntity();
        } else if (event instanceof PostInsertEvent) {
            return ((PostInsertEvent) event).getEntity();
        } else if (event instanceof PreUpdateEvent) {
            return ((PreUpdateEvent) event).getEntity();
        } else if (event instanceof PostUpdateEvent) {
            return ((PostUpdateEvent) event).getEntity();
        } else if (event instanceof PreDeleteEvent) {
            return ((PreDeleteEvent) event).getEntity();
        } else if (event instanceof PostDeleteEvent) {
            return ((PostDeleteEvent) event).getEntity();
        } else if (event instanceof PreLoadEvent) {
            return ((PreLoadEvent) event).getEntity();
        } else if (event instanceof PostLoadEvent) {
            return ((PostLoadEvent) event).getEntity();
        }
        return null;
    }

    private String describeObjectIdentity(Object value) {
        if (value == null) {
            return "null";
        }
        return value.getClass().getName() +
                "@ih=" + System.identityHashCode(value) +
                ",id=" + extractPropertySafely(value, "id") +
                ",key=" + extractPropertySafely(value, "key") +
                ",partitionKey=" + extractPropertySafely(value, "partitionKey") +
                ",processId=" + extractPropertySafely(value, "processId");
    }

    private Object extractPropertySafely(Object value, String propertyName) {
        try {
            String getterName = "get" + propertyName.substring(0, 1).toUpperCase(Locale.ROOT) + propertyName.substring(1);
            java.lang.reflect.Method getter = value.getClass().getMethod(getterName);
            return getter.invoke(value);
        } catch (Exception ignored) {
            return "n/a";
        }
    }

    private void logStateField(String rcaTraceId, String fieldName, String[] propertyNames, Object[] state) {
        int idx = IntStream.range(0, propertyNames.length)
                .filter(i -> fieldName.equals(propertyNames[i]))
                .findFirst()
                .orElse(-1);
        Object value = idx >= 0 && idx < state.length ? state[idx] : null;
        log.debug("[RCA-EO] trace={} event field={} idx={} value={}", rcaTraceId, fieldName, idx, value);
    }

    @Override
    public void onPreLoad(PreLoadEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.PRE_LOAD);
        applyDirectives(key, event);
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.POST_LOAD);
        applyDirectives(key, event);
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        log.debug(
                "[RCA-EO] onPreInsert direct entityFingerprint={} eventId={}",
                describeObjectIdentity(event != null ? event.getEntity() : null),
                event != null ? event.getId() : null
        );
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.PRE_INSERT);
        applyDirectives(key, event);
        return false;
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.POST_INSERT);
        applyDirectives(key, event);
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.PRE_UPDATE);
        applyDirectives(key, event);

        return false;
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.POST_UPDATE);
        applyDirectives(key, event);
    }


    private int getPropertyIndex(EntityPersister persister, String fieldName) {
        if (persister == null) {
            return -1;
        }
        return Arrays.asList(persister.getPropertyNames()).indexOf(fieldName);
    }

    @Override
    public boolean isUpdated(PostUpdateEvent event, String fieldName) {
        final List<String> propNames = Arrays.asList(event.getPersister().getPropertyNames());
        final int index = propNames.indexOf(fieldName);
        return areDifferent(event.getOldState()[index], event.getState()[index]);
    }

    @Override
    public boolean contains(PostCollectionRecreateEvent event, String className) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return contains(event, clazz);
    }

    @Override
    public boolean contains(PostCollectionRecreateEvent event, Class<?> clazz) {
        if (event != null && event.getCollection() != null && event.getCollection().getStoredSnapshot() != null && event.getCollection().getStoredSnapshot() instanceof Collection) {
            return ((Collection) event.getCollection().getStoredSnapshot()).stream().filter(o -> o != null && clazz.isInstance(o)).findAny().isPresent();
        }
        return false;
    }

    @Override
    public Collection getCollection(PostCollectionRecreateEvent event, String className) {
        Class clazz = null;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return getCollection(event, clazz);
    }

    @Override
    public Collection getCollection(PostCollectionRecreateEvent event, Class<?> clazz) {
        if (event != null && event.getCollection() != null && event.getCollection().getStoredSnapshot() != null && event.getCollection().getStoredSnapshot() instanceof Collection) {
            return (Collection) ((Collection) event.getCollection().getStoredSnapshot()).stream().filter(o -> o != null && clazz.isInstance(o)).collect(Collectors.toList());
        }
        return null;
    }

    private boolean areDifferent(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return false;
        }
        if ((o1 != null && o2 == null) || (o1 == null && o2 != null)) {
            return true;
        }
        return !o1.equals(o2);
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.PRE_DELETE);
        applyDirectives(key, event);
        return false;
    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getPersister().getEntityName(), EventDirectiveType.POST_DELETE);
        applyDirectives(key, event);
    }

    @Override
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getAffectedOwnerEntityName(), EventDirectiveType.POST_COLLECTION_RECREATE);
        applyDirectives(key, event);
    }

    @Override
    public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getAffectedOwnerEntityName(), EventDirectiveType.POST_COLLECTION_REMOVE);
        applyDirectives(key, event);
    }

    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getAffectedOwnerEntityName(), EventDirectiveType.POST_COLLECTION_UPDATE);
        applyDirectives(key, event);
    }

    @Override
    public void onPreRecreateCollection(PreCollectionRecreateEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getAffectedOwnerEntityName(), EventDirectiveType.PRE_COLLECTION_RECREATE);
        applyDirectives(key, event);
    }

    @Override
    public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getAffectedOwnerEntityName(), EventDirectiveType.PRE_COLLECTION_REMOVE);
        applyDirectives(key, event);
    }

    @Override
    public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
        ConfigurationDirectiveKey key = new ConfigurationDirectiveKey(event.getAffectedOwnerEntityName(), EventDirectiveType.PRE_COLLECTION_UPDATE);
        applyDirectives(key, event);
    }


}
