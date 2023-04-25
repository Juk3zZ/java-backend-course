package ru.tinkoff.edu.java.scrapper.domain.jooq.repository;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.edu.java.scrapper.IntegrationEnvironment;
import ru.tinkoff.edu.java.scrapper.domain.model.TableChat;

import javax.sql.DataSource;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import({JooqChatRepositoryTest.JooqTestConfiguration.class})
public class JooqChatRepositoryTest extends IntegrationEnvironment {

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
        public JooqChatRepository pgJooqChatRepository(DSLContext dslContext) {
            return new JooqChatRepository(dslContext);
        }
    }

    private final Random random = new Random();

    @Autowired
    private JooqChatRepository instance;
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