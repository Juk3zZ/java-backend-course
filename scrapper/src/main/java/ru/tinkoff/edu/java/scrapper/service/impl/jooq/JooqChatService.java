package ru.tinkoff.edu.java.scrapper.service.impl.jooq;

import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.domain.jooq.repository.JooqChatRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.TableChat;
import ru.tinkoff.edu.java.scrapper.service.api.ChatService;

@Transactional
public class JooqChatService implements ChatService {

    private final JooqChatRepository chatRepository;

    public JooqChatService(JooqChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public void register(long tgChatId, String username) {
        var isRegistered = chatRepository.add(new TableChat(tgChatId, username));
        if (!isRegistered)
            throw new IllegalArgumentException(String.format("Trying to add existing %s", new TableChat(tgChatId, username)));
    }

    @Override
    public void unregister(long tgChatId) {
        var isRemoved = chatRepository.remove(tgChatId);
        if (!isRemoved)
            throw new IllegalArgumentException(String.format("Trying to remove non-existing tgChatId=%d", tgChatId));
    }
}
