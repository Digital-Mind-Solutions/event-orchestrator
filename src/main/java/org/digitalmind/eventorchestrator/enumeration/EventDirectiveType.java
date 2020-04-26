package org.digitalmind.eventorchestrator.enumeration;

public enum EventDirectiveType {

    PRE_LOAD,
    POST_LOAD,

    PRE_INSERT,
    POST_INSERT,

    PRE_UPDATE,
    POST_UPDATE,

    PRE_DELETE,
    POST_DELETE,

    PRE_COLLECTION_RECREATE,
    POST_COLLECTION_RECREATE,

    PRE_COLLECTION_REMOVE,
    POST_COLLECTION_REMOVE,

    PRE_COLLECTION_UPDATE,
    POST_COLLECTION_UPDATE;

}
