package ru.tinkoff.edu.java.scrapper.controller;

import org.springframework.web.bind.annotation.*;
import ru.tinkoff.edu.java.scrapper.model.request.AddLinkRequest;
import ru.tinkoff.edu.java.scrapper.model.request.RemoveLinkRequest;
import ru.tinkoff.edu.java.scrapper.model.response.LinkResponse;
import ru.tinkoff.edu.java.scrapper.model.response.ListLinksResponse;
import ru.tinkoff.edu.java.scrapper.service.api.LinkService;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/links")
public class LinksController {

    private final LinkService linkService;

    public LinksController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public ListLinksResponse getAllLinks(@RequestHeader("Tg-Chat-Id") long tgChatId) {
        var trackingLinks = linkService.getTrackingLinks(tgChatId);
        return new ListLinksResponse(
                trackingLinks.stream().map(link -> new LinkResponse(link.id(), link.link().toString())).toList(),
                trackingLinks.size()
        );
    }

    @PostMapping
    public LinkResponse addLink(@RequestHeader("Tg-Chat-Id") long tgChatId, @RequestBody AddLinkRequest request) {
        return Optional.of(linkService.add(tgChatId, URI.create(request.link())))
                .map(l -> new LinkResponse(l.id(), l.link().toString())).get();
    }

    @DeleteMapping
    public LinkResponse deleteLink(@RequestHeader("Tg-Chat-Id") long tgChatId, @RequestBody RemoveLinkRequest request) {
        return Optional.of(linkService.remove(tgChatId, URI.create(request.link())))
                .map(l -> new LinkResponse(l.id(), l.link().toString())).get();
    }
}
