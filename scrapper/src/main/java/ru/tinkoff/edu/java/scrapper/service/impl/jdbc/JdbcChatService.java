package ru.tinkoff.edu.java.scrapper.service.impl.jdbc;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.domain.repository.JdbcChatRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.Chat;
import ru.tinkoff.edu.java.scrapper.service.api.ChatService;

@Service
@Profile("!test")
@Transactional
public class JdbcChatService implements ChatService {

    private final JdbcChatRepository chatRepository;

    public JdbcChatService(JdbcChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void register(long tgChatId, String username) {
        var isRegistered = chatRepository.add(new Chat(tgChatId, username));
        if (!isRegistered)
            throw new IllegalArgumentException(String.format("Trying to add existing %s", new Chat(tgChatId, username)));
    }

    @Override
    public void unregister(long tgChatId) {
        var isRemoved = chatRepository.remove(tgChatId);
        if (!isRemoved)
            throw new IllegalArgumentException(String.format("Trying to remove non-existing tgChatId=%d", tgChatId));
    }
}
