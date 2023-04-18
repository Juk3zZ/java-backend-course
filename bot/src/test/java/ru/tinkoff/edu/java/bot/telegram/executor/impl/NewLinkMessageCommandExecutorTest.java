package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.edu.java.bot.scrapperapi.HttpScrapperClient;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiClientErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.exception.ApiInternalServerErrorException;
import ru.tinkoff.edu.java.bot.scrapperapi.model.LinkResponse;
import ru.tinkoff.edu.java.bot.telegram.cache.InMemoryDialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import java.net.URI;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.NEW_LINK;

@ExtendWith(MockitoExtension.class)
public class NewLinkMessageCommandExecutorTest {

    @InjectMocks
    private NewLinkMessageCommandExecutor instance;
    @Mock
    private HttpScrapperClient scrapperClient;
    @Mock
    private LocaleMessageRepo messageRepo;
    @Mock
    private InMemoryDialogsStateCache stateCache;
    @Captor
    private ArgumentCaptor<Long> tgChatIdArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> linkArgumentCaptor;
    @Captor
    private ArgumentCaptor<BotState> botStateArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> messageTagArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> languageTagArgumentCaptor;

    private final Random random = new Random();

    @Test
    public void execute_shouldReturnOkMessageWhenNoErrors() {
        var uri = "https://github.com";
        when(scrapperClient.addLink(tgChatIdArgumentCaptor.capture(), linkArgumentCaptor.capture())).thenReturn(new LinkResponse(1L, URI.create(uri)));
        doNothing().when(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        var expected = "The link [%s] was successfully added to the tracked.";
        when(messageRepo.getMessageByKey(messageTagArgumentCaptor.capture(), languageTagArgumentCaptor.capture())).thenReturn(expected);

        var tgChatId = random.nextLong();
        var languageTag = "en";
        var actual = instance.execute(new Command(tgChatId, "Vladimir", uri, languageTag, NEW_LINK));

        verify(scrapperClient).addLink(tgChatIdArgumentCaptor.capture(), linkArgumentCaptor.capture());
        verify(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageTagArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(String.format(expected, uri), actual),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(uri, linkArgumentCaptor.getValue()),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals("reply.new-link", messageTagArgumentCaptor.getValue()),
                () -> assertEquals(languageTag, languageTagArgumentCaptor.getValue())
        );
    }

    @Test
    public void execute_shouldReturnErrorMessageWhenClientError() {
        var uri = "https://github.com";
        when(scrapperClient.addLink(tgChatIdArgumentCaptor.capture(), linkArgumentCaptor.capture())).thenThrow(new ApiClientErrorException(null));
        doNothing().when(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        var expected = "You are already tracking this link.";
        when(messageRepo.getMessageByKey(messageTagArgumentCaptor.capture(), languageTagArgumentCaptor.capture())).thenReturn(expected);

        var tgChatId = random.nextLong();
        var languageTag = "en";
        var actual = instance.execute(new Command(tgChatId, "Vladimir", uri, languageTag, NEW_LINK));

        verify(scrapperClient).addLink(tgChatIdArgumentCaptor.capture(), linkArgumentCaptor.capture());
        verify(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageTagArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(String.format(expected, uri), actual),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(uri, linkArgumentCaptor.getValue()),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals("reply.new-link.bad-request", messageTagArgumentCaptor.getValue()),
                () -> assertEquals(languageTag, languageTagArgumentCaptor.getValue())
        );
    }

    @Test
    public void execute_shouldReturnErrorMessageWhenServerError() {
        var uri = "https://github.com";
        when(scrapperClient.addLink(tgChatIdArgumentCaptor.capture(), linkArgumentCaptor.capture())).thenThrow(new ApiInternalServerErrorException(null));
        doNothing().when(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        var expected = "Setting link to track function is currently unavailable, please try again later.";
        when(messageRepo.getMessageByKey(messageTagArgumentCaptor.capture(), languageTagArgumentCaptor.capture())).thenReturn(expected);

        var tgChatId = random.nextLong();
        var languageTag = "en";
        var actual = instance.execute(new Command(tgChatId, "Vladimir", uri, languageTag, NEW_LINK));

        verify(scrapperClient).addLink(tgChatIdArgumentCaptor.capture(), linkArgumentCaptor.capture());
        verify(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageTagArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(String.format(expected, uri), actual),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(uri, linkArgumentCaptor.getValue()),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals("reply.new-link.server-error", messageTagArgumentCaptor.getValue()),
                () -> assertEquals(languageTag, languageTagArgumentCaptor.getValue())
        );
    }
}