package org.digitalmind.eventorchestrator.enumeration;

import lombok.Getter;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public enum EventVisibility {
    ADMIN(0),
    BACKOFFICE(1),
    AGENT(2),
    CUSTOMER(3);

    private final int priority;

    EventVisibility(int priority) {
        this.priority = priority;
    }

    public Set<EventVisibility> getAuthorizedSet() {
        return EnumSet.allOf(EventVisibility.class).stream()
                .filter(eventVisibilityAuthorized -> this.getPriority() <= eventVisibilityAuthorized.getPriority())
                .collect(Collectors.toSet());
    }

}
