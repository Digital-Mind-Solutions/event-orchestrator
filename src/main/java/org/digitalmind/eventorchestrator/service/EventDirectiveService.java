package org.digitalmind.eventorchestrator.service;

import org.hibernate.event.spi.*;

import java.util.Collection;

public interface EventDirectiveService {

    public void onPreLoad(PreLoadEvent event);

    public void onPostLoad(PostLoadEvent event);

    public boolean onPreInsert(PreInsertEvent event);

    public void onPostInsert(PostInsertEvent event);

    public boolean onPreUpdate(PreUpdateEvent event);

    public void onPostUpdate(PostUpdateEvent event);

    public boolean isUpdated(PostUpdateEvent event, String fieldName);

    public boolean contains(PostCollectionRecreateEvent event, String className);

    public boolean contains(PostCollectionRecreateEvent event, Class<?> clazz);

    public Collection getCollection(PostCollectionRecreateEvent event, String className);

    public Collection getCollection(PostCollectionRecreateEvent event, Class<?> Clazz);

    public boolean onPreDelete(PreDeleteEvent event);

    public void onPostDelete(PostDeleteEvent event);

    public void onPostRecreateCollection(PostCollectionRecreateEvent event);

    public void onPostRemoveCollection(PostCollectionRemoveEvent event);

    public void onPostUpdateCollection(PostCollectionUpdateEvent event);

    public void onPreRecreateCollection(PreCollectionRecreateEvent event);

    public void onPreRemoveCollection(PreCollectionRemoveEvent event);

    public void onPreUpdateCollection(PreCollectionUpdateEvent event);

}
