package ru.tinkoff.edu.java.bot.telegram.executor.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.tinkoff.edu.java.bot.telegram.cache.InMemoryDialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;
import ru.tinkoff.edu.java.bot.telegram.util.LocaleMessageRepo;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.HELP;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;

@ExtendWith(MockitoExtension.class)
public class HelpMessageCommandExecutorTest {

    @InjectMocks
    private HelpMessageCommandExecutor instance;
    @Mock
    private InMemoryDialogsStateCache stateCache;
    @Mock
    private LocaleMessageRepo messageRepo;
    @Captor
    private ArgumentCaptor<String> messageKeyArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> languageTagArgumentCaptor;
    @Captor
    private ArgumentCaptor<Long> tgChatIdArgumentCaptor;
    @Captor
    private ArgumentCaptor<BotState> botStateArgumentCaptor;
    private final Random random = new Random();

    @Test
    public void execute_shouldReturnCorrectHelpMessage() {
        var expected = """
                The bot provides the ability to track updates by links. Links to GitHub and StackOverflow are currently  available.

                List of available commands:
                /start - register to track the updates
                /help - display bot info
                /track -  start tracking links
                /untrack - stop tracking links
                /list - show a list of tracked links""";
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn(expected);
        doNothing().when(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());

        var tgChatId = random.nextLong();
        var result = instance.execute(new Command(tgChatId, "Vladimir", "/help", "en", HELP));

        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());
        verify(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(expected, result),
                () -> assertEquals("reply.help", messageKeyArgumentCaptor.getValue()),
                () -> assertEquals("en", languageTagArgumentCaptor.getValue()),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue())
        );
    }
}