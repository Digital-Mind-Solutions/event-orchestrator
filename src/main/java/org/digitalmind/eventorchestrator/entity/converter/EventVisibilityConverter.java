package org.digitalmind.eventorchestrator.entity.converter;

import org.digitalmind.eventorchestrator.enumeration.EventVisibility;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

@Converter(autoApply = false)
public class EventVisibilityConverter implements AttributeConverter<EventVisibility, Integer> {
    @Override
    public Integer convertToDatabaseColumn(EventVisibility attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getPriority();
    }

    @Override
    public EventVisibility convertToEntityAttribute(Integer dbData) {
        if (dbData == null) {
            return null;
        }

        return Stream.of(EventVisibility.values())
                .filter(c -> c.getPriority() == dbData.intValue())
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
