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
                Sort sortBy = new Sort(Sort.Direction.ASC, "entityName", "type", "priority");
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

    private RequestContext getRequestContext(AbstractEvent event, EvaluationContext context) {
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

    private void applyDirectives(ConfigurationDirectiveKey key, AbstractEvent event) {
        if (this.exceptionList.contains(key.getEntityName())) {
            return;
        }

        //System.inbound.println("applyDirectives: " + code + ", event: " + event);
        List<EventDirective> eventDirectiveList = cache.getIfPresent(key);

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


            for (EventDirective eventDirective : eventDirectiveList) {
                boolean qualify = false;
                try {
                    qualify = (boolean) spelService.getValue(eventDirective.getQualifier(), context);
                    if (qualify) {
                        spelService.getValue(eventDirective.getExecutor(), context);
                    }
                } catch (Exception e) {
                    //e.printStackTrace();
                    log.error("Event directive id {} execution error: {}", eventDirective.getId(), e.getLocalizedMessage());
                    throw new EventDirectiveFinalException(e);
                }
            }
        }
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
