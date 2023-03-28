package ru.tinkoff.edu.java.linkparser.model.answer;

public sealed interface UriParserAnswer permits GitHubUriParserAnswer, NotMatchedUriParserAnswer, StackOverflowUriParserAnswer {
}
