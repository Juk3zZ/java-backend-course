package ru.tinkoff.edu.java.linkparser.parser.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.tinkoff.edu.java.linkparser.model.answer.NotMatchedUriParserAnswer;
import ru.tinkoff.edu.java.linkparser.model.answer.UriParserAnswer;

import java.net.URI;
import java.util.regex.Pattern;

public abstract class CommonUriParser implements UriParser {

    private static final Logger LOG = LogManager.getLogger(CommonUriParser.class);
    protected final String processedAuthority;
    protected UriParser nextParser;
    protected final Pattern pattern;

    protected CommonUriParser(String processedAuthority, Pattern pattern) {
        this.processedAuthority = processedAuthority;
        this.pattern = pattern;
    }

    @Override
    public void setNext(UriParser next) {
        this.nextParser = next;
    }

    @Override
    public UriParserAnswer parse(String uri) {
        if (uri == null) return null;
        var parsedUri = parseToUri(uri);
        if (parsedUri == null) return null;
        var uriAuthority = parsedUri.getAuthority();
        if (processedAuthority.equals(uriAuthority)) {
            var uriParserAnswer = extractPayloadFromUri(parsedUri);
            return uriParserAnswer == null ? new NotMatchedUriParserAnswer() : uriParserAnswer;
        } else {
            return nextParser == null ? new NotMatchedUriParserAnswer() : nextParser.parse(uri);
        }
    }

    protected abstract UriParserAnswer extractPayloadFromUri(URI parsedUrl);

    protected URI parseToUri(String uri) {
        try {
            return URI.create(uri);
        } catch (IllegalArgumentException e) {
            LOG.error("Received URI [{}] is malformed!", uri, e);
            return null;
        }
    }
}
