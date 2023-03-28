package ru.tinkoff.edu.java.linkparser.parser;

import ru.tinkoff.edu.java.linkparser.model.answer.UriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.api.UriParser;

public class UriParsersChain {

    private final UriParser parserToStartFrom;

    public UriParsersChain(UriParser... uriParsers) {
        parserToStartFrom = uriParsers[0];
        for (int i = 0; i < uriParsers.length - 1; i++) {
            uriParsers[i].setNext(uriParsers[i + 1]);
        }
    }

    public UriParserAnswer doParse(String uri) {
        return parserToStartFrom.parse(uri);
    }
}
