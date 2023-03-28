package ru.tinkoff.edu.java.linkparser.parser.impl;

import ru.tinkoff.edu.java.linkparser.model.UserAndRepo;
import ru.tinkoff.edu.java.linkparser.model.answer.GitHubUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.UriParserAnswer;
import ru.tinkoff.edu.java.linkparser.parser.api.CommonUriParser;

import java.net.URI;
import java.util.regex.Pattern;

public final class GitHubUriParser extends CommonUriParser {

    public static final Pattern PATTERN = Pattern.compile("/(.+)/(.+)");
    public static final int USER_REGEXP_GROUP_NUMBER = 1;
    public static final int REPO_REGEXP_GROUP_NUMBER = 2;

    public GitHubUriParser(String processedAuthority) {
        super(processedAuthority, PATTERN);
    }

    @Override
    protected UriParserAnswer extractPayloadFromUri(URI parsedUri) {
        var matcher = pattern.matcher(parsedUri.getPath());
        if (matcher.find()) return new GitHubUriParserAnswer(
                new UserAndRepo(matcher.group(USER_REGEXP_GROUP_NUMBER), matcher.group(REPO_REGEXP_GROUP_NUMBER))
        );
        else return null;
    }
}
