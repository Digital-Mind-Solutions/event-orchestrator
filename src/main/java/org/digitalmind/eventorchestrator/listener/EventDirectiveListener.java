package org.digitalmind.eventorchestrator.listener;

import org.digitalmind.eventorchestrator.service.EventDirectiveService;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

@Component
public class EventDirectiveListener implements

        PreCollectionRecreateEventListener, PostCollectionRecreateEventListener,
        PreCollectionRemoveEventListener, PostCollectionRemoveEventListener,
        PreCollectionUpdateEventListener, PostCollectionUpdateEventListener,

        PreInsertEventListener, PostInsertEventListener,
        PreUpdateEventListener, PostUpdateEventListener,
        PreDeleteEventListener, PostDeleteEventListener,
        PreLoadEventListener, PostLoadEventListener {

    private final EventDirectiveService eventDirectiveService;
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    public EventDirectiveListener(
            EventDirectiveService eventDirectiveService,
            @Qualifier("esigDBEntityManagerFactory") EntityManagerFactory entityManagerFactory
    ) {
        this.eventDirectiveService = eventDirectiveService;
        this.entityManagerFactory = entityManagerFactory;
    }

    @PostConstruct
    public void postConstruct() {
        registerListeners(entityManagerFactory, this);
    }

    public void registerListeners(EntityManagerFactory entityManagerFactory, EventDirectiveListener eventDirectiveListener) {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        final EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);

        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(eventDirectiveListener);
        registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(eventDirectiveListener);

        registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener(eventDirectiveListener);
        registry.getEventListenerGroup(EventType.PRE_LOAD).appendListener(eventDirectiveListener);

        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(eventDirectiveListener);
        registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(eventDirectiveListener);

        registry.getEventListenerGroup(EventType.POST_DELETE).appendListener(eventDirectiveListener);
        registry.getEventListenerGroup(EventType.POST_LOAD).appendListener(eventDirectiveListener);

        registry.getEventListenerGroup(EventType.PRE_COLLECTION_RECREATE).appendListener(eventDirectiveListener);
        registry.getEventListenerGroup(EventType.POST_COLLECTION_RECREATE).appendListener(eventDirectiveListener);

        registry.getEventListenerGroup(EventType.PRE_COLLECTION_REMOVE).appendListener(eventDirectiveListener);
        registry.getEventListenerGroup(EventType.POST_COLLECTION_REMOVE).appendListener(eventDirectiveListener);

        registry.getEventListenerGroup(EventType.PRE_COLLECTION_UPDATE).appendListener(eventDirectiveListener);
        registry.getEventListenerGroup(EventType.POST_COLLECTION_UPDATE).appendListener(eventDirectiveListener);

    }

    @Override
    public void onPostDelete(PostDeleteEvent event) {
        eventDirectiveService.onPostDelete(event);
    }

    @Override
    public void onPostInsert(PostInsertEvent event) {
        eventDirectiveService.onPostInsert(event);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        eventDirectiveService.onPostUpdate(event);
    }

    @Override
    public void onPostLoad(PostLoadEvent event) {
        eventDirectiveService.onPostLoad(event);
    }

    @Override
    public boolean onPreDelete(PreDeleteEvent event) {
        return eventDirectiveService.onPreDelete(event);
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        return eventDirectiveService.onPreInsert(event);
    }

    @Override
    public void onPreLoad(PreLoadEvent event) {
        eventDirectiveService.onPreLoad(event);
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        return eventDirectiveService.onPreUpdate(event);
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        return false;
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    @Override
    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        eventDirectiveService.onPostRecreateCollection(event);
    }

    @Override
    public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
        eventDirectiveService.onPostRemoveCollection(event);
    }

    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
        eventDirectiveService.onPostUpdateCollection(event);
    }

    @Override
    public void onPreRecreateCollection(PreCollectionRecreateEvent event) {
        eventDirectiveService.onPreRecreateCollection(event);
    }

    @Override
    public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
        eventDirectiveService.onPreRemoveCollection(event);
    }

    @Override
    public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
        eventDirectiveService.onPreUpdateCollection(event);
    }

}
