package ru.tinkoff.edu.java.scrapper.service.impl.jpa;

import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.domain.jpa.repository.JpaChatRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaChat;
import ru.tinkoff.edu.java.scrapper.service.api.ChatService;

@Transactional
public class JpaChatService implements ChatService {

    private final JpaChatRepository chatRepository;

    public JpaChatService(JpaChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void register(long tgChatId, String username) {
        chatRepository.save(JpaChat.builder().tgChatId(tgChatId).nickname(username).build());
    }

    @Override
    public void unregister(long tgChatId) {
        chatRepository.deleteById(tgChatId);
    }
}
