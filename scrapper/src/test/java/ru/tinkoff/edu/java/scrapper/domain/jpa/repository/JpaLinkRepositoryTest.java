package ru.tinkoff.edu.java.scrapper.domain.jpa.repository;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.IntegrationEnvironment;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.LinkMapper;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaChat;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaLink;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(JpaLinkRepositoryTest.JpaTestConfiguration.class)
public class JpaLinkRepositoryTest extends IntegrationEnvironment {

    @DynamicPropertySource
    public static void props(DynamicPropertyRegistry registry) {
        registry.add("app.access-type", () -> "jpa");
    }
    @TestConfiguration
    public static class JpaTestConfiguration {

        @Bean
        @Primary
        public JdbcTemplate pgJdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        @Primary
        public DataSource pgDataSource() {
            var dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(POSTGRESQL_CONTAINER.getDriverClassName());
            dataSource.setUrl(POSTGRESQL_CONTAINER.getJdbcUrl());
            dataSource.setUsername(POSTGRESQL_CONTAINER.getUsername());
            dataSource.setPassword(POSTGRESQL_CONTAINER.getPassword());
            return dataSource;
        }
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private JpaLinkRepository instance;
    @Autowired
    private LinkMapper linkMapper;
    private final Random random = new Random();

    @Test
    @Transactional
    @Rollback
    public void save_shouldSaveNewLinkForTgChatIfNotExists() {
        var tgChatId = random.nextLong();
        var chat = new JpaChat(tgChatId, "Vladimir");
        var link = new JpaLink();
        link.setLink("https://github.com/VladimirZaitsev21/some-repo");
        link.setTrackingJpaChats(Set.of(chat));
        link.setUpdatedAt(Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis())));
        link.setUpdateInfo(Map.of("open_issues", 2));

        jdbcTemplate.update("INSERT INTO app.chats (tg_chat_id, nickname) VALUES (?, ?)", tgChatId, chat.getNickname());

        var savedLink = instance.save(link);
        instance.flush();

        var trackingLinkId = jdbcTemplate.queryForObject("SELECT link_id FROM app.trackings", Long.class);
        var tgChatTrackingId = jdbcTemplate.queryForObject("SELECT tg_chat_id FROM app.trackings", Long.class);

        assertAll(
                () -> assertEquals(savedLink.getId(), trackingLinkId),
                () -> assertEquals(link.getLink(), savedLink.getLink()),
                () -> assertEquals(link.getUpdatedAt(), savedLink.getUpdatedAt()),
                () -> assertEquals(link.getUpdateInfo(), savedLink.getUpdateInfo()),
                () -> assertEquals(link.getTrackingJpaChats(), Set.of(chat)),
                () -> assertEquals(tgChatId, tgChatTrackingId)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void save_shouldSaveNewLinkForTgChatIfExists() throws SQLException {
        var tgChatId = random.nextLong();
        var chat = new JpaChat(tgChatId, "Vladimir");
        var link = new JpaLink();
        link.setLink("https://github.com/VladimirZaitsev21/some-repo");
        link.setTrackingJpaChats(Set.of(chat));
        link.setUpdatedAt(Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis())));
        link.setUpdateInfo(Map.of("open_issues", 2));

        var updateInfo = new PGobject();
        updateInfo.setType("jsonb");
        updateInfo.setValue("{\"open_issues_count\": 1}");

        jdbcTemplate.update("INSERT INTO app.chats (tg_chat_id, nickname) VALUES (?, ?)", tgChatId, chat.getNickname());
        jdbcTemplate.update("INSERT INTO app.links (link, updated_at, update_info) VALUES (?, ?, ?)", link.getLink(), link.getUpdatedAt(), updateInfo);
        var linkId = jdbcTemplate.queryForObject("SELECT id FROM app.links", Long.class);
        link.setId(linkId);

        var savedLink = instance.save(link);
        instance.flush();

        var trackingLinkId = jdbcTemplate.queryForObject("SELECT link_id FROM app.trackings", Long.class);
        var tgChatTrackingId = jdbcTemplate.queryForObject("SELECT tg_chat_id FROM app.trackings", Long.class);

        assertAll(
                () -> assertEquals(savedLink.getId(), trackingLinkId),
                () -> assertEquals(link.getLink(), savedLink.getLink()),
                () -> assertEquals(link.getUpdatedAt(), savedLink.getUpdatedAt()),
                () -> assertEquals(link.getUpdateInfo(), savedLink.getUpdateInfo()),
                () -> assertEquals(link.getTrackingJpaChats(), Set.of(chat)),
                () -> assertEquals(tgChatId, tgChatTrackingId)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findByLink_shouldReturnCorrectLinkIfExists() throws SQLException {

        var updateInfo = new PGobject();
        updateInfo.setType("jsonb");
        updateInfo.setValue("{\"open_issues_count\": 1}");

        var url = "https://github.com/VladimirZaitsev21/some-repo";
        var updatedAt = Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis()));
        jdbcTemplate.update("INSERT INTO app.links (link, updated_at, update_info) VALUES (?, ?, ?)", url, updatedAt, updateInfo);
        var linkId = jdbcTemplate.queryForObject("SELECT id FROM app.links", Long.class);
        var jpaLink = instance.findByLink(url).get();

