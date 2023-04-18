package ru.tinkoff.edu.java.scrapper.service.api;

import ru.tinkoff.edu.java.scrapper.domain.model.Link;

import java.net.URI;
import java.util.List;

public interface LinkService {

    Link add(long tgChatId, URI url);

    Link remove(long tgChatId, URI url);

    List<Link> getTrackingLinks(long tgChatId);
}
