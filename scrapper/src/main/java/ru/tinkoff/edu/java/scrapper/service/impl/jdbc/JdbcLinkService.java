package ru.tinkoff.edu.java.scrapper.service.impl.jdbc;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.domain.repository.JdbcLinkRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;
import ru.tinkoff.edu.java.scrapper.service.api.LinkService;

import java.net.URI;
import java.util.List;

@Service
@Transactional
public class JdbcLinkService implements LinkService {

    private final JdbcLinkRepository linkRepository;

    public JdbcLinkService(JdbcLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public Link add(long tgChatId, URI url) {
        return linkRepository.add(tgChatId, url.toString());
    }

    @Override
    public Link remove(long tgChatId, URI url) {
        return linkRepository.remove(tgChatId, url.toString());
    }

    @Override
    public List<Link> getTrackingLinks(long tgChatId) {
        return linkRepository.findAll(tgChatId);
    }
}
