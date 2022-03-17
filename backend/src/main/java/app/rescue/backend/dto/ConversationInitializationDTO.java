package app.rescue.backend.dto;

import lombok.Data;

@Data
public class ConversationInitializationDTO {
    private final Long userOneId;
    private final Long userTwoId;
}
