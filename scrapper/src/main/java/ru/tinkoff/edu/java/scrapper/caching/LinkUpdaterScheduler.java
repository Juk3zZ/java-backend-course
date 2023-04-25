package ru.tinkoff.edu.java.scrapper.caching;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.tinkoff.edu.java.scrapper.service.api.LinkUpdater;

@Service
public class LinkUpdaterScheduler {

    private final LinkUpdater linkUpdater;
    private final long expirationInterval;

    public LinkUpdaterScheduler(LinkUpdater linkUpdater, @Qualifier("expirationIntervalMs") long expirationInterval) {
        this.linkUpdater = linkUpdater;
        this.expirationInterval = expirationInterval;
    }


    @Scheduled(fixedDelayString = "#{@schedulerIntervalMs}")
    public void update() {
        linkUpdater.update(expirationInterval);
    }
}