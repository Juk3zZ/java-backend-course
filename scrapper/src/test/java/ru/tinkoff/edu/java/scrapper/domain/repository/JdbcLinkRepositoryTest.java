package ru.tinkoff.edu.java.scrapper.domain.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.IntegrationEnvironment;
import ru.tinkoff.edu.java.scrapper.domain.mapper.LinkMapper;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;
import ru.tinkoff.edu.java.scrapper.domain.repository.testconfig.JdbcTestConfiguration;
import ru.tinkoff.edu.java.scrapper.domain.util.QueriesSource;

import javax.sql.DataSource;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({JdbcLinkRepositoryTest.JdbcTemplateTestConfiguration.class, JdbcTestConfiguration.class})
@ActiveProfiles("test")
public class JdbcLinkRepositoryTest extends IntegrationEnvironment {

    @TestConfiguration
    @Profile("test")
    public static class JdbcTemplateTestConfiguration {

        @Bean
        public DataSource pgDataSource() {
            var dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(POSTGRESQL_CONTAINER.getDriverClassName());
            dataSource.setUrl(POSTGRESQL_CONTAINER.getJdbcUrl());
            dataSource.setUsername(POSTGRESQL_CONTAINER.getUsername());
            dataSource.setPassword(POSTGRESQL_CONTAINER.getPassword());
            return dataSource;
        }

        @Bean
        public LinkMapper linkMapper() {
            return new LinkMapper();
        }
        @Bean
        public JdbcLinkRepository jdbcLinkRepository(JdbcTemplate jdbcTemplate, LinkMapper linkMapper, QueriesSource queriesSource) {
            return new JdbcLinkRepository(jdbcTemplate, linkMapper, queriesSource);
        }
    }

    @Autowired
    private JdbcLinkRepository instance;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private LinkMapper linkMapper;
    private final Random random = new Random();

    @Test
    @Transactional
    @Rollback
    public void add_shouldAddLinkAndTrackingIfLinkIsNew() {
        var linkToSave = "https://github.com/VladimirZaitsev21/some-repo";
        var tgChatId = random.nextLong();

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);
        var newLink = instance.add(tgChatId, linkToSave);

        var storedLink = jdbcTemplate.queryForObject("SELECT * FROM app.links", linkMapper);

        var relatedTgChatId = jdbcTemplate.queryForObject("SELECT tg_chat_id FROM app.trackings", Long.class);
        var relatedLinkId = jdbcTemplate.queryForObject("SELECT link_id FROM app.trackings", Long.class);

