package app.rescue.backend.controller;

import app.rescue.backend.dto.ConversationInitializationDTO;
import app.rescue.backend.dto.MessageDto;
import app.rescue.backend.service.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

@RestController
//@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PutMapping(path = "conversation")
    public ResponseEntity establishConversation(@RequestBody ConversationInitializationDTO conversationInitializationDTO) {
        chatService.establishConversation(conversationInitializationDTO);

        return new ResponseEntity(HttpStatus.OK);
    }


    //@MessageMapping("{conversationId}")
    //@SendTo("/topic/{conversationId}")
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public MessageDto sendMessage(MessageDto messageDto) {
        chatService.submitMessage(String.valueOf(2), messageDto);

        return messageDto;
    }

}
