package org.digitalmind.eventorchestrator.dto;


import org.digitalmind.buildingblocks.core.dtobase.PageWrapperDTO;
import org.springframework.data.domain.Page;

public class EventActivityPWDTO extends PageWrapperDTO<EventActivityDTO> {

    public EventActivityPWDTO(Page<EventActivityDTO> value) {
        super(value);
    }

}
