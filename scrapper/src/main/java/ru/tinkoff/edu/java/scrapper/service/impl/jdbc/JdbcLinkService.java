package ru.tinkoff.edu.java.scrapper.service.impl.jdbc;

import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.repository.JdbcLinkRepository;
import ru.tinkoff.edu.java.scrapper.domain.model.TableLink;
import ru.tinkoff.edu.java.scrapper.service.api.LinkService;
import ru.tinkoff.edu.java.scrapper.service.model.Link;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Transactional
public class JdbcLinkService implements LinkService {

    private final JdbcLinkRepository linkRepository;

    public JdbcLinkService(JdbcLinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    @Override
    public Link add(long tgChatId, URI url) {
        return convert(linkRepository.add(tgChatId, url.toString()));
    }

    @Override
    public Link remove(long tgChatId, URI url) {
        return convert(linkRepository.remove(tgChatId, url.toString()));
    }

    @Override
    public List<Link> getTrackingLinks(long tgChatId) {
        return linkRepository.findAll(tgChatId).stream().map(this::convert).toList();
    }

    private Link convert(TableLink source) {
        return new Link(
                source.id(),
                URI.create(source.link()),
                OffsetDateTime.ofInstant(source.updatedAt().toInstant(), ZoneId.systemDefault()),
                source.updateInfo()
        );
    }
}
