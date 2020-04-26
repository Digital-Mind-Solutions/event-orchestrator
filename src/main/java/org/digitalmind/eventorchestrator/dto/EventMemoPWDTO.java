package org.digitalmind.eventorchestrator.dto;

import org.digitalmind.buildingblocks.core.dtobase.PageWrapperDTO;
import org.springframework.data.domain.Page;

public class EventMemoPWDTO extends PageWrapperDTO<EventMemoDTO> {

    public EventMemoPWDTO(Page<EventMemoDTO> value) {
        super(value);
    }

}
