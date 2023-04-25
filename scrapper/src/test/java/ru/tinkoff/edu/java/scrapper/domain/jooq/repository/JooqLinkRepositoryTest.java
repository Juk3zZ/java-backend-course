package ru.tinkoff.edu.java.scrapper.domain.jooq.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.IntegrationEnvironment;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.LinkMapper;
import ru.tinkoff.edu.java.scrapper.domain.jooq.mapper.LinkFieldsMapper;
import ru.tinkoff.edu.java.scrapper.domain.model.TableLink;
import ru.tinkoff.edu.java.scrapper.domain.util.MappingUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class JooqLinkRepositoryTest extends IntegrationEnvironment {

    @DynamicPropertySource
    public static void props(DynamicPropertyRegistry registry) {
        registry.add("app.access-type", () -> "jooq");
    }

    @TestConfiguration
    public static class JooqTestConfiguration {

        @Bean
        @Primary
        public JdbcTemplate pgJdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        @Primary
        public TransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
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

        @Bean
        @Primary
        public JooqLinkRepository pgJooqChatRepository(
                DSLContext dslContext,
                MappingUtils mappingUtils,
                ObjectMapper objectMapper,
                LinkFieldsMapper linkFieldsMapper
        ) {
            return new JooqLinkRepository(dslContext, mappingUtils, objectMapper, linkFieldsMapper);
        }
    }

    @Autowired
    private JooqLinkRepository instance;
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
        var updateInfo = Map.of("open_issues_count", (Object) 1);
        instance.save(link, currentTime, updateInfo);

        var savedLink = jdbcTemplate.queryForObject("SELECT * FROM app.links WHERE link LIKE ?", linkMapper, link);

        assertAll(
                () -> assertEquals(link, savedLink.link()),
                () -> assertEquals(currentTime, savedLink.updatedAt()),
                () -> assertEquals(updateInfo, savedLink.updateInfo())
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
                () -> assertEquals(removedLink, new TableLink(linkId, linkToSave, updatedAt, emptyMap())),
                () -> assertTrue(storedTgChatId.isEmpty()),
                () -> assertTrue(storedLinkId.isEmpty())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findAll_shouldReturnAllStoredLinks() throws SQLException {
        var gitUpdateInfo = Map.of("open_issues_count", (Object) 1);
        var stackoverflowUpdateInfo = Map.of("answer_count", (Object) 29);
        var links = new java.util.ArrayList<>(List.of(
                new TableLink(
                        1,
                        "https://github.com/VladimirZaitsev21/some-repo",
                        new Timestamp(System.currentTimeMillis()),
                        gitUpdateInfo
                ),
                new TableLink(
                        2,
                        "https://github.com/JohnDoe/navigator",
                        new Timestamp(System.currentTimeMillis()),
                        gitUpdateInfo
                ),
                new TableLink(
                        3,
                        "https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c",
                        new Timestamp(System.currentTimeMillis()),
                        stackoverflowUpdateInfo
                )
        ));

        var pGobject = new PGobject();
        pGobject.setType("jsonb");
        pGobject.setValue("{\"open_issues_count\": 1}");
        var pGobject1 = new PGobject();
        pGobject1.setType("jsonb");
        pGobject1.setValue("{\"answer_count\": 29}");
        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at, update_info) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)",
                links.get(0).link(), links.get(0).updatedAt(), pGobject,
                links.get(1).link(), links.get(1).updatedAt(), pGobject,
                links.get(2).link(), links.get(2).updatedAt(), pGobject1
        );

        var allLinks = instance.findAll();

        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links", Long.class);
        links.set(0, new TableLink(linksIds.get(0), links.get(0).link(), links.get(0).updatedAt(), links.get(0).updateInfo()));
        links.set(1, new TableLink(linksIds.get(1), links.get(1).link(), links.get(1).updatedAt(), links.get(1).updateInfo()));
        links.set(2, new TableLink(linksIds.get(2), links.get(2).link(), links.get(2).updatedAt(), links.get(2).updateInfo()));

        assertEquals(links, allLinks);
    }

    @Test
    @Transactional
    @Rollback
    public void findAll_shouldReturnTrackingLinksForTgChatId() {
        var tgChatId = random.nextLong();
        var links = new java.util.ArrayList<>(List.of(
                new TableLink(1, "https://github.com/VladimirZaitsev21/some-repo", new Timestamp(System.currentTimeMillis()), emptyMap()),
                new TableLink(2, "https://github.com/JohnDoe/navigator", new Timestamp(System.currentTimeMillis()), emptyMap()),
                new TableLink(3, "https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c", new Timestamp(System.currentTimeMillis()), emptyMap())
        ));

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);
        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at) VALUES (?, ?), (?, ?), (?, ?)",
                links.get(0).link(), links.get(0).updatedAt(),
                links.get(1).link(), links.get(1).updatedAt(),
                links.get(2).link(), links.get(2).updatedAt()
        );
        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links LIMIT 2", Long.class);
        jdbcTemplate.update(
                "INSERT INTO app.trackings(tg_chat_id, link_id) VALUES (?, ?), (?, ?)",
                tgChatId, linksIds.get(0), tgChatId, linksIds.get(1)
        );

        var allLinksById = instance.findAll(tgChatId);

        links.set(0, new TableLink(linksIds.get(0), links.get(0).link(), links.get(0).updatedAt(), links.get(0).updateInfo()));
        links.set(1, new TableLink(linksIds.get(1), links.get(1).link(), links.get(1).updatedAt(), links.get(1).updateInfo()));
        assertEquals(links.subList(0, 2), allLinksById);
    }

    @Test
    @Transactional
    @Rollback
    public void findOld_shouldReturnOnlyOldLinks() throws SQLException {
        var tgChatId = random.nextLong();
        var gitUpdateInfo = Map.of("open_issues_count", (Object) 1);
        var stackoverflowUpdateInfo = Map.of("answer_count", (Object) 29);
        var links = new java.util.ArrayList<>(List.of(
                new TableLink(
                        1,
                        "https://github.com/VladimirZaitsev21/some-repo",
                        new Timestamp(System.currentTimeMillis() - 3600000),
                        gitUpdateInfo
                ),
                new TableLink(
                        2,
                        "https://github.com/JohnDoe/navigator",
                        new Timestamp(System.currentTimeMillis() - 3600000),
                        gitUpdateInfo
                ),
                new TableLink(
                        3,
                        "https://stackoverflow.com/questions/1642028/what-is-the-operator-in-c",
                        new Timestamp(System.currentTimeMillis()),
                        stackoverflowUpdateInfo
                )
        ));

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, 'Vladimir')", tgChatId);

        var pGobject = new PGobject();
        pGobject.setType("jsonb");
        pGobject.setValue("{\"open_issues_count\": 1}");
        var pGobject1 = new PGobject();
        pGobject1.setType("jsonb");
        pGobject1.setValue("{\"answer_count\": 29}");
        jdbcTemplate.update(
                "INSERT INTO app.links(link, updated_at, update_info) VALUES (?, ?, ?), (?, ?, ?), (?, ?, ?)",
                links.get(0).link(), links.get(0).updatedAt(), pGobject,
                links.get(1).link(), links.get(1).updatedAt(), pGobject,
                links.get(2).link(), links.get(2).updatedAt(), pGobject1
        );

        var linksIds = jdbcTemplate.queryForList("SELECT id FROM app.links", Long.class);
        jdbcTemplate.update(
                "INSERT INTO app.trackings(tg_chat_id, link_id) VALUES (?, ?), (?, ?), (?, ?)",
                tgChatId, linksIds.get(0), tgChatId, linksIds.get(1), tgChatId, linksIds.get(2)
        );

        var oldLinks = instance.findOld(3000000, System.currentTimeMillis());

        links.set(0, new TableLink(linksIds.get(0), links.get(0).link(), links.get(0).updatedAt(), links.get(0).updateInfo()));
        links.set(1, new TableLink(linksIds.get(1), links.get(1).link(), links.get(1).updatedAt(), links.get(1).updateInfo()));
        links.set(2, new TableLink(linksIds.get(2), links.get(2).link(), links.get(2).updatedAt(), links.get(2).updateInfo()));

        assertEquals(Map.of(links.get(0), List.of(tgChatId), links.get(1), List.of(tgChatId)), oldLinks);
    }
}