        assertAll(
                () -> assertNotNull(newLink),
                () -> assertNotNull(storedLink),
                () -> assertEquals(storedLink, newLink),
                () -> assertEquals(tgChatId, relatedTgChatId),
                () -> assertEquals(relatedLinkId, storedLink.id()),
                () -> assertEquals(relatedLinkId, newLink.id())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void add_shouldAddTrackingIfLinkExists() {
        var linkToSave = "https://github.com/VladimirZaitsev21/some-repo";
        var tgChatId = random.nextLong();

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);
        jdbcTemplate.update("INSERT INTO app.links(link) VALUES (?)", linkToSave);
        var newLink = instance.add(tgChatId, linkToSave);

        var relatedTgChatId = jdbcTemplate.queryForObject("SELECT tg_chat_id FROM app.trackings", Long.class);
        var relatedLinkId = jdbcTemplate.queryForObject("SELECT link_id FROM app.trackings", Long.class);

        assertAll(
                () -> assertNotNull(newLink),
                () -> assertEquals(tgChatId, relatedTgChatId),
                () -> assertEquals(relatedLinkId, newLink.id())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void save_shouldSaveLinks() {
        var currentTime = Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis()));
        var link = "https://github.com/VladimirZaitsev21/some-repo";
        instance.save(link, currentTime);

        var savedLink = jdbcTemplate.queryForObject("SELECT * FROM app.links WHERE link LIKE ?", linkMapper, link);

        assertAll(
                () -> assertEquals(link, savedLink.link().toString()),
                () -> assertEquals(currentTime, savedLink.updatedAt())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void remove_shouldRemoveLinkFromTracking() {
        var tgChatId = random.nextLong();
        var linkToSave = "https://github.com/VladimirZaitsev21/some-repo";
        var updatedAt = new Timestamp(System.currentTimeMillis());

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);
        jdbcTemplate.update("INSERT INTO app.links(link, updated_at) VALUES (?, ?)", linkToSave, updatedAt);
        var linkId = jdbcTemplate.queryForObject("SELECT id FROM app.links WHERE link LIKE ?", Long.class, linkToSave);
        jdbcTemplate.update("INSERT INTO app.trackings(tg_chat_id, link_id) VALUES (?, ?)", tgChatId, linkId);

        var removedLink = instance.remove(tgChatId, linkToSave);

        var storedTgChatId = jdbcTemplate.queryForList("SELECT tg_chat_id FROM app.trackings", Long.class);
        var storedLinkId = jdbcTemplate.queryForList("SELECT link_id FROM app.trackings", Long.class);

        assertAll(
                () -> assertEquals(removedLink, new Link(linkId, URI.create(linkToSave), updatedAt)),
                () -> assertTrue(storedTgChatId.isEmpty()),
                () -> assertTrue(storedLinkId.isEmpty())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findAll_shouldReturnAllStoredLinks() {
        var links = new java.util.ArrayList<>(List.of(
                new Link(1, URI.create("https://github.com/VladimirZaitsev21/some-repo"), new Timestamp(System.currentTimeMillis())),
                new Link(2, URI.create("https://github.com/JohnDoe/navigator"), new Timestamp(System.currentTimeMillis())),
                new Link(3, URI.create("https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c"), new Timestamp(System.currentTimeMillis()))
        ));

        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at) VALUES (?, ?), (?, ?), (?, ?)",
                links.get(0).link().toString(), links.get(0).updatedAt(),
                links.get(1).link().toString(), links.get(1).updatedAt(),
                links.get(2).link().toString(), links.get(2).updatedAt()
        );

        var allLinks = instance.findAll();

        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links", Long.class);
        links.set(0, new Link(linksIds.get(0), links.get(0).link(), links.get(0).updatedAt()));
        links.set(1, new Link(linksIds.get(1), links.get(1).link(), links.get(1).updatedAt()));
        links.set(2, new Link(linksIds.get(2), links.get(2).link(), links.get(2).updatedAt()));

        assertEquals(links, allLinks);
    }

    @Test
    @Transactional
    @Rollback
    public void findAll_shouldReturnTrackingLinksForTgChatId() {
        var tgChatId = random.nextLong();
        var links = new java.util.ArrayList<>(List.of(
                new Link(1, URI.create("https://github.com/VladimirZaitsev21/some-repo"), new Timestamp(System.currentTimeMillis())),
                new Link(2, URI.create("https://github.com/JohnDoe/navigator"), new Timestamp(System.currentTimeMillis())),
                new Link(3, URI.create("https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c"), new Timestamp(System.currentTimeMillis()))
        ));

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);
        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at) VALUES (?, ?), (?, ?), (?, ?)",
                links.get(0).link().toString(), links.get(0).updatedAt(),
                links.get(1).link().toString(), links.get(1).updatedAt(),
                links.get(2).link().toString(), links.get(2).updatedAt()
        );
        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links LIMIT 2", Long.class);
        jdbcTemplate.update(
                "INSERT INTO app.trackings(tg_chat_id, link_id) VALUES (?, ?), (?, ?)",
                tgChatId, linksIds.get(0), tgChatId, linksIds.get(1)
        );

        var allLinksById = instance.findAll(tgChatId);

        links.set(0, new Link(linksIds.get(0), links.get(0).link(), links.get(0).updatedAt()));
        links.set(1, new Link(linksIds.get(1), links.get(1).link(), links.get(1).updatedAt()));
        assertEquals(links.subList(0, 2), allLinksById);
    }

    @Test
    @Transactional
    @Rollback
    public void findOld_shouldReturnOnlyOldLinks() {
        var tgChatId = random.nextLong();
        var links = new java.util.ArrayList<>(List.of(
                new Link(1, URI.create("https://github.com/VladimirZaitsev21/some-repo"), new Timestamp(System.currentTimeMillis() - 3600000)),
                new Link(2, URI.create("https://github.com/JohnDoe/navigator"), new Timestamp(System.currentTimeMillis() - 3600000)),
                new Link(3, URI.create("https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c"), new Timestamp(System.currentTimeMillis()))
        ));

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);
        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at) VALUES (?, ?), (?, ?), (?, ?)",
                links.get(0).link().toString(), links.get(0).updatedAt(),
                links.get(1).link().toString(), links.get(1).updatedAt(),
                links.get(2).link().toString(), links.get(2).updatedAt()
        );

        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links", Long.class);
        jdbcTemplate.update(
                "INSERT INTO app.trackings(tg_chat_id, link_id) VALUES (?, ?), (?, ?), (?, ?)",
                tgChatId, linksIds.get(0), tgChatId, linksIds.get(1), tgChatId, linksIds.get(2)
        );

        var oldLinks = instance.findOld(3000000, System.currentTimeMillis());

        links.set(0, new Link(linksIds.get(0), links.get(0).link(), links.get(0).updatedAt()));
        links.set(1, new Link(linksIds.get(1), links.get(1).link(), links.get(1).updatedAt()));
        links.set(2, new Link(linksIds.get(2), links.get(2).link(), links.get(2).updatedAt()));

        assertEquals(Map.of(links.get(0), List.of(tgChatId), links.get(1), List.of(tgChatId)), oldLinks);
    }
}