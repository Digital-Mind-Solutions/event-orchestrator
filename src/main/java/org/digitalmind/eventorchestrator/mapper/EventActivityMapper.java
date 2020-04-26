package org.digitalmind.eventorchestrator.mapper;

import org.apache.commons.io.IOUtils;
import org.digitalmind.buildingblocks.templating.templatingcore.dto.TemplateIdentifier;
import org.digitalmind.buildingblocks.templating.templatingcore.dto.TemplateResult;
import org.digitalmind.buildingblocks.templating.templatingcore.service.TemplateService;
import org.digitalmind.eventorchestrator.dto.EventActivityDTO;
import org.digitalmind.eventorchestrator.dto.base.IMapper;
import org.digitalmind.eventorchestrator.entity.EventActivity;
import org.mapstruct.Context;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mapping.context.MappingContext;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Mapper(implementationName = "mapStructEventActivityMapperImpl",
        componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
        })
public abstract class EventActivityMapper implements IMapper {

    @Autowired
    private TemplateService templateService;

    private Locale getLocale() {
        return LocaleContextHolder.getLocale();
    }

    public EventActivityDTO toDTO(EventActivity eventActivity, @Context MappingContext mappingContext) {
        return toDTO(eventActivity, mappingContext, 1);
    }

    public EventActivityDTO toDTO(EventActivity eventActivity, @Context MappingContext mappingContext, @Context int level) {
        if (eventActivity == null) {
            return null;
        }

        String codeI18nNamespace = "eventMapper".toLowerCase().replace(".", "/");
        String descriptionI18nNamespace = "eventMapper".toLowerCase().replace(".", "/");
        String codeI18nKey = eventActivity.getCode() + "_" + "CODE" + "_" + eventActivity.getStatus().name();
        String memoI18nKey = eventActivity.getCode() + "_" + "MEMO" + "_" + eventActivity.getStatus().name();

        String codeI18n = codeI18nKey;
        String memoI18n = memoI18nKey;

        Map<String, Object> context = new HashMap<>();
        context.put("eventType", eventActivity.getClass().getSimpleName());
        context.put("eventActivity", eventActivity);
        context.put("event", eventActivity);
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
        return EventActivityDTO.builder()
                .id(eventActivity.getId())
                .processId(eventActivity.getProcessId())
                .parentMemoId(eventActivity.getParentMemoId())
                .type(eventActivity.getType())
                .code(codeI18n)
                .plannedDate(eventActivity.getPlannedDate())
                .retryDate(eventActivity.getRetryDate())
                .status(eventActivity.getStatus())
                .statusDescription(eventActivity.getStatusDescription())
                .systemMemo(memoI18n)
                .entityName(eventActivity.getEntityName())
                .entityId(eventActivity.getEntityId())
                .parameters(eventActivity.getParameters())
                .context(eventActivity.getContext())
                .contextId(eventActivity.getContextId())
                .createdAt(eventActivity.getCreatedAt())
                .build();
    }

}
