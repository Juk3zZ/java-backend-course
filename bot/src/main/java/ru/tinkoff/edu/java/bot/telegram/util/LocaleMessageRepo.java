package ru.tinkoff.edu.java.bot.telegram.util;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocaleMessageRepo {

    private final MessageSource messageSource;

    public LocaleMessageRepo(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getMessageByKey(String messageKey, String languageTag) {
        var locale = Locale.forLanguageTag(languageTag);
        return messageSource.getMessage(messageKey, null, locale);
    }
}
