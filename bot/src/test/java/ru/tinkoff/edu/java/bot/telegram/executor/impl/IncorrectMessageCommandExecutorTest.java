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
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.INCORRECT;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.MAIN_MENU;

@ExtendWith(MockitoExtension.class)
public class IncorrectMessageCommandExecutorTest {

    @Mock
    private InMemoryDialogsStateCache dialogsStateCache;
    @Mock
    private LocaleMessageRepo messageRepo;
    @InjectMocks
    private IncorrectMessageCommandExecutor instance;
    @Captor
    private ArgumentCaptor<Long> tgChatIdArgumentCaptor;
    @Captor
    private ArgumentCaptor<BotState> botStateArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> messageKeyArgumentCaptor;
    @Captor
    private ArgumentCaptor<String> languageTagArgumentCaptor;

    private final Random random = new Random();

    @Test
    public void execute_shouldReturnSpecialMessage() {
        doNothing().when(dialogsStateCache)
                .setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn("Incorrect command. Try type command from menu.");

        var tgChatId = random.nextLong();
        var givenCommand = new Command(tgChatId, "I don't know your commands", "en", INCORRECT);
        var executeResult = instance.execute(givenCommand);

        verify(dialogsStateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals("Incorrect command. Try type command from menu.", executeResult),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(MAIN_MENU, botStateArgumentCaptor.getValue()),
                () -> assertEquals("en", languageTagArgumentCaptor.getValue()),
                () -> assertEquals("reply.incorrect", messageKeyArgumentCaptor.getValue())
        );
    }
}