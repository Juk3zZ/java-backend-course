package ru.tinkoff.edu.java.scrapper.domain.util;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class QueriesSource {

    private final MessageSource messageSource;

    public QueriesSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getQuery(String key) {
        return messageSource.getMessage(key, null, Locale.US);
    }
}
