package ru.tinkoff.edu.java.scrapper.controller;

import org.springframework.web.bind.annotation.*;
import ru.tinkoff.edu.java.common.exception.IncorrectRequestParamsException;
import ru.tinkoff.edu.java.scrapper.exception.ResourceNotFoundException;
import ru.tinkoff.edu.java.scrapper.model.request.AddLinkRequest;
import ru.tinkoff.edu.java.scrapper.model.response.LinkResponse;
import ru.tinkoff.edu.java.scrapper.model.request.RemoveLinkRequest;
import ru.tinkoff.edu.java.scrapper.model.response.ListLinksResponse;

import java.util.*;

@RestController
@RequestMapping("/links")
public class LinksController {

    private final Map<Long, ListLinksResponse> links = new HashMap<>();
    private final Random random = new Random();

    public LinksController() {
        links.put(123876L, new ListLinksResponse(
            new ArrayList<>(
                Arrays.asList(
                    new LinkResponse(1, "https://github.com/VladimirZaitsev21/some-repo"),
                    new LinkResponse(2, "https://github.com/JohnDoe/navigator"),
                    new LinkResponse(3, "https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c")
            )), 3
        ));
        links.put(675438L, new ListLinksResponse(
            new ArrayList<>(
                Arrays.asList(
                    new LinkResponse(4, "https://stackoverflow.com/questions/5013327/using-multiple-values-httpstatus-in-responsestatus")
            )), 1
        ));
        links.put(574304L, new ListLinksResponse(
            new ArrayList<>(
                Arrays.asList(
                    new LinkResponse(5, "https://stackoverflow.com/questions/2469911/how-do-i-assert-my-exception-message-with-junit-test-annotation")
            )), 1
        ));
    }

    @GetMapping
    public ListLinksResponse getAllLinks(@RequestHeader("Tg-Chat-Id") long tgChatId) {
        var listLinksResponse = links.get(tgChatId);
        if (listLinksResponse != null) return listLinksResponse;
        else throw new ResourceNotFoundException(String.format("No links are able for tg-chat-id=[%s]", tgChatId));
    }

    @PostMapping
    public LinkResponse addLink(@RequestHeader("Tg-Chat-Id") long tgChatId, @RequestBody AddLinkRequest request) {
        var listLinksResponse = links.get(tgChatId);
        var newLinkResponse = new LinkResponse(random.nextLong(), request.link());
        if (listLinksResponse == null) {
            links.put(tgChatId, new ListLinksResponse(new ArrayList<>(Arrays.asList(newLinkResponse)), 1));
        } else {
            if (listLinksResponse.links().stream().anyMatch(linkResponse -> linkResponse.url().equals(newLinkResponse.url()))) {
                throw new IncorrectRequestParamsException("The link you are trying to add already exists");
            } else {
                listLinksResponse.links().add(newLinkResponse);
                links.put(tgChatId, new ListLinksResponse(listLinksResponse.links(), listLinksResponse.size() + 1));
            }
        }
        return newLinkResponse;
    }

    @DeleteMapping
    public LinkResponse deleteLink(@RequestHeader("Tg-Chat-Id") long tgChatId, @RequestBody RemoveLinkRequest request) {
        if (!links.containsKey(tgChatId)) {
            throw new IncorrectRequestParamsException(String.format("There is no such tg-chat-id=[%s]", tgChatId));
        }
        var listLinksResponse = links.get(tgChatId);
        if (!listLinksResponse.links().removeIf(x -> x.url().equals(request.link()))) {
            throw new IncorrectRequestParamsException(String.format("There is no such link=[%s]", request.link()));
        } else {
            links.put(tgChatId, new ListLinksResponse(listLinksResponse.links(), listLinksResponse.size() - 1));
            return new LinkResponse(random.nextLong(), request.link());
        }
    }
}
