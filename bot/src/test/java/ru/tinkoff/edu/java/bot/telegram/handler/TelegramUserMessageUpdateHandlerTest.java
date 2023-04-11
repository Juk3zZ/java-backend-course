package ru.tinkoff.edu.java.bot.telegram.handler;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.tinkoff.edu.java.bot.telegram.cache.InMemoryDialogsStateCache;
import ru.tinkoff.edu.java.bot.telegram.manager.CommandExecutorsManager;
import ru.tinkoff.edu.java.bot.telegram.model.BotState;
import ru.tinkoff.edu.java.bot.telegram.model.Command;

import java.util.Random;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.tinkoff.edu.java.bot.telegram.model.BotState.*;

@ExtendWith(MockitoExtension.class)
public class TelegramUserMessageUpdateHandlerTest {

    @Mock
    private CommandExecutorsManager commandExecutorsManager;
    @Mock
    private InMemoryDialogsStateCache dialogsStateCache;
    @InjectMocks
    private TelegramUserMessageUpdateHandler instance;
    @Captor
    private ArgumentCaptor<Long> tgChatIdArgumentCaptor;
    @Captor
    private ArgumentCaptor<BotState> botStateArgumentCaptor;
    @Captor
    private ArgumentCaptor<Command> commandArgumentCaptor;
    private final Random random = new Random();

    @ParameterizedTest
    @MethodSource("getArgumentsForHandleMockedStateCache")
    public void handleWithMockedStateCache(
            String executorReturns,
            BotState stateCacheReturns,
            String messageText,
            BotState expectedBotState
    ) {
        when(commandExecutorsManager.processStateful(commandArgumentCaptor.capture())).thenReturn(executorReturns);
        when(dialogsStateCache.getStateById(tgChatIdArgumentCaptor.capture())).thenReturn(stateCacheReturns);

        var tgChatId = random.nextLong();
        var update = new Update();
        var message = new Message();
        var chat = new Chat();
        chat.setId(tgChatId);
        message.setText(messageText);
        message.setChat(chat);
        var from = new User();
        from.setLanguageCode("en");
        message.setFrom(from);
        update.setMessage(message);

        var handleResult = instance.handle(update);

        verify(dialogsStateCache).setStateForId(tgChatIdArgumentCaptor.capture(), botStateArgumentCaptor.capture());
        verify(commandExecutorsManager).processStateful(commandArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(executorReturns, handleResult.getText()),
                () -> assertEquals(tgChatId, Long.parseLong(handleResult.getChatId())),
                () -> assertEquals(tgChatId, tgChatIdArgumentCaptor.getValue()),
                () -> assertEquals(expectedBotState, botStateArgumentCaptor.getValue()),
                () -> assertEquals(new Command(tgChatId, messageText, "en", expectedBotState), commandArgumentCaptor.getValue())
        );
    }

    public static Stream<Arguments> getArgumentsForHandleMockedStateCache() {
        return Stream.of(
                Arguments.of(
                        "Incorrect command. Try type command from menu",
                        MAIN_MENU,
                        "I don't know your commands",
                        INCORRECT
                ),
                Arguments.of(
                        "The link [https://github.com] was successfully added to the tracked.",
                        NEW_LINK,
                        "https://github.com",
                        NEW_LINK
                ),
                Arguments.of(
                        "The link [https://github.com] was successfully removed from the tracking",
                        REMOVING_LINK,
                        "https://github.com",
                        REMOVING_LINK
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getArgumentsForHandle")
    public void handle(
            String executorReturns,
            String messageText,
            BotState expectedBotState
    ) {
        when(commandExecutorsManager.processStateful(commandArgumentCaptor.capture())).thenReturn(executorReturns);

        var tgChatId = random.nextLong();
        var update = new Update();
        var message = new Message();
        var chat = new Chat();
        chat.setId(tgChatId);
        message.setText(messageText);
        message.setChat(chat);
        var from = new User();
        from.setLanguageCode("en");
        message.setFrom(from);
        update.setMessage(message);

        var handleResult = instance.handle(update);

        verify(commandExecutorsManager).processStateful(commandArgumentCaptor.capture());

        assertAll(
                () -> assertEquals(executorReturns, handleResult.getText()),
                () -> assertEquals(tgChatId, Long.parseLong(handleResult.getChatId())),
                () -> assertEquals(new Command(tgChatId, messageText, "en", expectedBotState), commandArgumentCaptor.getValue())
        );
    }

    public static Stream<Arguments> getArgumentsForHandle() {
        return Stream.of(
                Arguments.of(
                        "You have successfully registered! Now you can track updates by links.",
                        "/start",
                        START
                ),
                Arguments.of(
                        "Enter link you want to track.",
                        "/track",
                        TRACK
                ),
                Arguments.of(
                        "Enter link you want to stop tracking.",
                        "/untrack",
                        UNTRACK
                ),
                Arguments.of(
                        "You are not tracking any link now.",
                        "/list",
                        LIST
                ),
                Arguments.of(
                        """
                                The bot provides the ability to track updates by links. Links to GitHub and StackOverflow are currently  available.

                                List of available commands:
                                /start - register to track the updates
                                /help - display bot info
                                /track - start tracking links
                                /untrack - stop tracking links
                                /list - show a list of tracked links""",
                        "/help",
                        HELP
                )
        );
    }
}