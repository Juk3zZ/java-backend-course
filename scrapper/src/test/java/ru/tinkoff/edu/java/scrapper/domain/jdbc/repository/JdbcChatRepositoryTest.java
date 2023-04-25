package ru.tinkoff.edu.java.scrapper.domain.jdbc.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.IntegrationEnvironment;
import ru.tinkoff.edu.java.scrapper.domain.CommonConfig;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.repository.testconfig.JdbcTestConfiguration;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.ChatMapper;
import ru.tinkoff.edu.java.scrapper.domain.model.TableChat;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.util.QueriesSource;

import javax.sql.DataSource;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({JdbcChatRepositoryTest.JdbcTemplateTestConfiguration.class, JdbcTestConfiguration.class, CommonConfig.class})
@ActiveProfiles("test")
public class JdbcChatRepositoryTest extends IntegrationEnvironment {

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
        public ChatMapper chatMapper() {
            return new ChatMapper();
        }

        @Bean
        public JdbcChatRepository pgJdbcChatRepository(JdbcTemplate jdbcTemplate, ChatMapper chatMapper, QueriesSource queriesSource) {
            return new JdbcChatRepository(jdbcTemplate, chatMapper, queriesSource);
        }
    }

    private final Random random = new Random();
    @Autowired
    private JdbcChatRepository instance;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @Rollback
    public void add_shouldInsertOneRowToTable() {
        var chat = new TableChat(random.nextLong(), "Vladimir");

        var addResult = instance.add(chat);
        var chatById = getChatFromDB(chat);

        assertAll(
                () -> assertTrue(addResult),
                () -> assertEquals(chat, chatById)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void remove_shouldDeleteCorrectRow() {
        var chat = new TableChat(random.nextLong(), "Vladimir");
        var deletedRows = jdbcTemplate.update(
                "INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, ?)",
                chat.tgChatId(), chat.nickname()
        );

        var removeResult = instance.remove(chat.tgChatId());
        var chatById = getChatFromDB(chat);
        assertAll(
                () -> assertTrue(removeResult),
                () -> assertNull(chatById),
                () -> assertEquals(1, deletedRows)
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findAll_shouldReturnListOfChats() {
        var chats = List.of(
                new TableChat(random.nextLong(), "Vladimir"),
                new TableChat(random.nextLong(), "Alexander"),
                new TableChat(random.nextLong(), "Alexey")
        );

        jdbcTemplate.update(
                "INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, ?), (?, ?), (?, ?)",
                chats.get(0).tgChatId(), chats.get(0).nickname(),
                chats.get(1).tgChatId(), chats.get(1).nickname(),
                chats.get(2).tgChatId(), chats.get(2).nickname()
        );

        var foundChats = instance.findAll();

        assertEquals(chats, foundChats);
    }

    @Test
    @Transactional
    @Rollback
    public void findAll_shouldReturnEmptyListIfNoData() {
        assertTrue(instance.findAll().isEmpty());
    }

    @Test
    @Transactional
    @Rollback
    public void findById_shouldReturnCorrectChat() {
        var chat = new TableChat(random.nextLong(), "Vladimir");
        jdbcTemplate.update(
                "INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, ?)",
                chat.tgChatId(), chat.nickname()
        );

        var chatById = instance.findById(chat.tgChatId());

        assertEquals(chat, chatById);
    }

    @Test
    @Transactional
    @Rollback
    public void findById_shouldReturnNullIfNoData() {
        assertNull(instance.findById(random.nextLong()));
    }
    private TableChat getChatFromDB(TableChat chat) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT * FROM app.chats WHERE tg_chat_id = ?",
                    (rs, rowNum) -> new TableChat(rs.getLong(1), rs.getString(2)),
                    chat.tgChatId()
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}