package ru.tinkoff.edu.java.linkparser.parser.impl;

import ru.tinkoff.edu.java.linkparser.model.answer.StackOverflowUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.UriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.api.CommonUriParser;

import java.net.URI;
import java.util.regex.Pattern;

public final class StackOverflowUriParser extends CommonUriParser {

    public static final Pattern PATTERN = Pattern.compile("/(questions)/(\\d{1,20})");
    public static final int QUESTION_ID_REGEXP_GROUP_NUMBER = 2;

    public StackOverflowUriParser(String processedAuthority) {
        super(processedAuthority, PATTERN);
    }

    @Override
    protected UriParserAnswer extractPayloadFromUri(URI parsedUri) {
        var matcher = pattern.matcher(parsedUri.getPath());
        if (matcher.find()) return new StackOverflowUriParserAnswer(
                Long.parseLong(matcher.group(QUESTION_ID_REGEXP_GROUP_NUMBER))
        );
        else return null;
    }
}
