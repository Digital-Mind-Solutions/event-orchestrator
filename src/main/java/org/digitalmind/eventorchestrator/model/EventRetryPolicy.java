package org.digitalmind.eventorchestrator.model;


import lombok.*;
import lombok.experimental.SuperBuilder;
import org.digitalmind.eventorchestrator.enumeration.ExceptionType;

@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class EventRetryPolicy {
    private ExceptionType exceptionType;
    private Integer delay;
}