        assertAll(
                () -> assertEquals(linkId, jpaLink.getId()),
                () -> assertEquals(url, jpaLink.getLink()),
                () -> assertEquals(updatedAt, jpaLink.getUpdatedAt()),
                () -> assertEquals(Map.of("open_issues_count", 1), jpaLink.getUpdateInfo())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findByTrackingJpaChatsTgChatId_shouldReturnTrackingLinksForTgChatId() {
        var tgChatId = random.nextLong();
        var jpaChat = JpaChat.builder().tgChatId(tgChatId).nickname("Vladimir").build();
        var links = new java.util.ArrayList<>(List.of(
                JpaLink.builder()
                        .link("https://github.com/VladimirZaitsev21/some-repo")
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .trackingJpaChats(Set.of(jpaChat))
                        .build(),
                JpaLink.builder()
                        .link("https://github.com/JohnDoe/navigator")
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .trackingJpaChats(Set.of(jpaChat))
                        .build(),
                JpaLink.builder()
                        .link("https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c")
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .build()
        ));

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);
        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at) VALUES (?, ?), (?, ?), (?, ?)",
                links.get(0).getLink(), links.get(0).getUpdatedAt(),
                links.get(1).getLink(), links.get(1).getUpdatedAt(),
                links.get(2).getLink(), links.get(2).getUpdatedAt()
        );

        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links LIMIT 2", Long.class);
        jdbcTemplate.update(
                "INSERT INTO app.trackings(tg_chat_id, link_id) VALUES (?, ?), (?, ?)",
                tgChatId, linksIds.get(0), tgChatId, linksIds.get(1)
        );

        var allLinksById = instance.findByTrackingJpaChatsTgChatId(tgChatId);

        links.get(0).setId(linksIds.get(0));
        links.get(1).setId(linksIds.get(1));
        assertAll(
                () -> assertEquals(2, allLinksById.size()),
                () -> assertEquals(links.get(0), allLinksById.get(0)),
                () -> assertEquals(links.get(1), allLinksById.get(1))
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findByUpdatedAtLessThan_shouldReturnOnlyOldLinks() throws SQLException {
        var tgChatId = random.nextLong();
        var chat = JpaChat.builder().tgChatId(tgChatId).nickname("Vladimir").build();
        var gitUpdateInfo = Map.of("open_issues_count", (Object) 1);
        var stackoverflowUpdateInfo = Map.of("answer_count", (Object) 29);
        var links = new java.util.ArrayList<>(List.of(
                JpaLink.builder()
                        .link("https://github.com/VladimirZaitsev21/some-repo")
                        .updatedAt(new Timestamp(System.currentTimeMillis() - 3600000))
                        .updateInfo(gitUpdateInfo)
                        .trackingJpaChats(Set.of(chat))
                        .build(),
                JpaLink.builder()
                        .link("https://github.com/JohnDoe/navigator")
                        .updatedAt(new Timestamp(System.currentTimeMillis() - 3600000))
                        .updateInfo(gitUpdateInfo)
                        .trackingJpaChats(Set.of(chat))
                        .build(),
                JpaLink.builder()
                        .link("https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c")
                        .updatedAt(new Timestamp(System.currentTimeMillis()))
                        .updateInfo(stackoverflowUpdateInfo)
                        .trackingJpaChats(Set.of(chat))
                        .build()
        ));

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, ?)", chat.getTgChatId(), chat.getNickname());

        var pGobject = new PGobject();
        pGobject.setType("jsonb");
        pGobject.setValue("{\"open_issues_count\": 1}");
        var pGobject1 = new PGobject();
        pGobject1.setType("jsonb");
        pGobject1.setValue("{\"answer_count\": 29}");
        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at, update_info) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)",
                links.get(0).getLink(), links.get(0).getUpdatedAt(), pGobject,
                links.get(1).getLink(), links.get(1).getUpdatedAt(), pGobject,
                links.get(2).getLink(), links.get(2).getUpdatedAt(), pGobject1
        );

        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links", Long.class);
        jdbcTemplate.update(
                "INSERT INTO app.trackings(tg_chat_id, link_id) VALUES (?, ?), (?, ?), (?, ?)",
                tgChatId, linksIds.get(0), tgChatId, linksIds.get(1), tgChatId, linksIds.get(2)
        );

        var timeBorder = System.currentTimeMillis() - 3000000;

        var oldLinks = instance.findByUpdatedAtLessThan(new Timestamp(timeBorder));

        links.get(0).setId(linksIds.get(0));
        links.get(1).setId(linksIds.get(1));
        links.get(2).setId(linksIds.get(2));

        assertAll(
                () -> assertEquals(2, oldLinks.size()),
                () -> assertEquals(List.of(links.get(0), links.get(1)), oldLinks)
        );
    }
}