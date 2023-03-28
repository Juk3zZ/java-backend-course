package ru.tinkoff.edu.java.linkparser.parser.api;

import ru.tinkoff.edu.java.linkparser.model.answer.UriParserAnswer;

public interface UriParser {

    void setNext(UriParser next);

    UriParserAnswer parse(String uri);
}
