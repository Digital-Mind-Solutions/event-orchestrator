package org.digitalmind.eventorchestrator.mapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.digitalmind.buildingblocks.templating.templatingcore.dto.TemplateIdentifier;
import org.digitalmind.buildingblocks.templating.templatingcore.dto.TemplateResult;
import org.digitalmind.buildingblocks.templating.templatingcore.service.TemplateService;
import org.digitalmind.eventorchestrator.dto.EventMemoDTO;
import org.digitalmind.eventorchestrator.dto.base.IMapper;
import org.digitalmind.eventorchestrator.entity.EventMemo;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mapping.context.MappingContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mapper(implementationName = "mapStructEventMemoMapperImpl",
        componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
        })
@Slf4j
public abstract class EventMemoMapper implements IMapper {

    @Autowired
    private TemplateService templateService;

    private Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public EventMemoDTO toDTO(EventMemo eventMemo, @Context MappingContext mappingContext) {
        return toDTO(eventMemo, mappingContext, 1);
    }

    public EventMemoDTO toDTO(EventMemo eventMemo, @Context MappingContext mappingContext, @Context int level) {
        if (eventMemo == null) {
            return null;
        }

        String codeI18nNamespace = "eventMapper".toLowerCase().replace(".", "/");
        String descriptionI18nNamespace = "eventMapper".toLowerCase().replace(".", "/");
        String codeI18nKey = eventMemo.getCode() + "_" + "CODE" + "_" + eventMemo.getStatus().name();
        String memoI18nKey = eventMemo.getCode() + "_" + "MEMO" + "_" + eventMemo.getStatus().name();

        String codeI18n = codeI18nKey;
        String memoI18n = memoI18nKey;

        Map<String, Object> context = new HashMap<>();
        context.put("eventType", eventMemo.getClass().getSimpleName());
        context.put("eventMemo", eventMemo);
        context.put("event", eventMemo);
        try {
            TemplateIdentifier templateIdentifierCode = TemplateIdentifier.builder()
                    .namespace(codeI18nNamespace)
                    .name(codeI18nKey)
                    .build();
            TemplateResult templateResultCode = templateService.execute(templateIdentifierCode, context, getLocale(), null);
            codeI18n = IOUtils.toString(templateResultCode.getResource().getInputStream(), "UTF-8");
        } catch (Exception e) {
            //log.error("Unable to translate eventMemo id={}, code={}", eventMemo.getId(), eventMemo.getCode());
        }

        try {
            TemplateIdentifier templateIdentifierDescription = TemplateIdentifier.builder()
                    .namespace(descriptionI18nNamespace)
                    .name(memoI18nKey)
                    .build();
            TemplateResult templateResultDescription = templateService.execute(templateIdentifierDescription, context, getLocale(), null);
            memoI18n = IOUtils.toString(templateResultDescription.getResource().getInputStream(), "UTF-8");
        } catch (Exception e) {
            //log.error("Unable to translate eventMemo id={}, code={}", eventMemo.getId(), eventMemo.getCode());
        }

        return EventMemoDTO.builder()
                .id(eventMemo.getId())
                .processId(eventMemo.getProcessId())
                .parentId(eventMemo.getParentId())
                .type(eventMemo.getType())
                .code(codeI18n)
                .status(eventMemo.getStatus())
                .statusDescription(eventMemo.getStatusDescription())
                .systemMemo(memoI18n)
                .entityName(eventMemo.getEntityName())
                .entityId(eventMemo.getEntityId())
                .parameters(eventMemo.getParameters())
                .context(eventMemo.getContext())
                .contextId(eventMemo.getContextId())
                .createdAt(eventMemo.getCreatedAt())
                .build();
    }

}
