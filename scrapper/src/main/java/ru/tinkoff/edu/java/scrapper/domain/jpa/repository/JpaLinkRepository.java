package ru.tinkoff.edu.java.scrapper.domain.jpa.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaLink;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public interface JpaLinkRepository extends JpaRepository<JpaLink, Long> {

    Optional<JpaLink> findByLink(String link);

    List<JpaLink> findByTrackingJpaChatsTgChatId(long tgChatId);

    List<JpaLink> findByUpdatedAtLessThan(Timestamp timeBorder);
}
