package ru.tinkoff.edu.java.scrapper.domain.jdbc.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.edu.java.scrapper.domain.model.Chat;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.ChatMapper;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.util.QueriesSource;
import ru.tinkoff.edu.java.scrapper.exception.DatabaseException;

import java.util.List;

@Repository
@Profile("!test")
public class JdbcChatRepository {

    public static final String SELECT_KEY = "app.chats.select";
    public static final String SELECT_BY_ID_KEY = "app.chats.selectById";
    public static final String INSERT_KEY = "app.chats.insert";
    public static final String DELETE_KEY = "app.chats.delete";
    private final JdbcTemplate jdbcTemplate;
    private final ChatMapper chatMapper;
    private final QueriesSource queriesSource;

    public JdbcChatRepository(JdbcTemplate jdbcTemplate, ChatMapper chatMapper, QueriesSource queriesSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatMapper = chatMapper;
        this.queriesSource = queriesSource;
    }

    public List<Chat> findAll() {
        return jdbcTemplate.query(queriesSource.getQuery(SELECT_KEY), chatMapper);
    }

    public Chat findById(long tgChatId) {
        var foundChat = jdbcTemplate.query(queriesSource.getQuery(SELECT_BY_ID_KEY), chatMapper, tgChatId);
        return foundChat.stream().findFirst().orElse(null);
    }

    public boolean add(Chat chat) {
        try {
            jdbcTemplate.update(queriesSource.getQuery(INSERT_KEY), chat.tgChatId(), chat.nickname());
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        } catch (Exception e) {
            throw new DatabaseException(String.format("Something went wrong while inserting %s to DB", chat));
        }
    }

    public boolean remove(long tgChatId) {
        try {
            return jdbcTemplate.update(queriesSource.getQuery(DELETE_KEY), tgChatId) != 0;
        } catch (Exception e) {
            throw new DatabaseException(String.format("Something went wrong while deleting tgChatId=%d to DB", tgChatId));
        }
    }
}
