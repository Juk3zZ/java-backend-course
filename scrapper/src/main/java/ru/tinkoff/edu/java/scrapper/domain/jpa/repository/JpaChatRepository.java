package ru.tinkoff.edu.java.scrapper.domain.jpa.repository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaChat;

@Repository
@ConditionalOnProperty(prefix = "app", name = "access-type", havingValue = "jpa")
public interface JpaChatRepository extends JpaRepository<JpaChat, Long> {
}
