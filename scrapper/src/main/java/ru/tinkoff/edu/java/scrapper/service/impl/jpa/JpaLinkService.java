package ru.tinkoff.edu.java.scrapper.service.impl.jpa;

import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.domain.jpa.repository.JpaChatRepository;
import ru.tinkoff.edu.java.scrapper.domain.jpa.repository.JpaLinkRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaLink;
import ru.tinkoff.edu.java.scrapper.exception.DatabaseException;
import ru.tinkoff.edu.java.scrapper.service.api.LinkService;
import ru.tinkoff.edu.java.scrapper.service.model.Link;

import java.net.URI;
import java.util.List;
import java.util.Set;

@Transactional
public class JpaLinkService implements LinkService {

    private final JpaLinkRepository linkRepository;
    private final JpaChatRepository chatRepository;

    public JpaLinkService(JpaLinkRepository linkRepository, JpaChatRepository chatRepository) {
        this.linkRepository = linkRepository;
        this.chatRepository = chatRepository;
    }

    @Override
    public Link add(long tgChatId, URI url) {
        var chat = chatRepository.findById(tgChatId)
                .orElseThrow(() -> new DatabaseException(String.format("No chat is present with tgChatId=[%d]", tgChatId)));
        var link = linkRepository.findByLink(url.toString());

        if (link.isPresent()) {
            link.get().getTrackingJpaChats().add(chat);
            linkRepository.save(link.get());
        } else {
            var newLink = JpaLink.builder().link(url.toString()).trackingJpaChats(Set.of(chat)).build();
            linkRepository.save(newLink);
        }
        return null;
    }

    @Override
    public Link remove(long tgChatId, URI url) {
        var foundLink = linkRepository.findByLink(url.toString()).get();
        foundLink.getTrackingJpaChats().removeIf(c -> c.getTgChatId() == tgChatId);
        linkRepository.save(foundLink);
        return null;
    }

    @Override
    public List<Link> getTrackingLinks(long tgChatId) {
        linkRepository.findByTrackingJpaChatsTgChatId(tgChatId);
        return null;
    }
}
