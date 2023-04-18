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
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.NEW_LINK;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.TRACK;

@ExtendWith(MockitoExtension.class)
public class TrackMessageCommandExecutorTest {

    @InjectMocks
    private TrackMessageCommandExecutor instance;
    @Mock
    private InMemoryDialogsStateCache stateCache;
    @Mock
    private LocaleMessageRepo messageRepo;
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
    public void execute_shouldReturnCorrectMessage() {
        doNothing().when(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        var expected = "Enter link you want to track.";
        when(messageRepo.getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture()))
                .thenReturn(expected);

        var tgChatId = random.nextLong();
        var languageCode = "en";
        var actual = instance.execute(new Command(tgChatId, "Vladimir", "/track", languageCode, TRACK));

        verify(stateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(messageRepo).getMessageByKey(messageKeyArgumentCaptor.capture(), languageTagArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(NEW_LINK, botStateArgumentCaptor.getValue()),
                () -> assertEquals("reply.track", messageKeyArgumentCaptor.getValue()),
                () -> assertEquals(languageCode, languageTagArgumentCaptor.getValue())
        );
    }
}