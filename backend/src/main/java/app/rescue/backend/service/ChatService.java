package app.rescue.backend.service;

import app.rescue.backend.dto.ConversationInitializationDTO;
import app.rescue.backend.dto.MessageDto;
import app.rescue.backend.model.Conversation;
import app.rescue.backend.model.Message;
import app.rescue.backend.model.Participant;
import app.rescue.backend.model.User;
import app.rescue.backend.repository.ConversationRepository;
import app.rescue.backend.repository.MessageRepository;
import app.rescue.backend.repository.ParticipantRepository;
import app.rescue.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ParticipantRepository participantRepository;
    private final MessageRepository messageRepository;

    public ChatService(ConversationRepository conversationRepository, UserRepository userRepository, ParticipantRepository participantRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.messageRepository = messageRepository;
    }

    public void establishConversation(ConversationInitializationDTO conversationInitializationDTO) {
        if (conversationInitializationDTO.getUserOneId() == conversationInitializationDTO.getUserTwoId()) {
            throw new IllegalStateException("cannot send message to self");
        }
        Conversation conversation = new Conversation("test");
        conversationRepository.save(conversation);

        User userOne = userRepository.getById(conversationInitializationDTO.getUserOneId());

        Participant participantOne = new Participant(userOne, conversation);
        participantRepository.save(participantOne);

        User userTwo = userRepository.getById(conversationInitializationDTO.getUserTwoId());

        Participant participantTwo = new Participant(userTwo, conversation);
        participantRepository.save(participantTwo);
    }

    public void submitMessage(String conversationId, MessageDto messageDto) {
        //TODO
        Message message = mapFromDtoToMessage(conversationId, messageDto);

        messageRepository.save(message);
    }

    private Message mapFromDtoToMessage(String conversationId, MessageDto messageDto) {
        Message message = new Message();
        message.setConversation(conversationRepository.getById(Long.valueOf(conversationId)));
        message.setContent(messageDto.getContent());

        return message;
    }



}
