package ru.tinkoff.edu.java.linkparser.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.tinkoff.edu.java.linkparser.model.UserAndRepo;
import ru.tinkoff.edu.java.linkparser.model.answer.GitHubUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.NotMatchedUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.StackOverflowUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.UriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.api.UriParser;
import ru.tinkoff.edu.java.linkparser.parser.impl.GitHubUriParser;
import ru.tinkoff.edu.java.linkparser.parser.impl.StackOverflowUriParser;
import ru.tinkoff.edu.java.linkparser.parser.impl.UnsupportedUriParser;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UriParsersChainTest {

    private final UriParser gitHubUriParser = new GitHubUriParser("github.com");
    private final UriParser stackOverflowUriParser = new StackOverflowUriParser("stackoverflow.com");
    private final UriParser unsupportedUriParser = new UnsupportedUriParser();

    private final UriParsersChain parsersChain = new UriParsersChain(gitHubUriParser, stackOverflowUriParser, unsupportedUriParser);

    @ParameterizedTest
    @MethodSource("getArgumentsForDoParseTest")
    public void testDoParse(String source, UriParserAnswer expected) {
        var actual = parsersChain.doParse(source);
        assertEquals(expected, actual);
    }

    public static Stream<Arguments> getArgumentsForDoParseTest() {
        return Stream.of(
                Arguments.of(
                    "https://github.com/VladimirZaitsev21/some-repo",
                    new GitHubUriParserAnswer(new UserAndRepo("VladimirZaitsev21", "some-repo"))
                ),
                Arguments.of(
                    "https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c",
                    new StackOverflowUriParserAnswer(1642028)
                ),
                Arguments.of(
                    "https://github.com/VladimirZaitsev21?tab=repositories",
                    new NotMatchedUriParserAnswer()
                ),
                Arguments.of(
                    "https://stackoverflow.com/questions",
                    new NotMatchedUriParserAnswer()
                ),
                Arguments.of(
                    "https://bitbucket.org/VladimirZaitsev21/some-repo",
                    null
                ),
                Arguments.of(
                    "htt://stackoverflow.com/questions/1642028/what-is-the-operator-in-c java",
                    null
                ),
                Arguments.of(
                    null,
                    null
                )
        );
    }
}