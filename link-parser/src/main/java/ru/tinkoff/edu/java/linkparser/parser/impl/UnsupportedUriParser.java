package ru.tinkoff.edu.java.linkparser.parser.impl;

import ru.tinkoff.edu.java.linkparser.model.answer.UriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.api.UriParser;

public final class UnsupportedUriParser implements UriParser {

    private UriParser nextParser;
    @Override
    public void setNext(UriParser next) {
        this.nextParser = next;
    }

    @Override
    public UriParserAnswer parse(String uri) {
        return nextParser == null ? null : nextParser.parse(uri);
    }
}
