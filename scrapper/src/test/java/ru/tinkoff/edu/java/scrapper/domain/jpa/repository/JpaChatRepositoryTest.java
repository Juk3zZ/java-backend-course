package ru.tinkoff.edu.java.scrapper.domain.jpa.repository;

import org.junit.jupiter.api.Test;
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
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.ChatMapper;
import ru.tinkoff.edu.java.scrapper.domain.model.jpa.JpaChat;

import javax.sql.DataSource;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(JpaChatRepositoryTest.JpaTestConfiguration.class)
public class JpaChatRepositoryTest extends IntegrationEnvironment {

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
    private ChatMapper chatMapper;
    @Autowired
    private JpaChatRepository instance;
    private final Random random = new Random();

    @Test
    @Transactional
    @Rollback
    public void save_shouldSaveNewChatToDB() {
        var tgChatId = random.nextLong();
        var nickname = "Vladimir";
        var chat = new JpaChat(tgChatId, nickname);

        var jpaChat = instance.save(chat);
        instance.flush();

        var savedChat = jdbcTemplate.queryForObject("SELECT * FROM app.chats", chatMapper);

        assertAll(
                () -> assertEquals(tgChatId, savedChat.tgChatId()),
                () -> assertEquals(tgChatId, jpaChat.getTgChatId()),
                () -> assertEquals(nickname, savedChat.nickname()),
                () -> assertEquals(nickname, jpaChat.getNickname())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void deleteById_shouldDeleteChatWithCorrectIdFromDB() {
        var tgChatId = random.nextLong();

        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, ?)", tgChatId, "Vladimir");

        instance.deleteById(tgChatId);
        instance.flush();

        var chats = jdbcTemplate.query("SELECT * FROM app.chats", chatMapper);

        assertTrue(chats.isEmpty());
    }

    @Test
    @Transactional
    @Rollback
    public void findById_shouldReturnCorrectChat() {
        var tgChatId1 = random.nextLong();
        var tgChatId2 = random.nextLong();

        var nick1 = "user1";
        var nick2 = "user2";
        jdbcTemplate.update("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (?, ?), (?, ?)", tgChatId1, nick1, tgChatId2, nick2);

        var chat = instance.findById(tgChatId1);

        assertAll(
                () -> assertTrue(chat.isPresent()),
                () -> assertEquals(tgChatId1, chat.get().getTgChatId()),
                () -> assertEquals(nick1, chat.get().getNickname())
        );
    }

    @Test
    @Transactional
    @Rollback
    public void findById_shouldReturnNullIfNoData() {
        var chat = instance.findById(random.nextLong());
        assertTrue(chat.isEmpty());
    }
}