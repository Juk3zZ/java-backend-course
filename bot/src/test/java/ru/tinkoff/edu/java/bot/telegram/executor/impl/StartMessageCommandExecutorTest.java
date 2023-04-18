package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.edu.java.bot.scrapperapi.ScrapperClient;
import ru.tinkoff.edu.java.bot.telegram.cache.DialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.START;

@ExtendWith(MockitoExtension.class)
public class StartMessageCommandExecutorTest {

    @InjectMocks
    private StartMessageCommandExecutor instance;
    @Mock
    private DialogsStateCache dialogsStateCache;
    @Mock
    private ScrapperClient scrapperClient;
    @Mock
    private LocaleMessageRepo messageRepo;
    @Captor
    private ArgumentCaptor<Long> tgChatIdArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> usernameArgumentCaptor;
    @Captor
    private ArgumentCaptor<BotState> botStateArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> messageKeyArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> languageTagArgumentCaptor;
    private final Random random = new Random();
    @Test
    public void execute_shouldReturnOkMessageWhenNoErrors() {
        doNothing().when(scrapperClient).registerChat(tgChatIdArgumentCaptor.capture(), usernameArgumentCaptor.capture());
        doNothing().when(dialogsStateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        var expected = "You have successfully registered! Now you can track updates by links.";
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn(expected);

        var tgChatId = random.nextLong();
        var username = "Vladimir";
        var actual = instance.execute(new Command(tgChatId, username,"/start", "en", START));

        verify(scrapperClient).registerChat(tgChatIdArgumentCaptor.capture(), usernameArgumentCaptor.capture());
        verify(dialogsStateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(username, usernameArgumentCaptor.getValue()),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals("reply.start", messageKeyArgumentCaptor.getValue()),
                () -> assertEquals("en", languageTagArgumentCaptor.getValue())
        );
    }

}