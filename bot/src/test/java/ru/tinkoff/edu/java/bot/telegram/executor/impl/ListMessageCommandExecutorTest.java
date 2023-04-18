package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.edu.java.bot.scrapperapi.HttpScrapperClient;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiClientErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiInternalServerErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.model.AllLinksApiResponse;
import ru.tinkoff.edu.java.bot.scrapperapi.model.LinkResponse;
import ru.tinkoff.edu.java.bot.telegram.cache.InMemoryDialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import java.net.URI;
import java.util.List;
import java.util.Random;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.LIST;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;

@ExtendWith(MockitoExtension.class)
public class ListMessageCommandExecutorTest {

    @InjectMocks
    private ListMessageCommandExecutor instance;
    @Mock
    private InMemoryDialogsStateCache stateCache;
    @Mock
    private HttpScrapperClient scrapperClient;
    @Mock
    private LocaleMessageRepo messageRepo;
    @Captor
    private ArgumentCaptor<Long> tgChatArgumentCaptor;
    @Captor
    private ArgumentCaptor<BotState> botStateArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> messageKeyArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> languageTagArgumentCaptor;
    private final Random random = new Random();

    @Test
    public void execute_shouldReturnCorrectlyFormattedString() {
        var gitHubUri = "https://github.com";
        var stackOverflowUri = "https://stackoverflow.com";
        when(scrapperClient.getAllLinks(tgChatArgumentCaptor.capture()))
            .thenReturn(
                new AllLinksApiResponse(
                    List.of(
                        new LinkResponse(1, URI.create(gitHubUri)),
                        new LinkResponse(2, URI.create(stackOverflowUri))
                    ),
                    2
                )
            );
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn("You are tracking %d following links:\n\n");

        var tgChatId = random.nextLong();
        var commandToExecute = new Command(tgChatId, "Vladimir", "/list", "en", LIST);
        var executeResult = instance.execute(commandToExecute);
        var expectedResult = "You are tracking 2 following links:\n\n" + gitHubUri + "\n" + stackOverflowUri + "\n";

        verify(stateCache).setStateForId(tgChatArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(scrapperClient).getAllLinks(tgChatArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(expectedResult, executeResult),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals(tgChatId, tgChatArgumentCaptor.getValue()),
                () -> assertEquals("reply.list", messageKeyArgumentCaptor.getValue()),
                () -> assertEquals("en", languageTagArgumentCaptor.getValue())
        );
    }

    @Test
    public void execute_shouldReturnSpecialMessageWhenNoLinks() {
        when(scrapperClient.getAllLinks(tgChatArgumentCaptor.capture()))
                .thenReturn(new AllLinksApiResponse(emptyList(), 0));
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn("You are not tracking any link now.");

        var tgChatId = random.nextLong();
        var commandToExecute = new Command(tgChatId, "Vladimir", "/list", "en", LIST);
        var executeResult = instance.execute(commandToExecute);
        var expectedResult = "You are not tracking any link now.";

        verify(stateCache).setStateForId(tgChatArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(scrapperClient).getAllLinks(tgChatArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(expectedResult, executeResult),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals(tgChatId, tgChatArgumentCaptor.getValue()),
                () -> assertEquals("reply.list.empty", messageKeyArgumentCaptor.getValue()),
                () -> assertEquals("en", languageTagArgumentCaptor.getValue())
        );
    }

    @Test
    public void execute_shouldReturnSpecialMessageWhenNoSuchId() {
        when(scrapperClient.getAllLinks(tgChatArgumentCaptor.capture()))
                .thenThrow(new ApiClientErrorException(null));
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn("There are incorrect parameters in your request.");

        var tgChatId = random.nextLong();
        var commandToExecute = new Command(tgChatId, "Vladimir", "/list", "en", LIST);
        var executeResult = instance.execute(commandToExecute);
        var expectedResult = "There are incorrect parameters in your request.";

        verify(stateCache).setStateForId(tgChatArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(scrapperClient).getAllLinks(tgChatArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(expectedResult, executeResult),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals(tgChatId, tgChatArgumentCaptor.getValue()),
                () -> assertEquals("reply.list.bad-request", messageKeyArgumentCaptor.getValue()),
                () -> assertEquals("en", languageTagArgumentCaptor.getValue())
        );
    }

    @Test
    public void execute_shouldReturnSpecialMessageWhenNoServerError() {
        when(scrapperClient.getAllLinks(tgChatArgumentCaptor.capture()))
                .thenThrow(new ApiInternalServerErrorException(null));
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn("Server is currently unavailable. Try again later.");

        var tgChatId = random.nextLong();
        var commandToExecute = new Command(tgChatId, "Vladimir", "/list", "en", LIST);
        var executeResult = instance.execute(commandToExecute);
        var expectedResult = "Server is currently unavailable. Try again later.";

        verify(stateCache).setStateForId(tgChatArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(scrapperClient).getAllLinks(tgChatArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(expectedResult, executeResult),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals(tgChatId, tgChatArgumentCaptor.getValue()),
                () -> assertEquals("reply.list.server-error", messageKeyArgumentCaptor.getValue()),
                () -> assertEquals("en", languageTagArgumentCaptor.getValue())
        );
    }
}