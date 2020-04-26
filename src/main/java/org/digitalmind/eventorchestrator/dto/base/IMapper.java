package org.digitalmind.eventorchestrator.dto.base;

import org.apache.commons.lang3.StringUtils;
import org.digitalmind.eventorchestrator.dto.exception.MapperBadRequestException;

public abstract interface IMapper {

    default String getMapperName() {
        String beanName = this.getClass().getSimpleName();
        if (beanName.endsWith("Impl")) {
            beanName = beanName.substring(0, beanName.length() - 4);
        }
        beanName = StringUtils.capitalize(beanName);
        return beanName;
    }

    default void throwToDTOException(String message) {
        throw new MapperBadRequestException(getMapperName() + ".toDTO: " + message);
    }

    default void throwToDTOException(String message, Throwable e) {
        throw new MapperBadRequestException(getMapperName() + ".toDTO: " + message, e);
    }

    default void throwToEntityException(String message) {
        throw new MapperBadRequestException(getMapperName() + ".toEntity: " + message);
    }

    default void throwToEntityException(String message, Throwable e) {
        throw new MapperBadRequestException(getMapperName() + ".toEntity: " + message, e);
    }

}
